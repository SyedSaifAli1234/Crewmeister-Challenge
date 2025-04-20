package com.crewmeister.cmcodingchallenge.integration;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExchangeRateIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        exchangeRateRepository.deleteAll();
    }

    @Test
    void testGetAllExchangeRates() {
        // Given: Some exchange rates in the database
        List<ExchangeRate> rates = Arrays.asList(
            new ExchangeRate("USD", LocalDate.of(2023, 1, 1), new BigDecimal("1.2345")),
            new ExchangeRate("USD", LocalDate.of(2023, 1, 2), new BigDecimal("1.2346")),
            new ExchangeRate("GBP", LocalDate.of(2023, 1, 1), new BigDecimal("0.8765"))
        );
        exchangeRateRepository.saveAll(rates);

        // When: Making a request to get USD exchange rates
        String url = "http://localhost:" + port + "/api/v1/exchange-rates?currency=USD";
        ResponseEntity<ExchangeRate[]> response = restTemplate.getForEntity(url, ExchangeRate[].class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getCurrency()).isEqualTo("USD");
        assertThat(response.getBody()[0].getRate()).isEqualTo(new BigDecimal("1.2346")); // First rate is for 2023-01-02
        assertThat(response.getBody()[1].getRate()).isEqualTo(new BigDecimal("1.2345")); // Second rate is for 2023-01-01
    }

    @Test
    void testGetExchangeRateForDate() {
        // Given: An exchange rate in the database
        ExchangeRate rate = new ExchangeRate("USD", LocalDate.of(2023, 1, 1), new BigDecimal("1.2345"));
        exchangeRateRepository.save(rate);

        // When: Making a request to get the exchange rate for a specific date
        String url = "http://localhost:" + port + "/api/v1/exchange-rates/2023-01-01?currency=USD";
        ResponseEntity<ExchangeRate> response = restTemplate.getForEntity(url, ExchangeRate.class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCurrency()).isEqualTo("USD");
        assertThat(response.getBody().getDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(response.getBody().getRate()).isEqualTo(new BigDecimal("1.2345"));
    }

    @Test
    void testConvertCurrency() {
        // Given: An exchange rate in the database
        ExchangeRate rate = new ExchangeRate("USD", LocalDate.of(2023, 1, 1), new BigDecimal("1.2345"));
        exchangeRateRepository.save(rate);

        // When: Making a request to convert USD to EUR
        String url = "http://localhost:" + port + "/api/v1/exchange-rates/convert?currency=USD&amount=100.00&date=2023-01-01";
        ResponseEntity<ConversionResultDTO> response = restTemplate.getForEntity(url, ConversionResultDTO.class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.getBody().getFromCurrency()).isEqualTo("USD");
        assertThat(response.getBody().getConvertedAmount()).isEqualTo(new BigDecimal("81.00")); // 100 / 1.2345 ≈ 81.00
        assertThat(response.getBody().getRate()).isEqualTo(new BigDecimal("1.2345"));
        assertThat(response.getBody().getDate()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void testInvalidCurrency() {
        // When: Making a request with an invalid currency
        String url = "http://localhost:" + port + "/api/v1/exchange-rates?currency=XYZ";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testFutureDate() {
        // When: Making a request with a future date
        LocalDate futureDate = LocalDate.now().plusDays(1);
        String url = "http://localhost:" + port + "/api/v1/exchange-rates/" + futureDate + "?currency=USD";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testNegativeAmount() {
        // When: Making a request with a negative amount
        String url = "http://localhost:" + port + "/api/v1/exchange-rates/convert?currency=USD&amount=-100.00&date=2023-01-01";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then: Verify the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
