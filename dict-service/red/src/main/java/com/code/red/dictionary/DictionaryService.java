package com.code.red.dictionary;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.code.red.config.ScrabbleProperties;

@Service
public class DictionaryService {

    private final WordDictionary dictionary;
    private final ScrabbleProperties properties;

    public DictionaryService(WordDictionary dictionary, ScrabbleProperties properties) {
        this.dictionary = dictionary;
        this.properties = properties;
    }

    public boolean exists(String word) {
        String normalized = normalizeWord(word);
        validateLetters(normalized);
        return dictionary.contains(normalized);
    }

    public List<String> prefix(String prefix, Integer limit) {
        String normalized = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        validateLetters(normalized);
        int effectiveLimit = normalizeLimit(limit, properties.getMaxPrefixResults());
        return dictionary.findByPrefix(normalized, effectiveLimit);
    }

    public List<String> anagrams(String letters, Integer limit) {
        if (!StringUtils.hasText(letters)) {
            throw new IllegalArgumentException("Letters must be provided");
        }
        String normalized = letters.toLowerCase(Locale.ROOT);
        if (normalized.length() > properties.getMaxAnagramLetters()) {
            throw new IllegalArgumentException("Too many letters for anagram search");
        }
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch == '?') {
                continue;
            }
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("Letters must be alphabetic or ?");
            }
        }
        int effectiveLimit = normalizeLimit(limit, properties.getMaxAnagramResults());
        return dictionary.findAnagrams(normalized, effectiveLimit);
    }

    public int score(String word) {
        String normalized = normalizeWord(word);
        validateLetters(normalized);
        return dictionary.score(normalized);
    }

    private static void validateLetters(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("Value must contain only letters");
            }
        }
    }

    private static String normalizeWord(String word) {
        if (!StringUtils.hasText(word)) {
            throw new IllegalArgumentException("Word must be provided");
        }
        return word.toLowerCase(Locale.ROOT);
    }

    private static int normalizeLimit(Integer limit, int max) {
        if (limit == null || limit <= 0) {
            return max;
        }
        return Math.min(limit, max);
    }
}
