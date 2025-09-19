package com.code.red.scrabble.support;

import java.util.concurrent.locks.ReentrantLock;

import com.code.red.scrabble.model.GameState;

public class GameSession {

    private final GameState game;
    private final ReentrantLock lock = new ReentrantLock();

    public GameSession(GameState game) {
        this.game = game;
    }

    public GameState getGame() {
        return game;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
