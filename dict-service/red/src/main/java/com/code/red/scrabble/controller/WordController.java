package com.code.red.scrabble.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.code.red.dictionary.DictionaryService;
import com.code.red.scrabble.dto.WordExistsResponse;
import com.code.red.scrabble.dto.WordListResponse;
import com.code.red.scrabble.dto.WordScoreResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/words")
@Validated
public class WordController {

    private final DictionaryService dictionaryService;

    public WordController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/exists")
    public WordExistsResponse exists(@RequestParam("word") String word) {
        boolean exists = dictionaryService.exists(word);
        return new WordExistsResponse(word.toLowerCase(), exists);
    }

    @GetMapping("/prefix")
    public WordListResponse prefix(@RequestParam(value = "prefix", required = false) String prefix,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(50) Integer limit) {
        List<String> results = dictionaryService.prefix(prefix, limit);
        return new WordListResponse(results, results.size());
    }

    @GetMapping("/anagrams")
    public WordListResponse anagrams(@RequestParam("letters") String letters,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(50) Integer limit) {
        List<String> results = dictionaryService.anagrams(letters, limit);
        return new WordListResponse(results, results.size());
    }

    @GetMapping("/score")
    public WordScoreResponse score(@RequestParam("word") String word) {
        int score = dictionaryService.score(word);
        return new WordScoreResponse(word.toLowerCase(), score);
    }
}
