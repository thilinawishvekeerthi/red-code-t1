Coding rules:
- Architecture: Controller → Service → Dictionary (Strategy pattern). No repo/DB for now.
- DTOs only; no entities. Validation on inputs. Fail fast with 400s. 
- Performance: O(m) for lookup/suggest; no recursion for long strings; iterative traversals.
- Memory: Keep nodes compact; use arrays or maps carefully; prefer DAWG for static corpora.
- Security: Validate/normalize user input; limit=100 max; timeouts; no unsafe deserialization.
- Observability: Swagger/OpenAPI, actuator health, basic logs.
- Testing: JUnit tests for edge cases (empty, non-ascii, very long, high fan-out prefixes).
- Documentation: README with run commands and trade-off notes (Trie vs DAWG).
- Style: Clean code, small methods, meaningful names, comments only where non-obvious.

# Extra Rules for Word Game Challenge
- Word APIs must support:
  - exists() check
  - prefix search
  - anagrams (letters → valid words)
  - score (Scrabble letter values)
- Inputs:
  - Normalize to lowercase internally.
  - Reject invalid chars with 400.
  - Limit anagram letters to max 8.
  - Limit prefix/anagram results to max 50.
- Performance:
  - Use Trie/DAWG for dictionary lookups.
  - Use prefix pruning in anagram solver.
- Testing:
  - Add unit tests for anagrams and scoring.
  - Edge cases: empty input, invalid chars, too many letters.

