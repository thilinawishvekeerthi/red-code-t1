package com.code.red.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "scrabble")
public class ScrabbleProperties {

    @Min(1)
    @Max(50)
    private int maxPrefixResults = 50;

    @Min(1)
    @Max(50)
    private int maxAnagramResults = 50;

    @Min(1)
    @Max(8)
    private int maxAnagramLetters = 8;

    private long randomSeed = 12345L;

    public int getMaxPrefixResults() {
        return maxPrefixResults;
    }

    public void setMaxPrefixResults(int maxPrefixResults) {
        this.maxPrefixResults = Math.min(50, maxPrefixResults);
    }

    public int getMaxAnagramResults() {
        return maxAnagramResults;
    }

    public void setMaxAnagramResults(int maxAnagramResults) {
        this.maxAnagramResults = Math.min(50, maxAnagramResults);
    }

    public int getMaxAnagramLetters() {
        return maxAnagramLetters;
    }

    public void setMaxAnagramLetters(int maxAnagramLetters) {
        this.maxAnagramLetters = Math.min(8, maxAnagramLetters);
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }
}
