package com.code.red.scrabble.support;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.code.red.scrabble.dto.GameStateDto;
import com.code.red.scrabble.model.GameSnapshot;

@Component
public class GameNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public GameNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyGame(GameSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        GameStateDto payload = GameDtoMapper.toDto(snapshot);
        messagingTemplate.convertAndSend(topic(snapshot.gameId()), payload);
    }

    private static String topic(java.util.UUID gameId) {
        return "/topic/game/" + gameId;
    }
}
