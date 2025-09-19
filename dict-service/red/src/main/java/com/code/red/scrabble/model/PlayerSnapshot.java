package com.code.red.scrabble.model;

import java.util.List;
import java.util.UUID;

public record PlayerSnapshot(UUID playerId, String name, int score, List<String> rack) {
}
