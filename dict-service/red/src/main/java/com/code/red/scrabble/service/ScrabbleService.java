package com.code.red.scrabble.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.code.red.scrabble.model.GameSnapshot;
import com.code.red.scrabble.model.GameState;
import com.code.red.scrabble.model.GameStatus;
import com.code.red.scrabble.model.PlayerSnapshot;
import com.code.red.scrabble.model.PlayerState;
import com.code.red.scrabble.model.Tile;
import com.code.red.scrabble.model.TileBag;
import com.code.red.scrabble.support.GameNotifier;
import com.code.red.scrabble.support.GameSession;

@Service
public class ScrabbleService {

    private static final Logger log = LoggerFactory.getLogger(ScrabbleService.class);

    private final MoveValidator moveValidator;
    private final SplittableRandom random;
    private final Clock clock;
    private final GameNotifier gameNotifier;

    private final Lobby lobby = new Lobby();
    private final ConcurrentMap<UUID, GameSession> games = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, UUID> playerToGame = new ConcurrentHashMap<>();

    public ScrabbleService(MoveValidator moveValidator, SplittableRandom random, Clock clock, GameNotifier gameNotifier) {
        this.moveValidator = moveValidator;
        this.random = random;
        this.clock = clock;
        this.gameNotifier = gameNotifier;
    }

    public JoinResult joinLobby(String playerName) {
        Objects.requireNonNull(playerName, "playerName");
        Lobby.JoinOutcome outcome = lobby.addPlayer(playerName);
        if (outcome.gameCreated() != null) {
            registerGame(outcome.gameCreated());
            GameSnapshot snapshot = snapshot(outcome.gameCreated());
            gameNotifier.notifyGame(snapshot);
            return new JoinResult(outcome.playerId(), outcome.gameCreated().getId(), snapshot, false);
        }
        return new JoinResult(outcome.playerId(), null, null, true);
    }

    public Optional<UUID> gameForPlayer(UUID playerId) {
        return Optional.ofNullable(playerToGame.get(playerId));
    }

    public GameSnapshot getGame(UUID gameId) {
        GameSession session = requireSession(gameId);
        session.getLock().lock();
        try {
            GameState game = session.getGame();
            boolean timedOut = updateClock(game);
            GameSnapshot snapshot = snapshot(game);
            if (timedOut) {
                gameNotifier.notifyGame(snapshot);
            }
            return snapshot;
        } finally {
            session.getLock().unlock();
        }
    }

    public MoveResult playMove(UUID gameId, UUID playerId, List<Placement> placements) {
        GameSession session = requireSession(gameId);
        session.getLock().lock();
        try {
            GameState game = session.getGame();
            if (updateClock(game)) {
                GameSnapshot snapshot = snapshot(game);
                gameNotifier.notifyGame(snapshot);
                throw new IllegalStateException("Player timed out");
            }
            ensureActive(game);
            if (!game.getCurrentTurn().equals(playerId)) {
                throw new IllegalArgumentException("It is not the player's turn");
            }
            PlayerState player = game.requirePlayer(playerId);
            MoveValidator.MoveValidation validation = moveValidator.validate(game.getBoard(), placements);
            for (Placement placement : placements) {
                player.removeTile(placement.letter(), placement.blank());
            }
            for (Map.Entry<MoveValidator.Coordinate, Tile> entry : validation.tiles().entrySet()) {
                MoveValidator.Coordinate coordinate = entry.getKey();
                game.getBoard().place(coordinate.row(), coordinate.col(), entry.getValue());
            }
            player.addScore(validation.score());
            game.resetPasses();
            player.refillRack(game.getTileBag());
            handlePostMove(game, player);
            if (game.getStatus() == GameStatus.ACTIVE) {
                game.advanceTurn();
                game.markTurnStart(clock.instant());
            }
            GameSnapshot snapshot = snapshot(game);
            gameNotifier.notifyGame(snapshot);
            log.debug("Player {} played words {} for {} points", player.getName(), validation.words(),
                    validation.score());
            return new MoveResult(snapshot, validation.score(), validation.words());
        } finally {
            session.getLock().unlock();
        }
    }

    public GameSnapshot exchangeTiles(UUID gameId, UUID playerId, List<Character> letters) {
        GameSession session = requireSession(gameId);
        session.getLock().lock();
        try {
            GameState game = session.getGame();
            if (updateClock(game)) {
                GameSnapshot snapshot = snapshot(game);
                gameNotifier.notifyGame(snapshot);
                throw new IllegalStateException("Player timed out");
            }
            ensureActive(game);
            if (!game.getCurrentTurn().equals(playerId)) {
                throw new IllegalArgumentException("It is not the player's turn");
            }
            if (letters.isEmpty()) {
                throw new IllegalArgumentException("No tiles selected for exchange");
            }
            if (letters.size() > game.getTileBag().remaining()) {
                throw new IllegalArgumentException("Not enough tiles left in the bag to exchange");
            }
            PlayerState player = game.requirePlayer(playerId);
            List<Tile> toReturn = new ArrayList<>();
            for (char letter : letters) {
                boolean blank = letter == '?';
                Tile tile = player.removeTile(letter, blank);
                toReturn.add(tile);
            }
            game.getTileBag().returnTiles(toReturn);
            player.refillRack(game.getTileBag());
            game.resetPasses();
            if (game.getStatus() == GameStatus.ACTIVE) {
                game.advanceTurn();
                game.markTurnStart(clock.instant());
            }
            GameSnapshot snapshot = snapshot(game);
            gameNotifier.notifyGame(snapshot);
            return snapshot;
        } finally {
            session.getLock().unlock();
        }
    }

