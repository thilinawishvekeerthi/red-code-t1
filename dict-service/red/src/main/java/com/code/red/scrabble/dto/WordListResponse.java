package com.code.red.scrabble.dto;

import java.util.List;

public record WordListResponse(List<String> words, int count) {
}
