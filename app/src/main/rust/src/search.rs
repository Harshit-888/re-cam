//! Vocabulary matcher for Magic Search.
//!
//! # The one job
//!
//! Given the index's vocabulary and the words the user typed, say which
//! vocabulary entries each word matches — exactly, as a prefix, or within a
//! typo — and how close each match is. Kotlin's `DocumentSearch.expandReference`
//! is the definition of the answer; this is the same answer, computed where the
//! loop is cheap.
//!
//! # Why it is a pure function
//!
//! No globals, no cached state, no lifetime shared with the JVM. The inputs are two
//! byte arrays and a distance cap; the output is a list of triples. That is what
//! makes it possible to *prove* it against the Kotlin implementation on the device
//! before a single query uses it (`NativeSearch.selfTest`) — the same discipline
//! the pixel kernels follow.
//!
//! Everything here is `std`-only and allocation-light: one `Vec<u32>` per call and
//! no per-comparison allocation.

/// Below this many characters a prefix would match half the dictionary.
pub const PREFIX_MIN_LENGTH: usize = 3;

/// Longer than this and the input is a paste, not a word; typo matching is skipped.
pub const FUZZY_MAX_TERM_LENGTH: usize = 24;

/// Exact match.
pub const CODE_EXACT: i32 = 0;

/// Prefix match.
pub const CODE_PREFIX: i32 = -1;

/// Splits the length-prefixed UTF-8 format produced by `DocumentSearch.packStrings`.
///
/// Each entry is a little-endian `u32` byte length followed by that many UTF-8
/// bytes. Returns `None` for anything malformed rather than guessing — a truncated
/// buffer must fail the parity check, not produce plausible garbage.
pub fn parse_packed(bytes: &[u8]) -> Option<Vec<&str>> {
    let mut out = Vec::new();
    let mut offset = 0usize;
    while offset + 4 <= bytes.len() {
        let len = u32::from_le_bytes([
            bytes[offset],
            bytes[offset + 1],
            bytes[offset + 2],
            bytes[offset + 3],
        ]) as usize;
        offset += 4;
        let end = offset.checked_add(len)?;
        if end > bytes.len() {
            return None;
        }
        out.push(std::str::from_utf8(&bytes[offset..end]).ok()?);
        offset = end;
    }
    if offset != bytes.len() {
        return None;
    }
    Some(out)
}

/// Levenshtein distance capped at `max_distance`.
///
/// Two rows instead of a matrix, and it gives up as soon as every cell in a row is
/// already worse than the cap: a word that is nothing like the query should cost a
/// few comparisons, not the length of the word. Returns `max_distance + 1` to mean
/// "further than that", exactly like the Kotlin reference.
///
/// Generic over the symbol type so one implementation serves both cases: `char`
/// for text (a two-byte codepoint is *one* edit — comparing bytes would report a
/// Devanagari word as being two edits from itself) and `u8` for ASCII, where bytes
/// and characters are the same thing and no `Vec<char>` has to be allocated.
pub fn bounded_edit_distance<T: PartialEq>(a: &[T], b: &[T], max_distance: usize) -> usize {
    let over = max_distance + 1;
    if a.len().abs_diff(b.len()) > max_distance {
        return over;
    }
    let n = b.len();
    let mut previous: Vec<usize> = (0..=n).collect();
    let mut current = vec![0usize; n + 1];
    for i in 1..=a.len() {
        current[0] = i;
        let mut row_best = i;
        let ai = a[i - 1];
        for j in 1..=n {
            let cost = usize::from(ai != b[j - 1]);
            let substitution = previous[j - 1] + cost;
            let deletion = previous[j] + 1;
            let insertion = current[j - 1] + 1;
            let best = substitution.min(deletion).min(insertion);
            current[j] = best;
            if best < row_best {
                row_best = best;
            }
        }
        if row_best > max_distance {
            return over;
        }
        std::mem::swap(&mut previous, &mut current);
    }
    let distance = previous[n];
    if distance <= max_distance {
        distance
    } else {
        over
    }
}

