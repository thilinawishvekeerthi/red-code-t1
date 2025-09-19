package com.code.red.scrabble.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.code.red.dictionary.WordDictionary;
import com.code.red.scrabble.model.Board;
import com.code.red.scrabble.model.Tile;

@Component
public class MoveValidator {

    private static final int BOARD_CENTER = 7;

    private final WordDictionary dictionary;

    public MoveValidator(WordDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public MoveValidation validate(Board board, List<Placement> placements) {
        if (placements == null || placements.isEmpty()) {
            throw new IllegalArgumentException("Placement list cannot be empty");
        }
        Map<Coordinate, Placement> placementMap = new HashMap<>();
        Set<Integer> distinctRows = new HashSet<>();
        Set<Integer> distinctCols = new HashSet<>();
        for (Placement placement : placements) {
            int row = placement.row();
            int col = placement.col();
            if (row < 0 || row >= Board.SIZE || col < 0 || col >= Board.SIZE) {
                throw new IllegalArgumentException("Placement out of bounds");
            }
            char letter = Character.toLowerCase(placement.letter());
            if (letter < 'a' || letter > 'z') {
                throw new IllegalArgumentException("Invalid placement letter");
            }
            Coordinate key = new Coordinate(row, col);
            if (placementMap.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate placement detected");
            }
            if (!board.isEmpty(row, col)) {
                throw new IllegalArgumentException("Cannot place tile over existing tile");
            }
            placementMap.put(key, new Placement(row, col, letter, placement.blank()));
            distinctRows.add(row);
            distinctCols.add(col);
        }
        boolean horizontal = distinctRows.size() == 1;
        boolean vertical = distinctCols.size() == 1;
        if (!horizontal && !vertical) {
            throw new IllegalArgumentException("Tiles must align in a straight line");
        }
        if (horizontal && vertical) {
            horizontal = placements.size() > 1;
            vertical = !horizontal;
        }
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (Placement placement : placementMap.values()) {
            minRow = Math.min(minRow, placement.row());
            maxRow = Math.max(maxRow, placement.row());
            minCol = Math.min(minCol, placement.col());
            maxCol = Math.max(maxCol, placement.col());
        }
        if (horizontal) {
            int row = placementMap.values().iterator().next().row();
            for (int col = minCol; col <= maxCol; col++) {
                if (board.isEmpty(row, col) && !placementMap.containsKey(new Coordinate(row, col))) {
                    throw new IllegalArgumentException("Gaps are not allowed in the word");
                }
            }
        } else {
            int col = placementMap.values().iterator().next().col();
            for (int row = minRow; row <= maxRow; row++) {
                if (board.isEmpty(row, col) && !placementMap.containsKey(new Coordinate(row, col))) {
                    throw new IllegalArgumentException("Gaps are not allowed in the word");
                }
            }
        }
        boolean boardEmpty = !board.hasAnyTile();
        boolean touchesExisting = boardEmpty;
        if (boardEmpty) {
            boolean includesCenter = placementMap.containsKey(new Coordinate(BOARD_CENTER, BOARD_CENTER));
            if (!includesCenter) {
                throw new IllegalArgumentException("First move must cover the center");
            }
        } else {
            for (Placement placement : placementMap.values()) {
                if (hasAdjacentTile(board, placement.row(), placement.col())) {
                    touchesExisting = true;
                    break;
                }
            }
            if (!touchesExisting) {
                throw new IllegalArgumentException("Move must connect to existing tiles");
            }
        }
        Map<Coordinate, Tile> newlyPlacedTiles = new HashMap<>();
        for (Placement placement : placementMap.values()) {
            Tile tile = placement.blank() ? Tile.blankAs(placement.letter()) : Tile.of(placement.letter());
            newlyPlacedTiles.put(new Coordinate(placement.row(), placement.col()), tile);
        }
        MoveEvaluation evaluation = evaluateWords(board, placementMap, newlyPlacedTiles, horizontal);
        return new MoveValidation(evaluation.totalScore(), evaluation.words(), newlyPlacedTiles);
    }

    private boolean hasAdjacentTile(Board board, int row, int col) {
        if (row > 0 && !board.isEmpty(row - 1, col)) {
            return true;
        }
        if (row < Board.SIZE - 1 && !board.isEmpty(row + 1, col)) {
            return true;
        }
        if (col > 0 && !board.isEmpty(row, col - 1)) {
            return true;
        }
        return col < Board.SIZE - 1 && !board.isEmpty(row, col + 1);
    }

    private MoveEvaluation evaluateWords(Board board, Map<Coordinate, Placement> placements,
            Map<Coordinate, Tile> newlyPlaced, boolean horizontal) {
        int directionRow = horizontal ? 0 : 1;
        int directionCol = horizontal ? 1 : 0;
        Coordinate reference = placements.keySet().iterator().next();
        int startRow = reference.row();
        int startCol = reference.col();
        if (horizontal) {
            startCol = placements.keySet().stream().mapToInt(Coordinate::col).min().orElse(reference.col());
        } else {
            startRow = placements.keySet().stream().mapToInt(Coordinate::row).min().orElse(reference.row());
        }
        Coordinate wordStart = extend(board, newlyPlaced, startRow, startCol, -directionRow, -directionCol);
        WordCapture mainWord = collectWord(board, newlyPlaced, wordStart, directionRow, directionCol);
        if (!dictionary.contains(mainWord.word())) {
            throw new IllegalArgumentException("Word not found in dictionary: " + mainWord.word());
        }
        int totalScore = mainWord.score();
        List<String> words = new ArrayList<>();
        words.add(mainWord.word());
        for (Coordinate coordinate : placements.keySet()) {
            WordCapture cross = collectCrossWord(board, newlyPlaced, coordinate, horizontal);
            if (cross.tiles().size() > 1) {
                if (!dictionary.contains(cross.word())) {
                    throw new IllegalArgumentException("Invalid cross word: " + cross.word());
                }
                totalScore += cross.score();
                words.add(cross.word());
            }
        }
        return new MoveEvaluation(totalScore, words);
    }

    private Coordinate extend(Board board, Map<Coordinate, Tile> newlyPlaced, int row, int col, int deltaRow,
            int deltaCol) {
        int currentRow = row;
        int currentCol = col;
        while (true) {
            int nextRow = currentRow + deltaRow;
            int nextCol = currentCol + deltaCol;
            if (nextRow < 0 || nextRow >= Board.SIZE || nextCol < 0 || nextCol >= Board.SIZE) {
                break;
            }
            Coordinate next = new Coordinate(nextRow, nextCol);
            Tile tile = newlyPlaced.get(next);
            if (tile == null && board.isEmpty(nextRow, nextCol)) {
                break;
            }
            currentRow = nextRow;
            currentCol = nextCol;
        }
        return new Coordinate(currentRow, currentCol);
    }

    private WordCapture collectWord(Board board, Map<Coordinate, Tile> newlyPlaced, Coordinate start, int deltaRow,
            int deltaCol) {
        List<Tile> tiles = new ArrayList<>();
        StringBuilder letters = new StringBuilder();
        int row = start.row();
        int col = start.col();
        while (row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE) {
            Coordinate key = new Coordinate(row, col);
            Tile tile = newlyPlaced.get(key);
            if (tile == null) {
                tile = board.get(row, col);
            }
            if (tile == null) {
                break;
            }
            tiles.add(tile);
            letters.append(tile.letter());
            row += deltaRow;
            col += deltaCol;
        }
        return new WordCapture(letters.toString(), tiles);
    }

    private WordCapture collectCrossWord(Board board, Map<Coordinate, Tile> newlyPlaced, Coordinate origin,
            boolean horizontalMove) {
        int deltaRow = horizontalMove ? 1 : 0;
        int deltaCol = horizontalMove ? 0 : 1;
        Coordinate start = extend(board, newlyPlaced, origin.row(), origin.col(), -deltaRow, -deltaCol);
        return collectWord(board, newlyPlaced, start, deltaRow, deltaCol);
    }

    public record MoveValidation(int score, List<String> words, Map<Coordinate, Tile> tiles) {
    }

    private record WordCapture(String word, List<Tile> tiles) {
        int score() {
            int total = 0;
            for (Tile tile : tiles) {
                total += tile.score();
            }
            return total;
        }
    }

    public record Coordinate(int row, int col) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Coordinate other)) {
                return false;
            }
            return row == other.row && col == other.col;
        }

        @Override
        public int hashCode() {
            return row * 31 + col;
        }
    }

    private record MoveEvaluation(int totalScore, List<String> words) {
    }
}
