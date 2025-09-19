package com.code.red.scrabble.model;

import java.util.List;
import java.util.UUID;

public record GameSnapshot(UUID gameId,
        List<String> board,
        List<PlayerSnapshot> players,
        UUID currentPlayerId,
        GameStatus status,
        int tileBagRemaining) {
}
