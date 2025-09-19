package com.code.red.scrabble.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlacementDto(
        @Min(0) @Max(14) int row,
        @Min(0) @Max(14) int col,
        @NotBlank @Size(min = 1, max = 1) @Pattern(regexp = "[A-Za-z]", message = "Letter must be alphabetic") String letter,
        boolean blank) {
}
