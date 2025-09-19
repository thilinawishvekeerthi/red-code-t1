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
