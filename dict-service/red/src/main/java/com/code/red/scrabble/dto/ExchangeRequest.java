package com.code.red.scrabble.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExchangeRequest(
        @NotNull UUID playerId,
        @NotEmpty @Size(max = 7) List<@Pattern(regexp = "[A-Za-z?]", message = "Letters must be alphabetic or ? for blank") String> letters) {
}
