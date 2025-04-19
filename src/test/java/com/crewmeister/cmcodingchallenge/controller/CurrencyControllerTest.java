package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.facade.CurrencyFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    @Mock
    private CurrencyFacade currencyFacade;

    @InjectMocks
    private CurrencyController currencyController;

    @BeforeEach
    void setUp() {
        // Any setup needed before each test
    }

    @Test
    void getAllCurrencies_shouldReturnListOfCurrencies() {
        // Arrange
        List<String> expectedCurrencies = Arrays.asList("EUR", "USD", "GBP");
        when(currencyFacade.getAllCurrencies()).thenReturn(expectedCurrencies);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedCurrencies, response.getBody());
    }

    @Test
    void getAllCurrencies_shouldReturnEmptyList() {
        // Arrange
        List<String> expectedCurrencies = Collections.emptyList();
        when(currencyFacade.getAllCurrencies()).thenReturn(expectedCurrencies);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAllCurrencies_shouldReturnSingleCurrency() {
        // Arrange
        List<String> expectedCurrencies = Collections.singletonList("EUR");
        when(currencyFacade.getAllCurrencies()).thenReturn(expectedCurrencies);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("EUR", response.getBody().get(0));
    }

    @Test
    void getAllCurrencies_shouldReturnLargeList() {
        // Arrange
        List<String> baseCurrencies = Arrays.asList(
            "EUR", "USD", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "HKD", "NZD",
            "SEK", "KRW", "SGD", "NOK", "MXN", "INR", "RUB", "ZAR", "TRY", "BRL"
        );
        
        // Create a list of 200 currencies by repeating and modifying the base currencies
        List<String> expectedCurrencies = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (String currency : baseCurrencies) {
                expectedCurrencies.add(currency + (i > 0 ? i : ""));
            }
        }
        
        when(currencyFacade.getAllCurrencies()).thenReturn(expectedCurrencies);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(200, response.getBody().size());
        assertEquals(expectedCurrencies, response.getBody());
    }

    @Test
    void getAllCurrencies_shouldHandleFacadeException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Database connection failed");
        doThrow(expectedException).when(currencyFacade).getAllCurrencies();

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() == null || response.getBody().isEmpty());
    }

    @Test
    void getAllCurrencies_shouldHandleNullResponse() {
        // Arrange
        when(currencyFacade.getAllCurrencies()).thenReturn(null);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() == null || response.getBody().isEmpty());
    }

    @Test
    void getAllCurrencies_shouldHandleNullValuesInList() {
        // Arrange
        List<String> currenciesWithNull = Arrays.asList("EUR", null, "USD", null, "GBP");
        when(currencyFacade.getAllCurrencies()).thenReturn(currenciesWithNull);

        // Act
        ResponseEntity<List<String>> response = currencyController.getAllCurrencies();

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() == null || response.getBody().isEmpty());
    }
} 