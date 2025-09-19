package com.code.red.dictionary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Directed acyclic word graph (DAWG) backed dictionary.
 */
public final class DawgDictionary implements WordDictionary {

    private final DawgNode root;

    private DawgDictionary(DawgNode root) {
        this.root = root;
    }

    public static DawgDictionary fromSortedWords(List<String> words) {
        DawgBuilder builder = new DawgBuilder();
        for (String word : words) {
            builder.insert(word);
        }
        builder.finish();
        return new DawgDictionary(builder.getRoot());
    }

    @Override
    public boolean contains(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String normalized = word.toLowerCase(Locale.ROOT);
        DawgNode node = root;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            node = node.child(ch);
            if (node == null) {
                return false;
            }
        }
        return node.isTerminal();
    }

    @Override
    public boolean isPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        String normalized = prefix.toLowerCase(Locale.ROOT);
        DawgNode node = root;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            node = node.child(ch);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> findByPrefix(String prefix, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        String normalized = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        DawgNode node = root;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            node = node.child(ch);
            if (node == null) {
                return List.of();
            }
        }
        List<String> results = new ArrayList<>(Math.min(limit, 16));
        Deque<DawgTraversalState> stack = new ArrayDeque<>();
        stack.push(new DawgTraversalState(node, normalized));
        while (!stack.isEmpty() && results.size() < limit) {
            DawgTraversalState state = stack.pop();
            if (state.node.isTerminal()) {
                results.add(state.word);
                if (results.size() >= limit) {
                    break;
                }
            }
            List<Map.Entry<Character, DawgNode>> entries = state.node.childrenEntries();
            entries.sort((left, right) -> Character.compare(right.getKey(), left.getKey()));
            for (Map.Entry<Character, DawgNode> entry : entries) {
                stack.push(new DawgTraversalState(entry.getValue(), state.word + entry.getKey()));
            }
        }
        Collections.sort(results);
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    @Override
    public List<String> findAnagrams(String letters, int limit) {
        if (letters == null || letters.isEmpty() || limit <= 0) {
            return List.of();
        }
        String normalizedInput = letters.toLowerCase(Locale.ROOT);
        int[] counts = new int[27];
        for (int i = 0; i < normalizedInput.length(); i++) {
            char ch = normalizedInput.charAt(i);
            if (ch == '?') {
                counts[26] += 1;
            } else if (ch >= 'a' && ch <= 'z') {
                counts[ch - 'a'] += 1;
            }
        }
        Set<String> results = new HashSet<>();
        Deque<AnagramState> stack = new ArrayDeque<>();
        stack.push(new AnagramState("", counts, counts[26]));
        while (!stack.isEmpty() && results.size() < limit) {
            AnagramState state = stack.pop();
            if (!state.prefix().isEmpty() && contains(state.prefix())) {
                results.add(state.prefix());
                if (results.size() >= limit) {
                    break;
                }
            }
            for (int i = 0; i < 26; i++) {
                if (state.counts()[i] == 0) {
                    continue;
                }
                char letter = (char) ('a' + i);
                String nextWord = state.prefix() + letter;
                if (!isPrefix(nextWord)) {
                    continue;
                }
                int[] nextCounts = state.cloneCounts();
                nextCounts[i] -= 1;
                stack.push(new AnagramState(nextWord, nextCounts, state.blanks()));
            }
            if (state.blanks() > 0) {
                for (int i = 0; i < 26; i++) {
                    char letter = (char) ('a' + i);
                    String nextWord = state.prefix() + letter;
                    if (!isPrefix(nextWord)) {
                        continue;
                    }
                    stack.push(new AnagramState(nextWord, state.cloneCounts(), state.blanks() - 1));
                }
            }
        }
        List<String> ordered = new ArrayList<>(results);
        Collections.sort(ordered);
        return ordered.size() > limit ? ordered.subList(0, limit) : ordered;
    }

    @Override
    public int score(String word) {
        if (word == null || word.isEmpty()) {
            return 0;
        }
        String normalized = word.toLowerCase(Locale.ROOT);
        int score = 0;
        for (int i = 0; i < normalized.length(); i++) {
            score += ScrabbleScore.valueFor(normalized.charAt(i));
        }
        return score;
    }

    private record DawgTraversalState(DawgNode node, String word) {
    }

    private record AnagramState(String prefix, int[] counts, int blanks) {
        int[] cloneCounts() {
            int[] copy = new int[counts.length];
            System.arraycopy(counts, 0, copy, 0, counts.length);
            return copy;
        }
    }

    private static final class DawgBuilder {

        private final DawgNode root = new DawgNode();
        private final Map<DawgNode, DawgNode> registry = new HashMap<>();
        private String previousWord = "";
        private final List<DawgNode> previousNodes = new ArrayList<>();

        private DawgBuilder() {
            previousNodes.add(root);
        }

        void insert(String word) {
            if (previousWord.compareTo(word) > 0) {
                throw new IllegalArgumentException("Words must be inserted in lexicographical order");
            }
            int commonPrefix = commonPrefixLength(previousWord, word);
            replaceOrRegister(commonPrefix);

            DawgNode node = previousNodes.get(commonPrefix);
            for (int i = commonPrefix; i < word.length(); i++) {
                char letter = word.charAt(i);
                DawgNode next = new DawgNode();
                node.putChild(letter, next);
                node = next;
                previousNodes.add(node);
            }
            node.setTerminal(true);
            previousWord = word;
        }

        void finish() {
            replaceOrRegister(0);
        }

        DawgNode getRoot() {
            return root;
        }

        private void replaceOrRegister(int index) {
            for (int i = previousNodes.size() - 1; i > index; i--) {
                DawgNode node = previousNodes.get(i);
                DawgNode registered = registry.get(node);
                if (registered == null) {
                    registry.put(node, node);
                } else {
                    DawgNode parent = previousNodes.get(i - 1);
                    char letter = previousWord.charAt(i - 1);
                    parent.putChild(letter, registered);
                    previousNodes.set(i, registered);
                }
                previousNodes.remove(i);
            }
        }

        private static int commonPrefixLength(String a, String b) {
            int limit = Math.min(a.length(), b.length());
            for (int i = 0; i < limit; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    return i;
                }
            }
            return limit;
        }
    }

    private static final class DawgNode {
        private final Map<Character, DawgNode> children = new HashMap<>();
        private boolean terminal;

        DawgNode child(char letter) {
            return children.get(letter);
        }

        void putChild(char letter, DawgNode node) {
            children.put(letter, node);
        }

        List<Map.Entry<Character, DawgNode>> childrenEntries() {
            return new ArrayList<>(children.entrySet());
        }

        boolean isTerminal() {
            return terminal;
        }

        void setTerminal(boolean terminal) {
            this.terminal = terminal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DawgNode other)) {
                return false;
            }
            return terminal == other.terminal && Objects.equals(children, other.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(children, terminal);
        }
    }
}
