package com.aisleon.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiSpecIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void openApiEndpointReturnsValidSpec() throws Exception {
        ResponseEntity<String> response =
                rest.getForEntity("http://localhost:" + port + "/api/openapi.json", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        assertThat(root.hasNonNull("openapi")).isTrue();
        assertThat(root.path("openapi").asText()).startsWith("3.");
        assertThat(root.hasNonNull("paths")).isTrue();

        JsonNode schemas = root.path("components").path("schemas");
        // DietaryTag enum values — look up wherever it lives (named schema or inlined).
        String body = response.getBody();
        assertThat(body).as("openapi body").isNotNull();
        for (DietaryTag tag : DietaryTag.values()) {
            assertThat(body)
                    .as("DietaryTag enum value " + tag.name() + " present in spec")
                    .contains(tag.name());
        }
        for (Retailer retailer : Retailer.values()) {
            assertThat(body)
                    .as("Retailer enum value " + retailer.name() + " present in spec")
                    .contains(retailer.name());
        }
    }
}
