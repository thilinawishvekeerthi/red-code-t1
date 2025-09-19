package com.code.red.scrabble.dto;

import java.util.UUID;

public record JoinLobbyResponse(UUID playerId, boolean waiting, UUID gameId, GameStateDto game) {
}
