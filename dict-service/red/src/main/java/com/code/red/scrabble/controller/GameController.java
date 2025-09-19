package com.code.red.scrabble.controller;

import java.util.List;
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

import com.code.red.scrabble.dto.ExchangeRequest;
import com.code.red.scrabble.dto.GameStateDto;
import com.code.red.scrabble.dto.MoveRequest;
import com.code.red.scrabble.dto.MoveResponse;
import com.code.red.scrabble.dto.PassRequest;
import com.code.red.scrabble.service.Placement;
import com.code.red.scrabble.service.ScrabbleService;
import com.code.red.scrabble.support.GameDtoMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/game")
@Validated
public class GameController {

    private final ScrabbleService scrabbleService;

    public GameController(ScrabbleService scrabbleService) {
        this.scrabbleService = scrabbleService;
    }

    @GetMapping("/{gameId}/state")
    public GameStateDto gameState(@PathVariable UUID gameId) {
        return GameDtoMapper.toDto(scrabbleService.getGame(gameId));
    }

    @PostMapping("/{gameId}/move")
    @ResponseStatus(HttpStatus.OK)
    public MoveResponse playMove(@PathVariable UUID gameId, @Valid @RequestBody MoveRequest request) {
        List<Placement> placements = request.placements().stream()
                .map(dto -> new Placement(dto.row(), dto.col(), Character.toLowerCase(dto.letter().charAt(0)),
                        dto.blank()))
                .toList();
        ScrabbleService.MoveResult result = scrabbleService.playMove(gameId, request.playerId(), placements);
        return new MoveResponse(GameDtoMapper.toDto(result.game()), result.scoreEarned(), result.wordsFormed());
    }

    @PostMapping("/{gameId}/exchange")
    @ResponseStatus(HttpStatus.OK)
    public GameStateDto exchange(@PathVariable UUID gameId, @Valid @RequestBody ExchangeRequest request) {
        List<Character> letters = request.letters().stream()
                .map(value -> Character.toLowerCase(value.charAt(0)))
                .toList();
        return GameDtoMapper.toDto(scrabbleService.exchangeTiles(gameId, request.playerId(), letters));
    }

    @PostMapping("/{gameId}/pass")
    @ResponseStatus(HttpStatus.OK)
    public GameStateDto pass(@PathVariable UUID gameId, @Valid @RequestBody PassRequest request) {
        return GameDtoMapper.toDto(scrabbleService.pass(gameId, request.playerId()));
    }
}
