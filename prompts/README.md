## Scaffold Project
You are an expert Spring Boot engineer. Use the rules in AGENTS.md.
Task: In the existing Maven Spring Boot app, create a REST microservice "Dictionary Service".

Deliverables:
1) API:
   - GET /api/words/exists?word=abc -> {word, exists}
   - GET /api/words/prefix?q=ab&limit=20 -> ["ab", ...]
   - (Optional if dynamic) POST /api/words {word:"..."} -> 201 or 409
2) Layers:
   - controller: WordController
   - service: DictionaryService
   - strategy interface: Dictionary { load(InputStream), exists(word), suggest(prefix, limit) }
   - impls: TrieDictionary, DawgDictionary
   - DictionaryFactory selects impl via property app.dictionary=trie|dawg.
3) Bootstrap:
   - On startup, if app.dictionary.dataFile is set (UTF-8 word-per-line), load it.
   - Words normalized (trim, lowercase), deduped.
4) Observability:
   - Add springdoc-openapi UI; expose actuator/health.
5) Validation:
   - limit max 100; q/word length 1..64; reject invalid with 400.
6) Tests:
   - Controller tests for exists/prefix.
   - Unit tests for Dictionary impls with small sample corpus.
7) README edits:
   - Run commands with Maven Wrapper, and property examples.

## Implement TrieDictionary
Implement TrieDictionary (no recursion; iterative).
- Node fields optimized for memory, e.g., children Map<Character,Node> or fixed array for 'a'..'z'.
- Methods:
  - load(InputStream): stream lines, normalize, insert
  - exists(word): O(m)
  - suggest(prefix, limit): traverse to node, then bounded DFS/BFS collecting at most 'limit'
- Edge cases: empty lines; non-alpha characters (keep but exact-match compare).
- Include unit tests covering typical and edge cases.

## Implement DAWG/DAFSA (incremental, minimal)
Implement DawgDictionary using incremental DAFSA construction for a STATIC sorted list.
Algorithm outline:
- Sort unique words lexicographically (assume input file already sorted or sort in memory).
- Maintain register map<StateSignature, State> of minimized states.
- For each word:
  - Compute longest common prefix (LCP) with previous word.
  - Minimize suffix of previous word beyond LCP.
  - Add new suffix for current word.
- After all words, minimize the remaining suffix.
- suggest(): traverse prefix; bounded DFS.
- exists(): deterministic walk.
Provide unit tests comparing Trie vs Dawg for same dataset.

## Tests & Quick Perf
Add tests:
- exists(): true for present, false for absent; case normalization checks.
- suggest(): returns <= limit; empty result for missing prefix; stable lexicographic order.
- Compare Trie and Dawg outputs for same corpus.
Quick perf harness:
- Load 50–100k word list.
- Time suggest("pre",20) runs.
- Print rough heap use after load.

## README generator
Update README.md with:
- Service description.
- How to run:
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.dictionary=trie --app.dictionary.dataFile=./data/wordlists/words.txt"
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.dictionary=dawg --app.dictionary.dataFile=./data/wordlists/words.txt"
- Swagger URL and curl examples.
- Trade-offs: Trie vs DAWG.
- Known limits & future work.
