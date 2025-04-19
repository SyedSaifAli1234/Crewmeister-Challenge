package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.facade.CurrencyFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateControllerTest {

    @Mock
    private CurrencyFacade currencyFacade;

    @InjectMocks
    private ExchangeRateController exchangeRateController;

    private LocalDate testDate;
    private ExchangeRate testRate;
    private List<ExchangeRate> testRates;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 4, 19);
        testRate = new ExchangeRate("USD", testDate, new BigDecimal("1.0987"));
        testRates = Arrays.asList(
            new ExchangeRate("USD", testDate, new BigDecimal("1.0987")),
            new ExchangeRate("USD", testDate.minusDays(1), new BigDecimal("1.0985"))
        );
    }

    @Test
    void getExchangeRates_shouldReturnAllRatesForCurrency() {
        // Arrange
        when(currencyFacade.getExchangeRatesForCurrency("USD")).thenReturn(testRates);

        // Act
        ResponseEntity<List<ExchangeRate>> response = exchangeRateController.getExchangeRates("USD");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("USD", response.getBody().get(0).getCurrency());
        assertEquals(new BigDecimal("1.0987"), response.getBody().get(0).getRate());
    }

    @Test
    void getExchangeRateForDate_shouldReturnRateForSpecificDate() {
        // Arrange
        when(currencyFacade.getExchangeRateForDate(eq("USD"), any(LocalDate.class)))
            .thenReturn(testRate);

        // Act
        ResponseEntity<ExchangeRate> response = exchangeRateController.getExchangeRateForDate(testDate, "USD");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("USD", response.getBody().getCurrency());
        assertEquals(testDate, response.getBody().getDate());
        assertEquals(new BigDecimal("1.0987"), response.getBody().getRate());
    }

    @Test
    void convertCurrency_shouldReturnConvertedAmount() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal rate = new BigDecimal("1.0987");
        BigDecimal convertedAmount = new BigDecimal("91.02");
        ConversionResultDTO expectedResult = new ConversionResultDTO(
            "USD", amount, rate, convertedAmount, testDate);
        
        when(currencyFacade.convertToEur(eq("USD"), eq(amount), any(LocalDate.class)))
            .thenReturn(expectedResult);

        // Act
        ResponseEntity<ConversionResultDTO> response = exchangeRateController.convertCurrency(
            "USD", amount, testDate);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(amount, response.getBody().getAmount());
        assertEquals("USD", response.getBody().getFromCurrency());
        assertEquals(convertedAmount, response.getBody().getConvertedAmount());
        assertEquals(rate, response.getBody().getRate());
        assertEquals(testDate, response.getBody().getDate());
    }

    @Test
    void getExchangeRates_shouldHandleInvalidCurrency() {
        // Arrange
        when(currencyFacade.getExchangeRatesForCurrency("INVALID"))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.getExchangeRates("INVALID"));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void getExchangeRates_shouldHandleFacadeException() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(currencyFacade).getExchangeRatesForCurrency("USD");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.getExchangeRates("USD"));
        assertEquals(500, exception.getStatus().value());
    }

    @Test
    void getExchangeRateForDate_shouldHandleInvalidCurrency() {
        // Arrange
        when(currencyFacade.getExchangeRateForDate(eq("INVALID"), any(LocalDate.class)))
            .thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.getExchangeRateForDate(testDate, "INVALID"));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void getExchangeRateForDate_shouldHandleInvalidDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusYears(1);
        when(currencyFacade.getExchangeRateForDate(eq("USD"), eq(futureDate)))
            .thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.getExchangeRateForDate(futureDate, "USD"));
        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void getExchangeRateForDate_shouldHandleFacadeException() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(currencyFacade).getExchangeRateForDate(eq("USD"), any(LocalDate.class));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.getExchangeRateForDate(testDate, "USD"));
        assertEquals(500, exception.getStatus().value());
    }

    @Test
    void convertCurrency_shouldHandleInvalidCurrency() {
        // Arrange
        when(currencyFacade.convertToEur(eq("INVALID"), any(BigDecimal.class), any(LocalDate.class)))
            .thenThrow(new IllegalArgumentException("Invalid currency code"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.convertCurrency("INVALID", new BigDecimal("100.00"), testDate));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void convertCurrency_shouldHandleInvalidAmount() {
        // Arrange
        when(currencyFacade.convertToEur(eq("USD"), eq(new BigDecimal("-100.00")), any(LocalDate.class)))
            .thenThrow(new IllegalArgumentException("Amount must be positive"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.convertCurrency("USD", new BigDecimal("-100.00"), testDate));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void convertCurrency_shouldHandleInvalidDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusYears(1);
        when(currencyFacade.convertToEur(eq("USD"), any(BigDecimal.class), eq(futureDate)))
            .thenThrow(new IllegalArgumentException("No exchange rate available for future date"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.convertCurrency("USD", new BigDecimal("100.00"), futureDate));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void convertCurrency_shouldHandleFacadeException() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
            .when(currencyFacade).convertToEur(eq("USD"), any(BigDecimal.class), any(LocalDate.class));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> exchangeRateController.convertCurrency("USD", new BigDecimal("100.00"), testDate));
        assertEquals(500, exception.getStatus().value());
    }
} 