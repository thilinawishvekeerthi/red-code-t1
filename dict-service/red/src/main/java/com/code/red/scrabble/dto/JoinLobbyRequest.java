package com.code.red.scrabble.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JoinLobbyRequest(
        @NotBlank
        @Size(min = 1, max = 32)
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Player name must be alphanumeric or underscore")
        String playerName) {
}
