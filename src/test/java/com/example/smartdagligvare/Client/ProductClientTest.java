package com.example.smartdagligvare.Client;

import com.example.smartdagligvare.Service.ProductSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProductClientTest {
    @Test
    void emptyApiResponseProducesEmptyList() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ProductSearchService service = new ProductSearchService(builder, "https://example.test", "test-key");
        server.expect(requestTo("https://example.test/products?search=ris&size=100"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        assertTrue(service.searchProducts("ris").isEmpty());
        server.verify();
    }
}

