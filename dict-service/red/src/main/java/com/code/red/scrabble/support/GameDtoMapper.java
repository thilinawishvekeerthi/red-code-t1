package com.code.red.scrabble.support;

import java.util.ArrayList;
import java.util.List;

import com.code.red.scrabble.dto.GameStateDto;
import com.code.red.scrabble.dto.PlayerDto;
import com.code.red.scrabble.model.GameSnapshot;
import com.code.red.scrabble.model.PlayerSnapshot;

public final class GameDtoMapper {

    private GameDtoMapper() {
    }

    public static GameStateDto toDto(GameSnapshot snapshot) {
        List<PlayerDto> players = new ArrayList<>(snapshot.players().size());
        for (PlayerSnapshot player : snapshot.players()) {
            players.add(new PlayerDto(player.playerId(), player.name(), player.score(), player.rack()));
        }
        return new GameStateDto(snapshot.gameId(), snapshot.board(), players, snapshot.currentPlayerId(),
                snapshot.status(), snapshot.tileBagRemaining());
    }
}
