package com.code.red.config;

import java.time.Clock;
import java.util.SplittableRandom;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.code.red.dictionary.DictionaryLoader;
import com.code.red.dictionary.WordDictionary;

@Configuration
@EnableConfigurationProperties(ScrabbleProperties.class)
public class DictionaryConfiguration {

    @Bean
    public WordDictionary wordDictionary(DictionaryLoader loader) {
        return loader.load();
    }

    @Bean
    public Clock scrabbleClock() {
        return Clock.systemUTC();
    }

    @Bean
    public SplittableRandom scrabbleRandom(ScrabbleProperties properties) {
        return new SplittableRandom(properties.getRandomSeed());
    }
}
