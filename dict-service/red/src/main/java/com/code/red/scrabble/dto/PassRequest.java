package com.code.red.scrabble.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PassRequest(@NotNull UUID playerId) {
}
