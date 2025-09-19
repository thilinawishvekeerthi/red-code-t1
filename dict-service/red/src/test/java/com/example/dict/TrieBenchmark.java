package com.example.dict;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class TrieBenchmark {

    private static final List<String> DEFAULT_WORDS = List.of(
            "car",
            "cart",
            "dog",
            "doom",
            "door",
            "dove",
            "cat"
    );

    private final Trie trie = new Trie();

    @Setup(Level.Trial)
    public void setUp() {
        DEFAULT_WORDS.forEach(trie::insert);
    }

    @Benchmark
    public boolean benchmarkExists() {
        return trie.exists("cart");
    }

    @Benchmark
    public List<String> benchmarkCompletions() {
        return trie.completions("do", 4);
    }

    private static final class Trie {
        private final Node root = new Node();

        void insert(String word) {
            if (word == null || word.isEmpty()) {
                return;
            }
            Node current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                current = current.children.computeIfAbsent(ch, key -> new Node());
            }
            current.terminal = true;
        }

        boolean exists(String word) {
            if (word == null || word.isEmpty()) {
                return false;
            }
            Node current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                Node next = current.children.get(ch);
                if (next == null) {
                    return false;
                }
                current = next;
            }
            return current.terminal;
        }

        List<String> completions(String prefix, int limit) {
            if (limit <= 0 || prefix == null) {
                return List.of();
            }
            Node current = root;
            for (int i = 0; i < prefix.length(); i++) {
                char ch = prefix.charAt(i);
                Node next = current.children.get(ch);
                if (next == null) {
                    return List.of();
                }
                current = next;
            }
            List<String> results = new ArrayList<>(Math.min(limit, 8));
            Deque<Entry> queue = new ArrayDeque<>();
            queue.addLast(new Entry(current, prefix));
            while (!queue.isEmpty() && results.size() < limit) {
                Entry entry = queue.removeFirst();
                if (entry.node.terminal) {
                    results.add(entry.partial);
                    if (results.size() >= limit) {
                        break;
                    }
                }
                for (Map.Entry<Character, Node> child : entry.node.children.entrySet()) {
                    queue.addLast(new Entry(child.getValue(), entry.partial + child.getKey()));
                }
            }
            return results;
        }

        private record Entry(Node node, String partial) {
        }

        private static final class Node {
            private final Map<Character, Node> children = new HashMap<>();
            private boolean terminal;
        }
    }
}

