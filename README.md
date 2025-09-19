# Dictionary Service

## Swagger / OpenAPI
- Swagger UI: http://localhost:8080/swagger-ui.html

## Benchmarks
Run the JMH suite after packaging:
```bash
./mvnw clean install
java -jar target/benchmarks.jar'
```
# Scrabble Dictionary Service

## Features
- Word validity check (exists)
- Prefix search (autocomplete)
- Anagram solver (from letter tiles)
- Scrabble scoring

## Run
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.dictionary=trie --app.dictionary.dataFile=./data/wordlists/words.txt"

Swagger: http://localhost:8080/swagger-ui.html  
Health:  http://localhost:8080/actuator/health  

## API Examples
curl "http://localhost:8080/api/words/exists?word=cat"
curl "http://localhost:8080/api/words/prefix?q=ca&limit=5"
curl "http://localhost:8080/api/words/anagrams?letters=act"
curl "http://localhost:8080/api/words/score?word=quiz"

## Notes
- Trie for dynamic inserts; DAWG for memory-optimized static sets.
- Anagrams pruned by dictionary prefix lookups.
- Scoring based on Scrabble letter values.
- Input validation: only [A–Z], max 8 letters, result limit 50.
