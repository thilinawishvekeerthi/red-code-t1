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


Implement GameUtils.anagrams(letters: String, dict: Dictionary, limit: int):
- Normalize input (uppercase A–Z only).
- Generate candidate permutations/combinations.
- Filter with dict.exists().
- Return unique words, lexicographically sorted.
- Limit max results by 'limit' (default 50).
Optimize:
- Use backtracking with pruning.
- Use TrieDictionary prefix check to prune search space.
Add unit tests:
- letters="act" → ["act","cat"]
- letters="dog" → ["dog","god"]

Implement GameUtils.score(word: String): int
- Scrabble letter values:
  A,E,I,O,U,L,N,S,T,R=1
  D,G=2
  B,C,M,P=3
  F,H,V,W,Y=4
  K=5
  J,X=8
  Q,Z=10
- Return sum of values; ignore case.
- Return 0 for invalid/non-alpha input.
Add unit tests:
- score("cat")=5
- score("quiz")=22
- score("zzz")=30
