package com.example.docscanner.util

import com.example.docscanner.model.Document
import kotlin.math.ln

/**
 * Ranked search over a document's own words: title, tags, and the recognised text
 * of its pages.
 *
 * ## Why an index and not a `contains`
 *
 * The home screen used to ask, for every keystroke and every page, whether the
 * page's text contained the query. That is a full re-scan of the whole library per
 * character typed, and it can only answer "yes/no" — it cannot put the best match
 * first, and it cannot show *why* something matched.
 *
 * [Index] is built once per library change — on demand, on a background
 * dispatcher, a few milliseconds of tokenising — and answers queries from an
 * inverted index with BM25 scoring, so results arrive ranked with the sentence
 * that matched.
 *
 * ## Why BM25
 *
 * It is the boring, well-understood answer: term frequency saturates (a page that
 * says "invoice" twenty times is not twenty times more relevant), long pages are
 * normalised, and rare terms count more than common ones. Title and tag matches are
 * weighted above body text, because a document called "Tax return 2024" should win
 * over one that merely mentions it.
 *
 * This is lexical search: it matches the words that are there. Semantic matching —
 * finding "billing" when the page says "invoice" — needs an embedding model, which
 * is the next step documented in `docs/AGENT_COORDINATION.md` §6i.
 */
object DocumentSearch {

    /** One ranked result: the document, its score, and where it matched. */
    data class Hit(
        val document: Document,
        val score: Float,
        /** The matched passage, with a little context, or `null` for a title-only match. */
        val snippet: String?
    )

    /** Title matches outrank body matches. */
    private const val TITLE_WEIGHT = 3.2f

    /** A tag is a human-assigned keyword: worth more than one body occurrence. */
    private const val TAG_WEIGHT = 2.0f

    /** Matching the whole query as one phrase is a strong signal. */
    private const val PHRASE_BONUS = 4.0f

    /** …especially in the title. */
    private const val TITLE_PHRASE_BONUS = 6.0f

    /** Every term present somewhere: the query was specific and it landed. */
    private const val ALL_TERMS_BONUS = 1.5f

    private const val BM25_K1 = 1.2f
    private const val BM25_B = 0.75f

    /** Characters of context either side of the first match in a snippet. */
    private const val SNIPPET_RADIUS = 70

    private const val MIN_TOKEN_LENGTH = 2

    /**
     * An immutable snapshot of the library, ready to be queried.
     *
     * Build it with `remember(documents) { DocumentSearch.Index(documents) }`: it
     * depends only on the list of documents, never on the query.
     */
    class Index(documents: List<Document>) {

        private class Posting(val document: Int, val page: Int, val frequency: Int)

        private val documents: List<Document> = documents
        private val postings = HashMap<String, MutableList<Posting>>()

        /** Body token count per document, for BM25 length normalisation. */
        private val lengths = IntArray(documents.size)

        /** Raw page text, kept for snippets — snippets must show the original case. */
        private val pageTexts: Array<List<String>> =
            Array(documents.size) { documents[it].pages.map { page -> page.ocrText } }

        private val loweredPages: Array<List<String>> =
            Array(documents.size) { index -> pageTexts[index].map { it.lowercase() } }

        private val loweredTitles: List<String> = documents.map { it.title.lowercase() }
        private val loweredTags: List<List<String>> =
            documents.map { doc -> doc.tags.map { it.lowercase() } }

        private val averageLength: Float

        init {
            var total = 0
            for (index in documents.indices) {
                loweredPages[index].forEachIndexed { page, text ->
                    val tokens = tokenize(text)
                    lengths[index] += tokens.size
                    if (tokens.isEmpty()) return@forEachIndexed
                    val counts = HashMap<String, Int>()
                    for (token in tokens) counts[token] = (counts[token] ?: 0) + 1
                    for ((term, frequency) in counts) {
                        postings.getOrPut(term) { ArrayList() }.add(Posting(index, page, frequency))
                    }
                }
                total += lengths[index]
            }
            averageLength = if (documents.isEmpty()) {
                1f
            } else {
                (total.toFloat() / documents.size).coerceAtLeast(1f)
            }
        }

        /** True when there is any text to search at all. */
        val isEmpty: Boolean get() = postings.isEmpty() && documents.all { it.title.isBlank() }

