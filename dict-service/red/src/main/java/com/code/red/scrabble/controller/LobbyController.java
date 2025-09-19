package com.code.red.scrabble.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.code.red.scrabble.dto.GameStateDto;
import com.code.red.scrabble.dto.JoinLobbyRequest;
import com.code.red.scrabble.dto.JoinLobbyResponse;
import com.code.red.scrabble.service.ScrabbleService;
import com.code.red.scrabble.support.GameDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/lobby")
@Validated
public class LobbyController {

    private final ScrabbleService scrabbleService;

    public LobbyController(ScrabbleService scrabbleService) {
        this.scrabbleService = scrabbleService;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinLobbyResponse joinLobby(@Valid @RequestBody JoinLobbyRequest request) {
        ScrabbleService.JoinResult result = scrabbleService.joinLobby(request.playerName());
        GameStateDto state = result.game() == null ? null : GameDtoMapper.toDto(result.game());
        return new JoinLobbyResponse(result.playerId(), result.waiting(), result.gameId(), state);
    }

    @GetMapping("/{playerId}")
    public JoinLobbyResponse lobbyStatus(@PathVariable UUID playerId) {
        return scrabbleService.gameForPlayer(playerId)
                .map(gameId -> {
                    GameStateDto state = GameDtoMapper.toDto(scrabbleService.getGame(gameId));
                    return new JoinLobbyResponse(playerId, false, gameId, state);
                })
                .orElseGet(() -> new JoinLobbyResponse(playerId, true, null, null));
    }
}
