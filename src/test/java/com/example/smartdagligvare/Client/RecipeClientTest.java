package com.example.smartdagligvare.Client;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RecipeClientTest {
    @Test
    void parsesValidRecipeResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RecipeClient client = new RecipeClient(builder, "https://example.test", "test-key", "test-model", new ObjectMapper());
        server.expect(requestTo("https://example.test/chat/completions"))
                .andRespond(withSuccess("{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"{\\\"recipes\\\":[]}\"}}]}",
                        MediaType.APPLICATION_JSON));

        assertTrue(client.generateRecipes(2, java.util.List.of(), java.util.List.of()).recipes().isEmpty());
        server.verify();
    }

    @Test
    void rejectsTruncatedCompletion() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RecipeClient client = new RecipeClient(builder, "https://example.test", "test-key", "test-model", new ObjectMapper());
        server.expect(requestTo("https://example.test/chat/completions"))
                .andRespond(withSuccess("{\"choices\":[{\"finish_reason\":\"length\",\"message\":{\"content\":\"{}\"}}]}",
                        MediaType.APPLICATION_JSON));

        assertThrows(RuntimeException.class,
                () -> client.generateRecipes(2, java.util.List.of(), java.util.List.of()));
        server.verify();
    }
}

