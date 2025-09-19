package com.code.red.scrabble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class GameClockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MutableClock clock;

    @Test
    void playerTimesOutAfterTenMinutes() throws Exception {
        JsonNode firstJoin = join("Alice");
        UUID playerOneId = UUID.fromString(firstJoin.get("playerId").asText());
        assertThat(firstJoin.get("waiting").asBoolean()).isTrue();

        JsonNode secondJoin = join("Bob");
        UUID playerTwoId = UUID.fromString(secondJoin.get("playerId").asText());
        UUID gameId = UUID.fromString(secondJoin.get("gameId").asText());
        JsonNode state = secondJoin.get("game");
        UUID currentPlayerId = UUID.fromString(state.get("currentPlayerId").asText());
        UUID opponentId = currentPlayerId.equals(playerOneId) ? playerTwoId : playerOneId;

        clock.advance(Duration.ofMinutes(11));

        MvcResult result = mockMvc.perform(get("/game/" + gameId + "/state"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode updated = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(updated.get("status").asText()).isEqualTo("COMPLETED");
        JsonNode players = updated.get("players");
        JsonNode timedOutPlayer = findPlayer(players, currentPlayerId);
        assertThat(timedOutPlayer.get("remainingTimeMillis").asLong()).isEqualTo(0L);
        JsonNode opponent = findPlayer(players, opponentId);
        assertThat(opponent.get("remainingTimeMillis").asLong()).isGreaterThan(0L);
    }

    private JsonNode join(String playerName) throws Exception {
        String payload = objectMapper.createObjectNode()
                .put("playerName", playerName)
                .toString();
        MvcResult result = mockMvc.perform(post("/lobby/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static JsonNode findPlayer(JsonNode players, UUID playerId) {
        for (JsonNode node : players) {
            if (node.get("playerId").asText().equals(playerId.toString())) {
                return node;
            }
        }
        throw new IllegalStateException("Player not found in response: " + playerId);
    }

    @TestConfiguration
    static class ClockTestConfig {

        @Bean
        MutableClock mutableClock() {
            return new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        }

        @Bean
        Clock clock(MutableClock mutableClock) {
            return mutableClock;
        }
    }

    static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant initial, ZoneId zone) {
            this.instant = initial;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