    public GameSnapshot pass(UUID gameId, UUID playerId) {
        GameSession session = requireSession(gameId);
        session.getLock().lock();
        try {
            GameState game = session.getGame();
            if (updateClock(game)) {
                GameSnapshot snapshot = snapshot(game);
                gameNotifier.notifyGame(snapshot);
                throw new IllegalStateException("Player timed out");
            }
            ensureActive(game);
            if (!game.getCurrentTurn().equals(playerId)) {
                throw new IllegalArgumentException("It is not the player's turn");
            }
            game.incrementPass();
            if (game.getConsecutivePasses() >= 4) {
                game.setStatus(GameStatus.COMPLETED);
            }
            if (game.getStatus() == GameStatus.ACTIVE) {
                game.advanceTurn();
                game.markTurnStart(clock.instant());
            }
            GameSnapshot snapshot = snapshot(game);
            gameNotifier.notifyGame(snapshot);
            return snapshot;
        } finally {
            session.getLock().unlock();
        }
    }

    private void handlePostMove(GameState game, PlayerState player) {
        boolean playerFinished = player.rackView().isEmpty();
        if (playerFinished && game.getTileBag().remaining() == 0) {
            game.setStatus(GameStatus.COMPLETED);
            log.info("Game {} completed: player {} emptied rack", game.getId(), player.getId());
        }
    }

    private void registerGame(GameState game) {
        game.markTurnStart(clock.instant());
        GameSession session = new GameSession(game);
        games.put(game.getId(), session);
        for (UUID playerId : game.getTurnOrder()) {
            playerToGame.put(playerId, game.getId());
        }
    }

    private GameSession requireSession(UUID gameId) {
        GameSession session = games.get(gameId);
        if (session == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }
        return session;
    }

    private void ensureActive(GameState game) {
        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new IllegalStateException("Game is not active");
        }
    }

    private boolean updateClock(GameState game) {
        if (game.getStatus() != GameStatus.ACTIVE) {
            return false;
        }
        Instant now = clock.instant();
        Instant last = game.getLastTurnTimestamp();
        if (last == null) {
            game.markTurnStart(now);
            return false;
        }
        long elapsed = Duration.between(last, now).toMillis();
        if (elapsed <= 0) {
            return false;
        }
        PlayerState current = game.requirePlayer(game.getCurrentTurn());
        boolean expired = current.consumeTime(elapsed);
        game.markTurnStart(now);
        if (expired) {
            game.setStatus(GameStatus.COMPLETED);
        }
        return expired;
    }

    private GameSnapshot snapshot(GameState game) {
        List<String> boardRows = game.getBoard().asStringRows();
        List<PlayerSnapshot> players = new ArrayList<>();
        for (UUID playerId : game.getTurnOrder()) {
            PlayerState state = game.requirePlayer(playerId);
            List<String> rack = state.rackView().stream()
                    .map(tile -> tile.blank() && tile.letter() == '?' ? "?" : String.valueOf(tile.letter()))
                    .toList();
            players.add(new PlayerSnapshot(state.getId(), state.getName(), state.getScore(), rack, state.getRemainingTimeMillis()));
        }
        return new GameSnapshot(game.getId(), boardRows, players, game.getCurrentTurn(), game.getStatus(),
                game.getTileBag().remaining());
    }

    private GameState createGame(String playerAName, UUID playerAId, String playerBName, UUID playerBId) {
        PlayerState playerA = new PlayerState(playerAId, playerAName);
        PlayerState playerB = new PlayerState(playerBId, playerBName);
        playerA.resetClock();
        playerB.resetClock();
        TileBag tileBag = new TileBag(random.split());
        playerA.refillRack(tileBag);
        playerB.refillRack(tileBag);
        GameState game = new GameState(UUID.randomUUID(), List.of(playerA, playerB), tileBag);
        log.info("Game {} initialized for players {} and {}", game.getId(), playerAName, playerBName);
        return game;
    }

    public record JoinResult(UUID playerId, UUID gameId, GameSnapshot game, boolean waiting) {
    }

    public record MoveResult(GameSnapshot game, int scoreEarned, List<String> wordsFormed) {
    }

    private final class Lobby {

        private final ReentrantLock lock = new ReentrantLock();
        private LobbyPlayer waiting;

        JoinOutcome addPlayer(String playerName) {
            lock.lock();
            try {
                UUID playerId = UUID.randomUUID();
                if (waiting == null) {
                    waiting = new LobbyPlayer(playerId, playerName);
                    log.info("Player {} waiting for opponent", playerName);
                    return new JoinOutcome(playerId, null);
                }
                LobbyPlayer opponent = waiting;
                waiting = null;
                GameState game = createGame(opponent.name(), opponent.id(), playerName, playerId);
                return new JoinOutcome(playerId, game);
            } finally {
                lock.unlock();
            }
        }

        private record LobbyPlayer(UUID id, String name) {
        }

        private record JoinOutcome(UUID playerId, GameState gameCreated) {
        }
    }
}



