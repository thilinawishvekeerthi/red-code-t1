package com.code.red.scrabble.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameState {

    private final UUID id;
    private final Board board = new Board();
    private final Map<UUID, PlayerState> players = new LinkedHashMap<>();
    private final List<UUID> turnOrder;
    private final TileBag tileBag;

    private UUID currentTurn;
    private Instant lastTurnTimestamp;
    private GameStatus status = GameStatus.ACTIVE;
    private int consecutivePasses;

    public GameState(UUID id, List<PlayerState> playerStates, TileBag tileBag) {
        this.id = id;
        this.tileBag = tileBag;
        this.turnOrder = List.copyOf(playerStates.stream().map(PlayerState::getId).toList());
        for (PlayerState player : playerStates) {
            players.put(player.getId(), player);
        }
        this.currentTurn = turnOrder.get(0);
    }

    public UUID getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public TileBag getTileBag() {
        return tileBag;
    }

    public Map<UUID, PlayerState> getPlayers() {
        return players;
    }

    public List<UUID> getTurnOrder() {
        return List.copyOf(turnOrder);
    }

    public UUID getCurrentTurn() {
        return currentTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public int getConsecutivePasses() {
        return consecutivePasses;
    }

    public void incrementPass() {
        consecutivePasses++;
    }

    public void resetPasses() {
        consecutivePasses = 0;
    }

    public PlayerState requirePlayer(UUID playerId) {
        PlayerState player = players.get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Unknown player id: " + playerId);
        }
        return player;
    }

    public void advanceTurn() {
        int index = turnOrder.indexOf(currentTurn);
        int nextIndex = (index + 1) % turnOrder.size();
        currentTurn = turnOrder.get(nextIndex);
    }

    public PlayerState opponentOf(UUID playerId) {
        for (UUID id : turnOrder) {
            if (!id.equals(playerId)) {
                return players.get(id);
            }
        }
        throw new IllegalArgumentException("Opponent not found");
    }

    public Instant getLastTurnTimestamp() {
        return lastTurnTimestamp;
    }

    public void markTurnStart(Instant now) {
        this.lastTurnTimestamp = now;
    }
}

