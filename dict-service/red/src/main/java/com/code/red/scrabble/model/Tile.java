package com.code.red.scrabble.model;

import com.code.red.dictionary.ScrabbleScore;

public record Tile(char letter, int score, boolean blank) {

    public static Tile of(char letter) {
        char normalized = Character.toLowerCase(letter);
        boolean isBlank = normalized == '?';
        int value = isBlank ? 0 : ScrabbleScore.valueFor(normalized);
        return new Tile(normalized, value, isBlank);
    }

    public static Tile blankAs(char letter) {
        return new Tile(Character.toLowerCase(letter), 0, true);
    }
}
