package com.crewmeister.cmcodingchallenge.integration;

import com.crewmeister.cmcodingchallenge.domain.Currency;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CurrencyIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        currencyRepository.deleteAll();
    }

    @Test
    void testCompleteFlow_GetAllCurrencies() {
        // Given: Some currencies in the database
        List<Currency> currencies = Arrays.asList(
            new Currency("USD"),
            new Currency("EUR"),
            new Currency("GBP")
        );
        currencyRepository.saveAll(currencies);

        // When: Making a request to the API
        String url = "http://localhost:" + port + "/api/currencies";
        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()).containsExactlyInAnyOrder("USD", "EUR", "GBP");
    }

    @Test
    void testCompleteFlow_EmptyDatabase() {
        // Given: Empty database

        // When: Making a request to the API
        String url = "http://localhost:" + port + "/api/currencies";
        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testCompleteFlow_InvalidCurrencies() {
        // Given: Some valid and invalid currencies in the database
        List<Currency> currencies = Arrays.asList(
            new Currency("USD"),
            new Currency("INVALID"),
            new Currency("GBP")
        );
        currencyRepository.saveAll(currencies);

        // When: Making a request to the API
        String url = "http://localhost:" + port + "/api/currencies";
        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        // Then: Verify only valid currencies are returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder("USD", "GBP");
    }
} 