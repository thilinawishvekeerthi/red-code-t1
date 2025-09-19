# Scrabble Service

Spring Boot service for two-player Scrabble matches with dictionary-backed validation and word utilities.

## Run
- `mvn spring-boot:run` (from `dict-service/red`)
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator health: http://localhost:8080/actuator/health

## Test
- `mvn test`
- Integration coverage: lobby matchmaking, move submission, dictionary edge cases (invalid input, high fan-out prefixes, anagram solver limits).

## Features
- Lobby matchmaking: `POST /lobby/join`, `GET /lobby/{playerId}`
- Game lifecycle: state, move, exchange, pass endpoints under `/game/{id}`
- Word APIs: exists, prefix search, anagrams with prefix pruning, Scrabble scoring (`/words/*`)
- Input validation via Jakarta Validation and global error handler
- Deterministic tile bag seeded via `scrabble.random-seed`
- WebSockets: STOMP endpoint at `/ws` (SockJS optional). Subscribe to `/topic/game/{gameId}` for live `GameStateDto` payloads after joins/moves/exchanges/passes; REST remains the command channel.
- 10-minute per-player game clock with automatic timeout when it reaches zero

## Architecture
- Controller ? Service ? Dictionary (strategy) layers
- `DawgDictionary` loads CSW24 corpus once on startup (O(m) lookups, iterative traversals)
- `ScrabbleService` manages in-memory games with per-session locks and tile-bag operations
- `MoveValidator` enforces board constraints, cross-word checks, and scoring using dictionary strategy

## Dictionary Trade-offs (Trie vs DAWG)
- **Trie**: simpler build, but high node count (~280k words) leads to large heap usage and slower startup; prefix queries remain O(m) yet memory locality suffers
- **DAWG (chosen)**: minimises duplicate suffixes for static corpora, reducing memory footprint; build cost paid at bootstrap, but resulting graph enables fast prefix walks and anagram pruning with iterative traversal
- Blank tiles stored as zero-score nodes; cross-word scoring reuses board tiles to avoid recounting premiums

## Notes
- Requests reject non-latin letters, anagram letters capped at 8, result limits clamped to 50
- Timeouts configured via `spring.mvc.async.request-timeout`; logging scoped under `com.code.red`
