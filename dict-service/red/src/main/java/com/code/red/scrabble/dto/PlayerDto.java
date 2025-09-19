package com.code.red.scrabble.dto;

import java.util.List;
import java.util.UUID;

public record PlayerDto(UUID playerId, String name, int score, List<String> rack) {
}
