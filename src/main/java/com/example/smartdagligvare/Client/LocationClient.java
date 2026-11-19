package com.example.smartdagligvare.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class LocationClient {

    private final RestClient restClient;

    public LocationClient(
            @Value("${geocoding.base-url}")
            String baseUrl,

            @Value("${geocoding.user-agent}")
            String userAgent
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "User-Agent",
                        userAgent
                )
                .build();
    }

    public Coordinates findCoordinates(
            String locationText
    ) {
        JsonNode response = restClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/search")
                                .queryParam(
                                        "q",
                                        locationText
                                )
                                .queryParam(
                                        "format",
                                        "jsonv2"
                                )
                                .queryParam(
                                        "limit",
                                        1
                                )
                                .queryParam(
                                        "countrycodes",
                                        "no"
                                )
                                .build()
                )
                .retrieve()
                .body(JsonNode.class);

        if (
                response == null ||
                        !response.isArray() ||
                        response.size() == 0
        ) {
            throw new IllegalArgumentException(
                    "Could not find location: "
                            + locationText
            );
        }

        JsonNode result = response.get(0);

        return new Coordinates(
                result.path("display_name").asText(),
                result.path("lat").asDouble(),
                result.path("lon").asDouble()
        );
    }

    public record Coordinates(
            String displayName,
            double latitude,
            double longitude
    ) {
    }
}