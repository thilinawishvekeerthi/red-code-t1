package com.code.red.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService;

    @Test
    void existsRejectsEmptyInput() {
        assertThatThrownBy(() -> dictionaryService.exists(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prefixRespectsLimitAndFiltersNonLetters() {
        List<String> results = dictionaryService.prefix("aa", 200);
        assertThat(results).isNotEmpty();
        assertThat(results).hasSizeLessThanOrEqualTo(50);
    }

    @Test
    void anagramsRejectTooManyLetters() {
        assertThatThrownBy(() -> dictionaryService.anagrams("abcdefghij", 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anagramsFindValidWord() {
        List<String> anagrams = dictionaryService.anagrams("alert", 5);
        assertThat(anagrams).isNotEmpty();
        assertThat(anagrams).anyMatch(word -> word.length() >= 3);
        assertThat(anagrams).contains("tear");
    }

    @Test
    void scoreMatchesScrabbleValues() {
        int score = dictionaryService.score("quiz");
        assertThat(score).isEqualTo(22);
    }
}
