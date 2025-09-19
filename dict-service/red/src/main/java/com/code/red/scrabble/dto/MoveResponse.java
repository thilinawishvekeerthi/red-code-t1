package com.code.red.scrabble.dto;

import java.util.List;

public record MoveResponse(GameStateDto game, int scoreEarned, List<String> wordsFormed) {
}