        /**
         * Ranks the library against [query].
         *
         * @return documents that match at least one term, best first. An empty
         *   query returns an empty list — the caller shows the unfiltered library.
         */
        fun search(query: String): List<Hit> {
            val terms = tokenize(query.lowercase()).distinct()
            if (terms.isEmpty() || documents.isEmpty()) return emptyList()

            val scores = FloatArray(documents.size)
            val matchedTerms = IntArray(documents.size)
            val bestPage = IntArray(documents.size) { -1 }
            val bestPageScore = FloatArray(documents.size)

            for (term in terms) {
                val list = postings[term]
                if (!list.isNullOrEmpty()) {
                    // BM25 inverse document frequency: rare terms carry the query.
                    val idf = ln(1f + (documents.size - list.size + 0.5f) / (list.size + 0.5f))
                    for (posting in list) {
                        val target = posting.document
                        val lengthNorm = lengths[target] / averageLength
                        val saturated = (posting.frequency * (BM25_K1 + 1f)) /
                            (posting.frequency + BM25_K1 * (1f - BM25_B + BM25_B * lengthNorm))
                        val contribution = idf * saturated
                        scores[target] += contribution
                        matchedTerms[target]++
                        if (contribution > bestPageScore[target]) {
                            bestPageScore[target] = contribution
                            bestPage[target] = posting.page
                        }
                    }
                }
                // Title and tags are short, so plain containment is the right test.
                for (index in documents.indices) {
                    if (loweredTitles[index].contains(term)) scores[index] += TITLE_WEIGHT
                    if (loweredTags[index].any { it.contains(term) }) scores[index] += TAG_WEIGHT
                }
            }

            val phrase = query.trim().lowercase()
            if (phrase.length >= MIN_TOKEN_LENGTH) {
                for (index in documents.indices) {
                    if (loweredTitles[index].contains(phrase)) scores[index] += TITLE_PHRASE_BONUS
                    if (loweredPages[index].any { it.contains(phrase) }) scores[index] += PHRASE_BONUS
                }
            }
            if (terms.size > 1) {
                for (index in documents.indices) {
                    if (matchedTerms[index] == terms.size) scores[index] += ALL_TERMS_BONUS
                }
            }

            val hits = ArrayList<Hit>()
            for (index in documents.indices) {
                if (scores[index] <= 0f) continue
                hits.add(
                    Hit(
                        document = documents[index],
                        score = scores[index],
                        snippet = snippet(index, bestPage[index], terms)
                    )
                )
            }
            hits.sortWith(
                compareByDescending<Hit> { it.score }
                    .thenByDescending { it.document.modifiedDate }
            )
            return hits
        }

        /**
         * A window of the page that matched, centred on the first query term found,
         * with whitespace collapsed. Returns `null` for a title-only match.
         */
        private fun snippet(documentIndex: Int, pageIndex: Int, terms: List<String>): String? {
            val pages = pageTexts[documentIndex]
            if (pages.isEmpty()) return null
            // Prefer the page that scored; fall back to scanning the others so a title
            // match on a long document still shows something useful.
            val preferred = if (pageIndex in pages.indices) pageIndex else -1
            val order = if (preferred >= 0) listOf(preferred) + pages.indices.filter { it != preferred } else pages.indices.toList()
            for (page in order) {
                val text = pages[page]
                if (text.isBlank()) continue
                val lowered = text.lowercase()
                var at = -1
                for (term in terms) {
                    val found = lowered.indexOf(term)
                    if (found >= 0 && (at < 0 || found < at)) at = found
                }
                if (at < 0) continue
                val start = (at - SNIPPET_RADIUS).coerceAtLeast(0)
                val end = (at + SNIPPET_RADIUS).coerceAtMost(text.length)
                val body = text.substring(start, end)
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (body.isEmpty()) continue
                return buildString {
                    if (start > 0) append("… ")
                    append(body)
                    if (end < text.length) append(" …")
                }
            }
            return null
        }
    }

    /**
     * Splits text into searchable tokens.
     *
     * Unicode-aware rather than ASCII-only: a scan can be in any script, and
     * `[^a-z0-9]` would have thrown away every word of it.
     */
    internal fun tokenize(text: String): List<String> {
        val tokens = ArrayList<String>()
        val current = StringBuilder()
        for (character in text) {
            if (character.isLetterOrDigit()) {
                current.append(character.lowercaseChar())
            } else if (current.isNotEmpty()) {
                if (current.length >= MIN_TOKEN_LENGTH) tokens.add(current.toString())
                current.setLength(0)
            }
        }
        if (current.length >= MIN_TOKEN_LENGTH) tokens.add(current.toString())
        return tokens
    }
}
