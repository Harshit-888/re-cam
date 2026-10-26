package com.example.docscanner.util

import com.example.docscanner.model.Document
import kotlin.math.ln

/**
 * **Magic Search**: ranked search over everything the app knows about a document.
 *
 * Two kinds of evidence, because a scan has two:
 *
 * * the **words** it contains — a page's recognised text, the title, the tags;
 * * the **shape** of the question — which words matter, which are ignored, and how
 *   sure we are that a word the user typed is a word on the page.
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
 * ## What makes it "magic" rather than lexical
 *
 * Each of these costs a little precision and buys a lot of recall; all of them are
 * weighted so an exact hit always beats an inferred one:
 *
 * | the user types | what is also searched | weight |
 * | :--- | :--- | :--- |
 * | `invoic` | any term starting with it | 0.85 |
 * | `invoce` (typo) | terms within edit distance 1 | 0.55 |
 * | `aadhr` (bad typo) | terms within edit distance 2 | 0.30 |
 * | `aadhar` | the curated spelling group `aadhaar` | 0.90 |
 * | `invoices` | the stem `invoice` | 1.00 |
 * | `"tax return"` | the contiguous phrase, in a page or a title | +4 / +6 |
 *
 * Fuzzy matching is length-gated on purpose: two edits on a five-letter word turns
 * "tax" into "toy", and a search that answers the wrong question is worse than a
 * search that answers none.
 *
 * The vocabulary walk that powers prefix and fuzzy matching is the one part of this
 * file that is worth native code: it is O(vocabulary) per query and the vocabulary
 * is the largest structure the index holds. [NativeSearch] runs it in Rust when the
 * kernel is present *and* has been proven identical to [expandReference] on this
 * device; otherwise this file does it in Kotlin. See [expand].
 *
 * This is still lexical search plus spelling tolerance. Semantic matching —
 * finding "billing" when the page says "invoice" — needs an embedding model, which
 * is the next step documented in `docs/AGENT_COORDINATION.md` §6i.
 */
object DocumentSearch {

