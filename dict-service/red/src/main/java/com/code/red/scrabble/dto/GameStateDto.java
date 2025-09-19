package com.code.red.scrabble.dto;

import java.util.List;
import java.util.UUID;

import com.code.red.scrabble.model.GameStatus;

public record GameStateDto(UUID gameId,
        List<String> board,
        List<PlayerDto> players,
        UUID currentPlayerId,
        GameStatus status,
        int tileBagRemaining) {
}
