package com.code.red.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class DictionaryLoader {

    private static final Logger log = LoggerFactory.getLogger(DictionaryLoader.class);

    private final Resource dictionaryResource;

    public DictionaryLoader(@Value("${scrabble.dictionary-path:classpath:CSW24.txt}") Resource dictionaryResource) {
        this.dictionaryResource = dictionaryResource;
    }

    public DawgDictionary load() {
        List<String> words = new ArrayList<>(256_000);
        try (InputStream inputStream = dictionaryResource.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String word = extractWord(line);
                if (!word.isEmpty()) {
                    words.add(word.toLowerCase(Locale.ROOT));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dictionary", e);
        }
        Collections.sort(words);
        log.info("Loaded {} words into DAWG dictionary", words.size());
        return DawgDictionary.fromSortedWords(words);
    }

    private static String extractWord(String line) {
        int index = 0;
        while (index < line.length() && !Character.isWhitespace(line.charAt(index))) {
            index++;
        }
        return line.substring(0, index);
    }
}