    /** One ranked result: the document, its score, and where it matched. */
    data class Hit(
        val document: Document,
        val score: Float,
        /** The matched passage, with a little context, or `null` for a title-only match. */
        val snippet: String?,
        /**
         * Ranges **within [snippet]** to emphasise: the terms that actually matched,
         * including the ones that matched by stem rather than verbatim. Sorted and
         * non-overlapping, so a UI can build one `AnnotatedString` from them.
         */
        val highlights: List<IntRange> = emptyList()
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

    /**
     * Every term on the *same page*.
     *
     * This is a cheap stand-in for proximity scoring, and for a scanned document it
     * is the right one: "GST" on page 2 and "invoice" on page 9 means the document
     * mentions both, while both on page 2 means the user is looking at that page.
     */
    private const val SAME_PAGE_BONUS = 2.0f

    /**
     * How much a recent document is preferred when relevance is otherwise equal.
     *
     * Small on purpose — 0.4 is less than a single body occurrence of a rare term —
     * so it reorders near-ties ("two receipts, the newer one") without ever
     * promoting an irrelevant recent scan over a relevant old one.
     */
    private const val RECENCY_WEIGHT = 0.4f

    /** Exact stem. The reference weight every other match is discounted against. */
    private const val WEIGHT_EXACT = 1.0f

    /** A typed prefix of a word: how people search while typing. */
    private const val WEIGHT_PREFIX = 0.85f

    /** One edit away: a typo, or a legitimate variant spelling. */
    private const val WEIGHT_FUZZY_1 = 0.55f

    /** Two edits away. Believable on a long word, noise on a short one. */
    private const val WEIGHT_FUZZY_2 = 0.30f

    /** A curated spelling group ("aadhar" → "aadhaar"), not a semantic guess. */
    private const val WEIGHT_SYNONYM = 0.90f

    private const val BM25_K1 = 1.2f
    private const val BM25_B = 0.75f

    /** Characters of context either side of the first match in a snippet. */
    private const val SNIPPET_RADIUS = 70

    private const val MIN_TOKEN_LENGTH = 2

    /** Below three characters a prefix would match half the vocabulary. */
    private const val PREFIX_MIN_LENGTH = 3

    /** Longest query term the fuzzy matcher will try; longer is a paste, not a word. */
    private const val FUZZY_MAX_TERM_LENGTH = 24

    /** One edit is only believable on a word this long. */
    private const val FUZZY_MIN_LENGTH_1 = 5

    /** …and two only on one this long. */
    private const val FUZZY_MIN_LENGTH_2 = 8

    /** Ceiling for the native matcher's DP, and the Kotlin reference's. */
    private const val FUZZY_MAX_DISTANCE = 2

    /**
     * Snippets are built for the top results only.
     *
     * Building one scans the matching pages and allocates a string per hit; doing it
     * for a 400-result search to show the first twelve is work nobody sees. Results
     * past this depth fall back to the date label, which is what they showed before
     * search existed.
     */
    private const val SNIPPET_LIMIT = 80

    /** Documents given the (relatively expensive) phrase and same-page checks. */
    private const val CANDIDATE_LIMIT = 240

    /** Needles a snippet will highlight. Beyond this the result is a different query. */
    private const val MAX_HIGHLIGHTS = 12

    /**
     * Words that carry no signal in a document search.
     *
     * Dropped only when the query has something else left: `"the"` on its own is a
     * legitimate thing to search for, and a search that silently answers nothing is
     * worse than one that answers something useless.
     */
    private val STOPWORDS = setOf(
        "the", "and", "for", "with", "from", "that", "this", "these", "those",
        "are", "was", "were", "have", "has", "had", "but", "not", "you", "your",
        "its", "his", "her", "our", "their", "of", "to", "in", "on", "at", "by",
        "as", "is", "it", "be", "an", "or"
    )

    /**
     * Curated spelling and abbreviation groups — **not** a thesaurus.
     *
     * Every entry here is the same thing written two ways (Aadhaar is transliterated
     * half a dozen ways, and "dob" is how the form itself labels the field on a
     * government document). Nothing here asserts that two *different* things are
     * similar; that is the embedding model's job (§6i), and guessing it with a
     * hand-written list would be the kind of invented constant that makes a search
     * engine feel random.
     */
    private val SYNONYMS: Map<String, List<String>> = mapOf(
        "aadhar" to listOf("aadhaar", "adhar"),
        "aadhaar" to listOf("aadhar", "adhar"),
        "adhar" to listOf("aadhaar", "aadhar"),
        "pancard" to listOf("pan"),
        "pan" to listOf("pancard"),
        "dob" to listOf("birth"),
        "birth" to listOf("dob"),
        "inv" to listOf("invoice"),
        "invc" to listOf("invoice"),
        "rcpt" to listOf("receipt"),
        "gstin" to listOf("gst"),
        "licence" to listOf("license"),
        "license" to listOf("licence"),
        "cheque" to listOf("check"),
        "phone" to listOf("mobile"),
        "mobile" to listOf("phone")
    )

    /**
     * An immutable snapshot of the library, ready to be queried.
     *
     * Build it with `remember(documents) { DocumentSearch.Index(documents) }`: it
     * depends only on the list of documents, never on the query.
     */
    class Index(documents: List<Document>) {

        private class Posting(val document: Int, val page: Int, val frequency: Int)

        private val documents: List<Document> = documents

        /** Body postings, keyed by **stem** so "invoice" and "invoices" meet. */
        private val postings = HashMap<String, MutableList<Posting>>()

        /** Documents whose title contains the stem. Kept apart from [postings]. */
        private val titlePostings = HashMap<String, MutableList<Int>>()

        /** Documents carrying the stem as a tag. */
        private val tagPostings = HashMap<String, MutableList<Int>>()

        /**
         * The stems, once, in a stable order.
         *
         * This is what the prefix and fuzzy matchers scan, and what [packStrings]
         * serialises for the native matcher. Sorted, so a mismatch between the two
         * implementations is reproducible rather than hash-order-dependent.
         *
         * Assigned at the **end of `init`**, not here: property initialisers run in
         * declaration order, and `postings` is still empty at this point.
         */
        private var vocabulary: Array<String> = emptyArray()

        /** [vocabulary], length-prefixed, so the matcher does not re-pack it per keystroke. */
        private var packedVocabulary: ByteArray = ByteArray(0)

        /** Body token count per document, for BM25 length normalisation. */
        private val lengths = IntArray(documents.size)

        /** Raw page text, kept for snippets — snippets must show the original case. */
        private val pageTexts: Array<List<String>> =
            Array(documents.size) { documents[it].pages.map { page -> page.ocrText } }

        private val loweredPages: Array<List<String>> =
            Array(documents.size) { index -> pageTexts[index].map { it.lowercase() } }

        private val loweredTitles: List<String> = documents.map { it.title.lowercase() }

        /**
         * Four-digit years mentioned anywhere in a document, for `2024`-style queries.
         *
         * Collected page by page rather than by concatenating the document: a year
         * scan that allocates a copy of every page's text, once per library change,
         * is a needless spike on a library of a few hundred pages.
         */
        private val years: Array<IntArray> = Array(documents.size) { index ->
            val found = LinkedHashSet<Int>(4)
            fun collect(text: String) {
                YEAR_PATTERN.findAll(text).forEach { match ->
                    match.value.toIntOrNull()?.takeIf { it in MIN_YEAR..MAX_YEAR }?.let(found::add)
                }
            }
            collect(loweredTitles[index])
            loweredPages[index].forEach { text -> collect(text) }
            found.toIntArray()
        }

        /** Newest document in the library: "now", for a library-relative recency nudge. */
        private val newestModified: Long =
            documents.maxOfOrNull { it.modifiedDate } ?: 0L

        private val averageLength: Float

        init {
            var total = 0
            for (index in documents.indices) {
                loweredPages[index].forEachIndexed { page, text ->
                    val tokens = tokenize(text)
                    lengths[index] += tokens.size
                    if (tokens.isEmpty()) return@forEachIndexed
                    val counts = HashMap<String, Int>()
                    for (token in tokens) {
                        val key = stem(token)
                        counts[key] = (counts[key] ?: 0) + 1
                    }
                    for ((term, frequency) in counts) {
                        postings.getOrPut(term) { ArrayList() }.add(Posting(index, page, frequency))
                    }
                }
                for (token in tokenize(loweredTitles[index])) {
                    val key = stem(token)
                    if (!titlePostings.containsSlotFor(key, index)) {
                        titlePostings.getOrPut(key) { ArrayList() }.add(index)
                    }
                }
                for (tag in documents[index].tags) {
                    for (token in tokenize(tag.lowercase())) {
                        val key = stem(token)
                        if (!tagPostings.containsSlotFor(key, index)) {
                            tagPostings.getOrPut(key) { ArrayList() }.add(index)
                        }
                    }
                }
                total += lengths[index]
            }
            averageLength = if (documents.isEmpty()) {
                1f
            } else {
                (total.toFloat() / documents.size).coerceAtLeast(1f)
            }
            // Every word the index knows about, not just the page words: a typo in a
            // word that only ever appears in a *title* still has to be findable, and
            // expansion can only offer words the vocabulary contains.
            vocabulary = (postings.keys + titlePostings.keys + tagPostings.keys)
                .distinct()
                .sorted()
                .toTypedArray()
            packedVocabulary = packStrings(vocabulary.toList())
        }

        private fun HashMap<String, MutableList<Int>>.containsSlotFor(key: String, index: Int): Boolean =
            this[key]?.contains(index) == true

        /** True when there is any text to search at all. */
        val isEmpty: Boolean get() = postings.isEmpty() && documents.all { it.title.isBlank() }

        /**
         * Ranks the library against [query].
         *
         * @return documents that match at least one term, best first. An empty
         *   query returns an empty list — the caller shows the unfiltered library.
         */
        fun search(query: String): List<Hit> {
            val parsed = parseQuery(query)
            if (parsed.terms.isEmpty() || documents.isEmpty()) return emptyList()

            val total = documents.size
            val scores = FloatArray(total)
            val matchedTerms = IntArray(total)
            val bestPage = IntArray(total) { -1 }
            val bestPageScore = FloatArray(total)
            val excluded: Set<String> = parsed.excluded.map { stem(it) }.toSet()

            // Per-term scratch, so a document that matches through three different
            // expansions of the same word is scored once, at its best weight.
            val termBest = FloatArray(total)
            val touched = ArrayList<Int>(64)
            val titleBest = FloatArray(total)
            val titleTouched = ArrayList<Int>(16)
            val tagBest = FloatArray(total)
            val tagTouched = ArrayList<Int>(16)

            // One walk of the vocabulary for the whole query: three typed words must
            // not copy the same vocabulary into the matcher three times.
            val bases = parsed.terms.map { stem(it) }
            val walks: List<List<Expansion>> = runVocabularyWalk(
                packedVocabulary = packedVocabulary,
                vocabulary = vocabulary,
                queries = bases,
                maxDistance = FUZZY_MAX_DISTANCE
            ).groupBy { it.queryIndex }.let { grouped ->
                bases.indices.map { grouped[it].orEmpty() }
            }

            for (termIndex in parsed.terms.indices) {
                val raw = parsed.terms[termIndex]
                val candidates = expandTerm(raw, bases[termIndex], walks[termIndex])
                if (candidates.isEmpty()) continue
                touched.clear()
                titleTouched.clear()
                tagTouched.clear()

                for (candidate in candidates) {
                    val list = postings[candidate.stem]
                    if (!list.isNullOrEmpty()) {
                        // BM25 inverse document frequency: rare terms carry the query.
                        val idf = ln(1f + (total - list.size + 0.5f) / (list.size + 0.5f))
                        for (posting in list) {
                            val target = posting.document
                            val lengthNorm = lengths[target] / averageLength
                            val saturated = (posting.frequency * (BM25_K1 + 1f)) /
                                (posting.frequency + BM25_K1 * (1f - BM25_B + BM25_B * lengthNorm))
                            val contribution = idf * saturated * candidate.weight
                            if (contribution <= 0f) continue
                            if (termBest[target] == 0f) touched.add(target)
                            if (contribution > termBest[target]) {
                                termBest[target] = contribution
                                if (contribution > bestPageScore[target]) {
                                    bestPageScore[target] = contribution
                                    bestPage[target] = posting.page
                                }
                            }
                        }
                    }

                    titlePostings[candidate.stem]?.forEach { target ->
                        val bonus = TITLE_WEIGHT * candidate.weight
                        if (titleBest[target] == 0f) titleTouched.add(target)
                        if (bonus > titleBest[target]) titleBest[target] = bonus
                    }
                    tagPostings[candidate.stem]?.forEach { target ->
                        val bonus = TAG_WEIGHT * candidate.weight
                        if (tagBest[target] == 0f) tagTouched.add(target)
                        if (bonus > tagBest[target]) tagBest[target] = bonus
                    }
                }

                // A title or tag match is evidence too: fold it in at the strongest
                // weight, and let it count towards "every term matched".
                for (target in titleTouched) {
                    if (titleBest[target] > termBest[target]) {
                        if (termBest[target] == 0f) touched.add(target)
                        termBest[target] = titleBest[target]
                    }
                    titleBest[target] = 0f
                }
                for (target in tagTouched) {
                    if (tagBest[target] > termBest[target]) {
                        if (termBest[target] == 0f) touched.add(target)
                        termBest[target] = tagBest[target]
                    }
                    tagBest[target] = 0f
                }

                for (target in touched) {
                    scores[target] += termBest[target]
                    matchedTerms[target]++
                    termBest[target] = 0f
                }
            }

            // ---- document-level evidence, candidates only -----------------------
            val candidates = ArrayList<Int>()
            for (index in documents.indices) {
                if (scores[index] > 0f) candidates.add(index)
            }
            if (candidates.isEmpty()) return emptyList()

            // `-draft` removes a document that contains the excluded word *anywhere*,
            // which is the only reading that matches what people mean by it.
            if (excluded.isNotEmpty()) {
                candidates.removeAll { documentContainsAll(it, excluded) }
                if (candidates.isEmpty()) return emptyList()
            }

            candidates.sortByDescending { scores[it] }
            val checked = candidates.take(CANDIDATE_LIMIT)

            val needles = highlightNeedles(parsed.terms)
            // Quoted phrases, plus the query as written: "tax return" typed without
            // quotes should still reward the document that says it in that order.
            // Each bonus is applied at most once per document, however many forms of
            // the phrase it happens to contain.
            val phraseForms = ArrayList<String>(parsed.phrases)
            if (parsed.terms.size > 1) phraseForms.add(parsed.terms.joinToString(" "))
            val phrases = phraseForms.filter { it.length >= MIN_TOKEN_LENGTH }.distinct()
            for (index in checked) {
                if (phrases.any { phraseInPages(index, it) }) scores[index] += PHRASE_BONUS
                if (phrases.any { loweredTitles[index].contains(it) }) scores[index] += TITLE_PHRASE_BONUS
                if (matchedTerms[index] == parsed.terms.size && parsed.terms.size > 1) {
                    scores[index] += ALL_TERMS_BONUS
                }
                if (parsed.terms.size > 1 && allTermsOnOnePage(index, parsed.terms)) {
                    scores[index] += SAME_PAGE_BONUS
                }
                if (parsed.years.isNotEmpty() && parsed.years.any { it in years[index] }) {
                    // A year in the query that the document actually mentions is a
                    // filter the user did not have to learn: "tax 2023" is a query
                    // nobody needs a syntax for.
                    scores[index] += ALL_TERMS_BONUS
                }
                scores[index] += RECENCY_WEIGHT * recencyFactor(index)
            }

            val ranked = candidates.sortedWith(
                compareByDescending<Int> { scores[it] }
                    .thenByDescending { documents[it].modifiedDate }
                    .thenBy { loweredTitles[it] }
            )

            val hits = ArrayList<Hit>(ranked.size)
            ranked.forEachIndexed { rank, index ->
                val passage = if (rank < SNIPPET_LIMIT) {
                    snippet(index, bestPage[index], needles)
                } else {
                    null
                }
                hits.add(
                    Hit(
                        document = documents[index],
                        score = scores[index],
                        snippet = passage?.first,
                        highlights = passage?.second ?: emptyList()
                    )
                )
            }
            return hits
        }

        // ------------------------------------------------------------- expansion

        /**
         * The stems [raw] should be allowed to match, with a weight each.
         *
         * Order of authority: the word itself, then its curated spelling group, then
         * prefixes of it, then words within a typo of it. Duplicates collapse to the
         * strongest weight, so "aadhar" does not get the exact *and* the synonym
         * bonus for the same page.
         */
        private fun expandTerm(
            raw: String,
            base: String,
            rows: List<Expansion>
        ): List<Candidate> {
            val best = LinkedHashMap<String, Float>()
            fun offer(key: String, weight: Float) {
                if (key.isEmpty()) return
                val current = best[key]
                if (current == null || weight > current) best[key] = weight
            }

            offer(base, WEIGHT_EXACT)
            for (synonym in SYNONYMS[base].orEmpty()) offer(stem(synonym), WEIGHT_SYNONYM)
            for (synonym in SYNONYMS[raw].orEmpty()) offer(stem(synonym), WEIGHT_SYNONYM)

            for (row in rows) {
                val match = vocabulary[row.vocabularyIndex]
                when {
                    row.code == 0 -> offer(match, WEIGHT_EXACT)
                    row.code == -1 -> offer(match, WEIGHT_PREFIX)
                    row.code == 1 && base.length >= FUZZY_MIN_LENGTH_1 -> offer(match, WEIGHT_FUZZY_1)
                    row.code == 2 && base.length >= FUZZY_MIN_LENGTH_2 -> offer(match, WEIGHT_FUZZY_2)
                }
            }
            return best.map { (matchedStem, weight) -> Candidate(matchedStem, weight) }
        }

        // ------------------------------------------------------------ evidence

        /** True when a page of [index] contains [phrase] contiguously (case-folded). */
        private fun phraseInPages(index: Int, phrase: String): Boolean =
            loweredPages[index].any { it.contains(phrase) }

        /**
         * Every term appears on the *same* page of [index].
         *
         * Both the typed word and its stem are tried, because the page holds the
         * word as it was written: `invoices` on the page has to satisfy the query
         * `invoice`, and `invoice` has to satisfy `invoices`.
         */
        private fun allTermsOnOnePage(index: Int, terms: List<String>): Boolean {
            val needles = terms.flatMap { listOf(it, stem(it)) }.distinct()
            for (text in loweredPages[index]) {
                if (needles.all { text.contains(it) }) return true
            }
            return false
        }

        /** An exclusion (`-draft`) is satisfied when the document has that word anywhere. */
        private fun documentContainsAll(index: Int, stems: Set<String>): Boolean {
            val haystack = buildString {
                append(loweredTitles[index])
                loweredPages[index].forEach { append(' ').append(it) }
                documents[index].tags.forEach { append(' ').append(it.lowercase()) }
            }
            return stems.all { haystack.contains(it) }
        }

        /**
         * 1.0 for a document modified today, ~0.5 at a year old, relative to the
         * newest document in the library rather than to the wall clock — so the
         * ranking is reproducible and a library that has not changed ranks the same
         * tomorrow as it did today.
         */
        private fun recencyFactor(index: Int): Float {
            val ageDays = (newestModified - documents[index].modifiedDate).coerceAtLeast(0L) /
                MILLIS_PER_DAY
            return 1f / (1f + ageDays.toFloat() / DAYS_PER_YEAR)
        }

        private fun highlightNeedles(terms: List<String>): List<String> {
            val needles = LinkedHashSet<String>()
            for (term in terms) {
                if (term.length >= MIN_TOKEN_LENGTH) needles.add(term)
                val stemmed = stem(term)
                if (stemmed.length >= MIN_TOKEN_LENGTH) needles.add(stemmed)
                if (needles.size >= MAX_HIGHLIGHTS) break
            }
            return needles.toList()
        }

        // ------------------------------------------------------------- snippets

        /**
         * A window of the page that matched, centred on the first query term found,
         * with whitespace collapsed and the matching words marked.
         *
         * Returns `null` for a title-only match.
         */
        private fun snippet(
            documentIndex: Int,
            pageIndex: Int,
            needles: List<String>
        ): Pair<String, List<IntRange>>? {
            val pages = pageTexts[documentIndex]
            if (pages.isEmpty()) return null
            // Prefer the page that scored; fall back to scanning the others so a title
            // match on a long document still shows something useful.
            val preferred = if (pageIndex in pages.indices) pageIndex else -1
            val order = if (preferred >= 0) {
                listOf(preferred) + pages.indices.filter { it != preferred }
            } else {
                pages.indices.toList()
            }
            for (page in order) {
                val text = pages[page]
                if (text.isBlank()) continue
                val lowered = text.lowercase()
                var at = -1
                for (needle in needles) {
                    val found = lowered.indexOf(needle)
                    if (found >= 0 && (at < 0 || found < at)) at = found
                }
                if (at < 0) continue
                val start = (at - SNIPPET_RADIUS).coerceAtLeast(0)
                val end = (at + SNIPPET_RADIUS).coerceAtMost(text.length)
                val body = text.substring(start, end)
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (body.isEmpty()) continue
                val snippet = buildString {
                    if (start > 0) append("… ")
                    append(body)
                    if (end < text.length) append(" …")
                }
                return snippet to highlightRanges(snippet, needles)
            }
            return null
        }

        /**
         * Where the needles sit in [snippet], merged and sorted.
         *
         * Re-found in the collapsed snippet rather than mapped through the substring:
         * whitespace collapsing moves every offset after it, and an off-by-one
         * highlight is worse than none.
         */
        private fun highlightRanges(snippet: String, needles: List<String>): List<IntRange> {
            val lowered = snippet.lowercase()
            val raw = ArrayList<IntRange>()
            for (needle in needles) {
                var from = 0
                while (raw.size < MAX_HIGHLIGHTS * 2) {
                    val at = lowered.indexOf(needle, from)
                    if (at < 0) break
                    raw.add(at until (at + needle.length))
                    from = at + needle.length
                }
            }
            if (raw.isEmpty()) return emptyList()
            raw.sortBy { it.first }
            val merged = ArrayList<IntRange>(raw.size)
            for (range in raw) {
                val last = merged.lastOrNull()
                if (last != null && range.first <= last.last + 1) {
                    merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
                } else {
                    merged.add(range)
                }
            }
            return merged
        }
    }

    /** One stem the query is allowed to match, and how much it counts. */
    internal data class Candidate(val stem: String, val weight: Float)

    /** One vocabulary match: which query, which vocabulary entry, and how close. */
    internal data class Expansion(val queryIndex: Int, val vocabularyIndex: Int, val code: Int)

    // ---------------------------------------------------------------- query

    /**
     * What the user meant, separated from what they typed.
     *
     * Supported, in the order people discover it:
     *
     * * `invoice` — a word; stemmed, prefix- and typo-tolerant.
     * * `"tax return"` — a contiguous phrase: the phrase bonus only.
     * * `-draft` — exclude documents that contain the word.
     * * `2024` — also treated as a year filter, matched against the digits found in
     *   the document. No syntax to learn.
     */
    internal data class ParsedQuery(
        val terms: List<String>,
        val phrases: List<String>,
        val excluded: List<String>,
        val years: List<Int>
    )

    /** Splits a raw query into words, quoted phrases, exclusions and years. */
    internal fun parseQuery(raw: String): ParsedQuery {
        val lowered = raw.lowercase()
        val phrases = ArrayList<String>()
        val excluded = ArrayList<String>()
        val plain = StringBuilder()

        var index = 0
        while (index < lowered.length) {
            val character = lowered[index]
            when {
                character == '"' -> {
                    val close = lowered.indexOf('"', index + 1)
                    val phrase = if (close < 0) {
                        lowered.substring(index + 1)
                    } else {
                        lowered.substring(index + 1, close)
                    }
                    val normalised = phrase.trim().replace(Regex("\\s+"), " ")
                    if (normalised.isNotEmpty()) phrases.add(normalised)
                    index = if (close < 0) lowered.length else close + 1
                }
                character == '-' && (index == 0 || !lowered[index - 1].isLetterOrDigit()) -> {
                    // A minus at the start of a word excludes it (and is not a hyphen
                    // inside one: "e-mail" stays one token).
                    var end = index + 1
                    val word = StringBuilder()
                    while (end < lowered.length && lowered[end].isLetterOrDigit()) {
                        word.append(lowered[end])
                        end++
                    }
                    if (word.length >= MIN_TOKEN_LENGTH) excluded.add(word.toString())
                    index = end
                }
                else -> {
                    plain.append(character)
                    index++
                }
            }
        }

        val tokens = tokenize(plain.toString())
        val withoutStopwords = if (tokens.size > 1) tokens.filterNot { it in STOPWORDS } else tokens
        // A query that is *only* a phrase ("tax return") still has to search for
        // something: its words are the terms, and the phrase bonus then decides the
        // order. Without this, quoting a phrase would return nothing at all.
        var terms = (if (withoutStopwords.isEmpty()) tokens else withoutStopwords).distinct()
        if (terms.isEmpty() && phrases.isNotEmpty()) {
            terms = phrases.flatMap { tokenize(it) }.distinct()
        }
        val years = terms.filter { term ->
            term.length == 4 && term.all { it.isDigit() } &&
                term.toInt() in MIN_YEAR..MAX_YEAR
        }.map { it.toInt() }
        return ParsedQuery(
            terms = terms,
            phrases = phrases.distinct(),
            excluded = excluded.distinct(),
            years = years
        )
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

    /**
     * A small, deterministic English suffix stripper — **not** a linguist's stemmer.
     *
     * It exists so `invoice` and `invoices` are the same word, and it is applied
     * identically to the index and to the query, which is the property that matters:
     * a stemmer only has to be *consistent*, not correct, or the two sides stop
     * meeting. Non-Latin scripts are returned unchanged — the rules are English
     * suffixes, and applying them to Devanagari would corrupt real words.
     */
    internal fun stem(token: String): String {
        if (token.length <= 3) return token
        if (token.any { it.code > 0x7F }) return token
        var word = when {
            token.endsWith("'s") -> token.dropLast(2)
            token.endsWith("ies") && token.length > 4 -> token.dropLast(3) + "y"
            token.endsWith("sses") -> token.dropLast(2)
            token.endsWith("ses") || token.endsWith("xes") ||
                token.endsWith("zes") || token.endsWith("ches") ||
                token.endsWith("shes") -> token.dropLast(2)
            token.endsWith("ss") || token.endsWith("us") || token.endsWith("is") -> token
            token.endsWith("s") -> token.dropLast(1)
            else -> token
        }
        if (word.length > 5 && word.endsWith("ing")) word = word.dropLast(3)
        if (word.length > 4 && word.endsWith("ed")) word = word.dropLast(2)
        if (word.length > 5 && word.endsWith("ly")) word = word.dropLast(2)
        // "booking" → "bookk": a doubled consonant left behind by a suffix is one
        // letter too many, and dropping it is what lets "book" find "booked".
        if (word.length > 3) {
            val last = word.last()
            if (last == word[word.length - 2] && last !in "aeiou") word = word.dropLast(1)
        }
        return word
    }

    // ------------------------------------------------------- vocabulary walk

    /**
     * Runs the vocabulary walk — prefix and fuzzy matching — through Rust when it is
     * available and proven, and through [expandReference] otherwise.
     *
     * `expect` in the KDoc above is deliberate: the Kotlin reference is the
     * definition, and the native kernel is an optimisation that is verified against
     * it on the device before a single query uses it.
     */
    private fun runVocabularyWalk(
        packedVocabulary: ByteArray,
        vocabulary: Array<String>,
        queries: List<String>,
        maxDistance: Int
    ): List<Expansion> {
        // `null` means "the kernel is absent or unproven"; an empty array is a real
        // answer ("nothing matched") and must not trigger a second walk.
        val triples = NativeSearch.expand(packedVocabulary, queries, maxDistance)
            ?: return expandReference(vocabulary.asList(), queries, maxDistance)
        val rows = ArrayList<Expansion>(triples.size / 3)
        var index = 0
        while (index + 2 < triples.size) {
            rows.add(Expansion(triples[index], triples[index + 1], triples[index + 2]))
            index += 3
        }
        return rows
    }

    /**
     * The reference implementation of the vocabulary walk — and the definition of
     * what the Rust kernel must produce, byte for byte.
     *
     * Iteration order is part of the contract (**vocabulary outer, query inner**),
     * because that is the only way two implementations can be compared as arrays
     * rather than as sets.
     */
    internal fun expandReference(
        vocabulary: List<String>,
        queries: List<String>,
        maxDistance: Int
    ): List<Expansion> {
        val out = ArrayList<Expansion>()
        for (vocabularyIndex in vocabulary.indices) {
            val entry = vocabulary[vocabularyIndex]
            for (queryIndex in queries.indices) {
                val query = queries[queryIndex]
                if (query.isEmpty()) continue
                if (entry == query) {
                    out.add(Expansion(queryIndex, vocabularyIndex, 0))
                    continue
                }
                if (query.length >= PREFIX_MIN_LENGTH &&
                    entry.length > query.length &&
                    entry.startsWith(query)
                ) {
                    out.add(Expansion(queryIndex, vocabularyIndex, PREFIX_CODE))
                    continue
                }
                if (query.length > FUZZY_MAX_TERM_LENGTH) continue
                val distance = boundedEditDistance(query, entry, maxDistance)
                if (distance in 1..maxDistance) {
                    out.add(Expansion(queryIndex, vocabularyIndex, distance))
                }
            }
        }
        return out
    }

    /**
     * Levenshtein distance, capped at [maxDistance].
     *
     * Two rows, not a matrix, and it gives up as soon as every cell in a row is
     * already worse than the cap: a query that is nothing like a word should cost
     * a couple of comparisons, not the length of the word. Returns `maxDistance + 1`
     * to mean "further than that".
     */
    internal fun boundedEditDistance(a: String, b: String, maxDistance: Int): Int {
        if (maxDistance <= 0) return if (a == b) 0 else maxDistance + 1
        if (kotlin.math.abs(a.length - b.length) > maxDistance) return maxDistance + 1
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in 1..a.length) {
            current[0] = i
            var rowBest = i
            val ai = a[i - 1]
            for (j in 1..b.length) {
                val substitution = previous[j - 1] + if (ai == b[j - 1]) 0 else 1
                val deletion = previous[j] + 1
                val insertion = current[j - 1] + 1
                var best = substitution
                if (deletion < best) best = deletion
                if (insertion < best) best = insertion
                current[j] = best
                if (best < rowBest) rowBest = best
            }
            if (rowBest > maxDistance) return maxDistance + 1
            val swap = previous
            previous = current
            current = swap
        }
        val distance = previous[b.length]
        return if (distance <= maxDistance) distance else maxDistance + 1
    }

    /** Length-prefixed UTF-8, the format `NativeSearch` and `search.rs` share. */
    internal fun packStrings(words: List<String>): ByteArray {
        var size = 0
        val encoded = ArrayList<ByteArray>(words.size)
        for (word in words) {
            val bytes = word.toByteArray(Charsets.UTF_8)
            encoded.add(bytes)
            size += 4 + bytes.size
        }
        val out = ByteArray(size)
        var offset = 0
        for (bytes in encoded) {
            out[offset] = (bytes.size and 0xFF).toByte()
            out[offset + 1] = ((bytes.size ushr 8) and 0xFF).toByte()
            out[offset + 2] = ((bytes.size ushr 16) and 0xFF).toByte()
            out[offset + 3] = ((bytes.size ushr 24) and 0xFF).toByte()
            offset += 4
            bytes.copyInto(out, offset)
            offset += bytes.size
        }
        return out
    }

    /** Code the reference and the kernel both use for a prefix match. */
    internal const val PREFIX_CODE = -1

    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2100

    private const val MILLIS_PER_DAY = 86_400_000L
    private const val DAYS_PER_YEAR = 365f

    private val YEAR_PATTERN = Regex("(?<![0-9])(1[9][0-9]{2}|20[0-9]{2}|2100)(?![0-9])")
}
