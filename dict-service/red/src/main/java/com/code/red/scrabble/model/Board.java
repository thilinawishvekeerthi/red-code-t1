package com.code.red.scrabble.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final int SIZE = 15;

    private final Tile[][] grid = new Tile[SIZE][SIZE];

    public Tile get(int row, int col) {
        validateBounds(row, col);
        return grid[row][col];
    }

    public boolean isEmpty(int row, int col) {
        return get(row, col) == null;
    }

    public void place(int row, int col, Tile tile) {
        validateBounds(row, col);
        grid[row][col] = tile;
    }

    public boolean hasAnyTile() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> asStringRows() {
        List<String> rows = new ArrayList<>(SIZE);
        for (int row = 0; row < SIZE; row++) {
            StringBuilder builder = new StringBuilder(SIZE);
            for (int col = 0; col < SIZE; col++) {
                Tile tile = grid[row][col];
                builder.append(tile == null ? '.' : tile.letter());
            }
            rows.add(builder.toString());
        }
        return rows;
    }

    private static void validateBounds(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("Board coordinates out of range");
        }
    }
}
