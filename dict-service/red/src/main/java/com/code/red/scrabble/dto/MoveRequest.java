package com.code.red.scrabble.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoveRequest(
        @NotNull UUID playerId,
        @NotEmpty @Size(max = 7) List<@Valid PlacementDto> placements) {
}
