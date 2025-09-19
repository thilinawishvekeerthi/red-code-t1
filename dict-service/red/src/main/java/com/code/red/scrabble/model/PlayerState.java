package com.code.red.scrabble.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerState {

    private final UUID id;
    private final String name;
    private final List<Tile> rack = new ArrayList<>(7);
    private int score;

    public PlayerState(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    public List<Tile> rackView() {
        return Collections.unmodifiableList(rack);
    }

    public void refillRack(TileBag bag) {
        List<Tile> needed = bag.draw(7 - rack.size());
        rack.addAll(needed);
    }

    public Tile removeTile(char letter, boolean useBlank) {
        char normalized = Character.toLowerCase(letter);
        if (useBlank) {
            int index = indexOfBlank();
            if (index < 0) {
                throw new IllegalArgumentException("Player does not have a blank tile");
            }
            return rack.remove(index);
        }
        int index = indexOfLetter(normalized);
        if (index < 0) {
            throw new IllegalArgumentException("Player rack missing letter: " + normalized);
        }
        return rack.remove(index);
    }

    public void returnTiles(List<Tile> tiles) {
        rack.addAll(tiles);
    }

    private int indexOfLetter(char letter) {
        for (int i = 0; i < rack.size(); i++) {
            Tile tile = rack.get(i);
            if (!tile.blank() && tile.letter() == letter) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfBlank() {
        for (int i = 0; i < rack.size(); i++) {
            Tile tile = rack.get(i);
            if (tile.blank()) {
                return i;
            }
        }
        return -1;
    }
}
