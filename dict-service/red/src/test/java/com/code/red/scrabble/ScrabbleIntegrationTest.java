package com.code.red.scrabble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.code.red.dictionary.DictionaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ScrabbleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DictionaryService dictionaryService;

    @Test
    void joinTwoPlayersStartsGame() throws Exception {
        GameContext context = startGame("Alice", "Bob");

        assertThat(context.gameId()).isNotNull();
        assertThat(context.playerOneId()).isNotNull();
        assertThat(context.playerTwoId()).isNotNull();
        assertThat(context.state().get("players").size()).isEqualTo(2);

        MvcResult lobbyResult = mockMvc.perform(get("/lobby/" + context.playerOneId()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode lobbyNode = objectMapper.readTree(lobbyResult.getResponse().getContentAsString());
        assertThat(lobbyNode.get("waiting").asBoolean()).isFalse();
        assertThat(UUID.fromString(lobbyNode.get("gameId").asText())).isEqualTo(context.gameId());
    }

    @Test
    void placeWordUpdatesBoard() throws Exception {
        GameContext context = startGame("Clara", "Dylan");
        UUID currentPlayer = UUID.fromString(context.state().get("currentPlayerId").asText());
        JsonNode players = context.state().get("players");
        JsonNode activePlayer = null;
        for (JsonNode node : players) {
            if (node.get("playerId").asText().equals(currentPlayer.toString())) {
                activePlayer = node;
                break;
            }
        }
        assertThat(activePlayer).isNotNull();
        List<String> rack = new ArrayList<>();
        activePlayer.get("rack").forEach(tile -> rack.add(tile.asText()));
        String rackLetters = String.join("", rack).toLowerCase();
        String word = selectPlayableWord(rackLetters);
        List<PlacementPayload> placements = buildPlacements(word, rackLetters);

        String requestBody = buildMovePayload(currentPlayer, placements);

        MvcResult moveResult = mockMvc.perform(post("/game/" + context.gameId() + "/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode moveNode = objectMapper.readTree(moveResult.getResponse().getContentAsString());
        JsonNode rows = moveNode.get("game").get("board");
        String row = rows.get(7).asText();
        assertThat(row.replace(".", "")).contains(word.toLowerCase());
    }

    private GameContext startGame(String playerOne, String playerTwo) throws Exception {
        JsonNode firstJoin = performJoin(playerOne);
        UUID playerOneId = UUID.fromString(firstJoin.get("playerId").asText());
        assertThat(firstJoin.get("waiting").asBoolean()).isTrue();

        JsonNode secondJoin = performJoin(playerTwo);
        UUID playerTwoId = UUID.fromString(secondJoin.get("playerId").asText());
        assertThat(secondJoin.get("waiting").asBoolean()).isFalse();
        UUID gameId = UUID.fromString(secondJoin.get("gameId").asText());
        JsonNode gameState = secondJoin.get("game");
        return new GameContext(playerOneId, playerTwoId, gameId, gameState);
    }

    private JsonNode performJoin(String playerName) throws Exception {
        String payload = objectMapper.writeValueAsString(new JoinPayload(playerName));
        MvcResult result = mockMvc.perform(post("/lobby/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String selectPlayableWord(String rackLetters) {
        int length = rackLetters.length();
        Set<String> seen = new HashSet<>();
        for (int mask = (1 << length) - 1; mask > 0; mask--) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if ((mask & (1 << i)) != 0) {
                    builder.append(rackLetters.charAt(i));
                }
            }
            String subset = builder.toString();
            char[] chars = subset.toCharArray();
            java.util.Arrays.sort(chars);
            String key = new String(chars);
            if (!seen.add(key)) {
                continue;
            }
            List<String> candidates = dictionaryService.anagrams(subset, 1);
            if (!candidates.isEmpty()) {
                return candidates.get(0);
            }
        }
        throw new IllegalStateException("No playable words found for rack: " + rackLetters);
    }

    private List<PlacementPayload> buildPlacements(String word, String rack) {
        int center = 7;
        int startCol = center - (word.length() / 2);
        List<PlacementPayload> placements = new ArrayList<>(word.length());
        int blanks = (int) rack.chars().filter(ch -> ch == '?').count();
        int[] counts = new int[26];
        for (char ch : rack.toCharArray()) {
            if (ch == '?') {
                continue;
            }
            counts[ch - 'a'] += 1;
        }
        for (int i = 0; i < word.length(); i++) {
            char letter = Character.toLowerCase(word.charAt(i));
            boolean useBlank;
            if (counts[letter - 'a'] > 0) {
                counts[letter - 'a'] -= 1;
                useBlank = false;
            } else {
                if (blanks <= 0) {
                    throw new IllegalStateException("Not enough tiles in rack for word: " + word);
                }
                blanks -= 1;
                useBlank = true;
            }
            placements.add(new PlacementPayload(7, startCol + i, String.valueOf(letter), useBlank));
        }
        return placements;
    }

    private String buildMovePayload(UUID playerId, List<PlacementPayload> placements) throws Exception {
        MovePayload payload = new MovePayload(playerId, placements);
        return objectMapper.writeValueAsString(payload);
    }

    private record GameContext(UUID playerOneId, UUID playerTwoId, UUID gameId, JsonNode state) {
    }

    private record JoinPayload(String playerName) {
    }

    private record PlacementPayload(int row, int col, String letter, boolean blank) {
    }

    private record MovePayload(UUID playerId, List<PlacementPayload> placements) {
    }
}
