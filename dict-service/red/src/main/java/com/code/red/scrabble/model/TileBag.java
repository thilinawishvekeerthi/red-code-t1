package com.code.red.scrabble.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class TileBag {

    private static final Map<Character, Integer> DISTRIBUTION = Map.ofEntries(
            Map.entry('a', 9),
            Map.entry('b', 2),
            Map.entry('c', 2),
            Map.entry('d', 4),
            Map.entry('e', 12),
            Map.entry('f', 2),
            Map.entry('g', 3),
            Map.entry('h', 2),
            Map.entry('i', 9),
            Map.entry('j', 1),
            Map.entry('k', 1),
            Map.entry('l', 4),
            Map.entry('m', 2),
            Map.entry('n', 6),
            Map.entry('o', 8),
            Map.entry('p', 2),
            Map.entry('q', 1),
            Map.entry('r', 6),
            Map.entry('s', 4),
            Map.entry('t', 6),
            Map.entry('u', 4),
            Map.entry('v', 2),
            Map.entry('w', 2),
            Map.entry('x', 1),
            Map.entry('y', 2),
            Map.entry('z', 1),
            Map.entry('?', 2));

    private final List<Tile> tiles;
    private final SplittableRandom random;

    public TileBag(SplittableRandom random) {
        this.random = random;
        this.tiles = new ArrayList<>(100);
        populate();
        shuffle();
    }

    public List<Tile> draw(int count) {
        int actual = Math.min(count, tiles.size());
        List<Tile> drawn = new ArrayList<>(actual);
        for (int i = 0; i < actual; i++) {
            drawn.add(tiles.remove(tiles.size() - 1));
        }
        return drawn;
    }

    public void returnTiles(List<Tile> returned) {
        if (returned == null || returned.isEmpty()) {
            return;
        }
        tiles.addAll(returned);
        shuffle();
    }

    public int remaining() {
        return tiles.size();
    }

    private void populate() {
        for (Map.Entry<Character, Integer> entry : DISTRIBUTION.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                tiles.add(Tile.of(entry.getKey()));
            }
        }
    }

    private void shuffle() {
        Collections.shuffle(tiles, new java.util.Random(random.nextLong()));
    }
}
