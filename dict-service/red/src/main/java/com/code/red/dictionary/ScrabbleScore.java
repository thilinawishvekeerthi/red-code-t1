package com.code.red.dictionary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Scrabble letter scores.
 */
public final class ScrabbleScore {

    private static final Map<Character, Integer> LETTER_SCORES;

    static {
        Map<Character, Integer> scores = new HashMap<>();
        scores.put('a', 1);
        scores.put('b', 3);
        scores.put('c', 3);
        scores.put('d', 2);
        scores.put('e', 1);
        scores.put('f', 4);
        scores.put('g', 2);
        scores.put('h', 4);
        scores.put('i', 1);
        scores.put('j', 8);
        scores.put('k', 5);
        scores.put('l', 1);
        scores.put('m', 3);
        scores.put('n', 1);
        scores.put('o', 1);
        scores.put('p', 3);
        scores.put('q', 10);
        scores.put('r', 1);
        scores.put('s', 1);
        scores.put('t', 1);
        scores.put('u', 1);
        scores.put('v', 4);
        scores.put('w', 4);
        scores.put('x', 8);
        scores.put('y', 4);
        scores.put('z', 10);
        scores.put('?', 0); // blank tile
        LETTER_SCORES = Collections.unmodifiableMap(scores);
    }

    private ScrabbleScore() {
    }

    public static int valueFor(char letter) {
        return LETTER_SCORES.getOrDefault(Character.toLowerCase(letter), 0);
    }
}
