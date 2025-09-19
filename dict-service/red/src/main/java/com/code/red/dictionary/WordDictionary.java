package com.code.red.dictionary;

import java.util.List;

/**
 * Strategy interface for dictionary implementations.
 */
public interface WordDictionary {

    boolean contains(String word);

    boolean isPrefix(String prefix);

    List<String> findByPrefix(String prefix, int limit);

    List<String> findAnagrams(String letters, int limit);

    int score(String word);
}