/// The whole walk: flattened `(query_index, vocab_index, code)` triples.
///
/// **Order is part of the contract** — vocabulary outer, query inner — because the
/// Kotlin reference and this must be comparable as arrays. An entry matches at most
/// one code per query, in priority order: exact, then prefix, then distance.
///
/// Two cheap filters run before any distance is computed, and both are exact:
///
/// * ASCII pairs compare lengths in bytes, which *is* the character-count test;
/// * otherwise a byte-length gap wider than four bytes per allowed edit cannot
///   possibly be within the cap, because no character is longer than four bytes.
///
/// What this buys is the absence of an allocation for the common case: an ASCII
/// query walks an ASCII vocabulary comparing byte slices, with no `Vec<char>`
/// anywhere. The Kotlin reference allocates nothing either, so the two still agree.
pub fn expand(vocabulary: &[&str], queries: &[&str], max_distance: usize) -> Vec<i32> {
    // Decoded only for the non-ASCII pairs that reach the distance computation.
    let query_chars: Vec<Vec<char>> = queries.iter().map(|q| q.chars().collect()).collect();
    let query_ascii: Vec<bool> = queries.iter().map(|q| q.is_ascii()).collect();
    let mut out: Vec<i32> = Vec::new();

    for (vocabulary_index, entry) in vocabulary.iter().enumerate() {
        let entry_ascii = entry.is_ascii();
        for (query_index, query) in queries.iter().enumerate() {
            if query.is_empty() {
                continue;
            }
            if entry == query {
                push(&mut out, query_index, vocabulary_index, CODE_EXACT);
                continue;
            }
            let query_len = query_chars[query_index].len();
            // A byte-longer entry that starts with the query has at least one more
            // character, so this is the same test as the reference's char count.
            if query_len >= PREFIX_MIN_LENGTH
                && entry.len() > query.len()
                && entry.starts_with(query.as_str())
            {
                push(&mut out, query_index, vocabulary_index, CODE_PREFIX);
                continue;
            }
            if query_len > FUZZY_MAX_TERM_LENGTH {
                continue;
            }

            let distance = if entry_ascii && query_ascii[query_index] {
                if entry.len().abs_diff(query.len()) > max_distance {
                    continue;
                }
                bounded_edit_distance(entry.as_bytes(), query.as_bytes(), max_distance)
            } else {
                // One character is at most four bytes, so a wider byte gap than
                // `4 * max_distance` cannot hide a character gap within the cap.
                if entry.len().abs_diff(query.len()) > 4 * max_distance {
                    continue;
                }
                let entry_chars: Vec<char> = entry.chars().collect();
                bounded_edit_distance(&entry_chars, &query_chars[query_index], max_distance)
            };

            if distance >= 1 && distance <= max_distance {
                push(&mut out, query_index, vocabulary_index, distance as i32);
            }
        }
    }
    out
}

fn push(out: &mut Vec<i32>, query_index: usize, vocabulary_index: usize, code: i32) {
    out.push(query_index as i32);
    out.push(vocabulary_index as i32);
    out.push(code);
}

#[cfg(test)]
mod tests {
    use super::*;

    fn strings(list: &[&'static str]) -> Vec<&'static str> {
        list.to_vec()
    }

    #[test]
    fn packed_format_round_trips() {
        let packed = {
            let mut out = Vec::new();
            for word in ["invoice", "कर"] {
                let bytes = word.as_bytes();
                out.extend_from_slice(&(bytes.len() as u32).to_le_bytes());
                out.extend_from_slice(bytes);
            }
            out
        };
        assert_eq!(parse_packed(&packed), Some(strings(&["invoice", "कर"])));
        assert_eq!(parse_packed(&[]), Some(Vec::new()));
        // A truncated buffer is an error, not an empty vocabulary.
        assert_eq!(parse_packed(&packed[..packed.len() - 1]), None);
        assert_eq!(parse_packed(&[9, 0, 0, 0]), None);
    }

    #[test]
    fn matches_the_golden_expansion() {
        let vocabulary = strings(&[
            "aadhaar", "aadhar", "bill", "billing", "birth", "card", "date", "dob",
            "invoice", "invoices", "ledger", "pan", "pancard", "receipt", "receipts",
            "return", "tax", "total",
        ]);
        let queries = strings(&["invoce", "invoi", "aadhar", "dob", "totl"]);
        assert_eq!(
            expand(&vocabulary, &queries, 2),
            vec![
                (2, 0, 1), (2, 1, 0), (3, 7, 0), (0, 8, 1), (1, 8, -1),
                (0, 9, 2), (1, 9, -1), (4, 17, 1),
            ]
            .into_iter()
            .flat_map(|(q, v, c)| [q, v, c])
            .collect::<Vec<i32>>()
        );
    }

    #[test]
    fn no_match_is_no_match() {
        let vocabulary = strings(&["invoice", "tax"]);
        let queries = strings(&["passport"]);
        assert!(expand(&vocabulary, &queries, 2).is_empty());
    }

    #[test]
    fn ascii_and_char_forms_agree() {
        // The byte fast path must be indistinguishable from the character path.
        assert_eq!(
            bounded_edit_distance(b"invoce", b"invoice", 2),
            bounded_edit_distance(
                &"invoce".chars().collect::<Vec<char>>(),
                &"invoice".chars().collect::<Vec<char>>(),
                2
            )
        );
        assert_eq!(bounded_edit_distance(b"totl", b"total", 2), 1);
        assert_eq!(bounded_edit_distance(b"dob", b"date", 2), 3);
    }

    #[test]
    fn distance_is_capped_and_char_aware() {
        let a: Vec<char> = "aadhar".chars().collect();
        let b: Vec<char> = "aadhaar".chars().collect();
        assert_eq!(bounded_edit_distance(&a, &b, 2), 1);
        let c: Vec<char> = "dob".chars().collect();
        let d: Vec<char> = "date".chars().collect();
        assert_eq!(bounded_edit_distance(&c, &d, 2), 3);
        let e: Vec<char> = "कर".chars().collect();
        assert_eq!(bounded_edit_distance(&e, &e, 2), 0);
        // Two bytes per character: 2 chars in, 2 chars out, not 6 bytes.
        assert_eq!(e.len(), 2);
    }

    #[test]
    fn prefix_needs_three_characters() {
        let vocabulary = strings(&["tax", "taxation"]);
        let queries = strings(&["ta"]);
        assert!(expand(&vocabulary, &queries, 0).is_empty());
        let queries = strings(&["tax"]);
        // "tax" is exact for one entry and a prefix of the other.
        assert_eq!(expand(&vocabulary, &queries, 0), vec![0, 0, 0, 0, 1, -1]);
    }
}
