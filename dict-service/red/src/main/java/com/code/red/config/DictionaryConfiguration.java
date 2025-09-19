package com.code.red.config;

import java.util.SplittableRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
    public SplittableRandom scrabbleRandom(ScrabbleProperties properties) {
        return new SplittableRandom(properties.getRandomSeed());
    }
}
