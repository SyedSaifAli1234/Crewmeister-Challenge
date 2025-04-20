package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateException;
import com.crewmeister.cmcodingchallenge.integration.BundesbankApiClient;
import com.crewmeister.cmcodingchallenge.integration.BundesbankApiClient.ExchangeRateData;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository repository;

    @Mock
    private BundesbankApiClient bundesbankApiClient;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private final CacheManager cacheManager = new ConcurrentMapCacheManager("exchangeRates", "exchangeRate");

    @BeforeEach
    void setUp() {
        // Enable lenient mocking to avoid unnecessary stubbing errors
        lenient().when(currencyService.isValidCurrency("USD")).thenReturn(true);
    }

    @Test
    void getExchangeRates_shouldReturnAllRates() {
        // Given
        ExchangeRate rate1 = new ExchangeRate("USD", LocalDate.now(), BigDecimal.valueOf(1.1));
        when(repository.findByCurrencyOrderByDateDesc("USD")).thenReturn(Arrays.asList(rate1));
        
        // When
        List<ExchangeRateDTO> usdRates = exchangeRateService.getAllExchangeRates("USD");
        
        // Then
        assertNotNull(usdRates);
        assertFalse(usdRates.isEmpty());
        assertEquals(1, usdRates.size());
        assertEquals("USD", usdRates.get(0).getCurrency());
    }

    @Test
    void getExchangeRatesForDate_shouldReturnRatesForSpecificDate() {
        // Given
        LocalDate date = LocalDate.now();
        ExchangeRate rate1 = new ExchangeRate("USD", date, BigDecimal.valueOf(1.1));
        when(repository.findByCurrencyAndDate("USD", date)).thenReturn(Optional.of(rate1));
        
        // When
        ExchangeRateDTO usdRate = exchangeRateService.getExchangeRate("USD", date);
        
        // Then
        assertNotNull(usdRate);
        assertEquals("USD", usdRate.getCurrency());
        assertEquals(date, usdRate.getDate());
    }

    @Test
    void convertCurrency_shouldThrowExceptionForInvalidCurrency() {
        // Given
        String invalidCurrency = "XYZ"; // Using a 3-letter invalid currency
        when(currencyService.isValidCurrency(invalidCurrency)).thenReturn(false);
        
        // When & Then
        ExchangeRateException exception = assertThrows(ExchangeRateException.class, () -> 
            exchangeRateService.convertCurrency(invalidCurrency, BigDecimal.valueOf(100), LocalDate.now()));
        assertEquals("INVALID_CURRENCY", exception.getErrorCode());
    }

    @Test
    void convertCurrency_shouldThrowExceptionForInvalidCurrencyFormat() {
        // Given - no specific setup needed
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            exchangeRateService.convertCurrency("INVALID", BigDecimal.valueOf(100), LocalDate.now()));
        assertEquals("Currency code must be 3 characters long", exception.getMessage());
    }

    @Test
    void convertCurrency_shouldThrowExceptionForFutureDate() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        
        // When & Then
        ExchangeRateException exception = assertThrows(ExchangeRateException.class, () -> 
            exchangeRateService.convertCurrency("USD", BigDecimal.valueOf(100), futureDate));
        assertEquals("FUTURE_DATE", exception.getErrorCode());
    }

    @Test
    void convertCurrency_shouldThrowExceptionForNegativeAmount() {
        // Given - no specific setup needed as USD is already set as valid in setUp()
        
        // When & Then
        ExchangeRateException exception = assertThrows(ExchangeRateException.class, () -> 
            exchangeRateService.convertCurrency("USD", BigDecimal.valueOf(-100), LocalDate.now()));
        assertEquals("INVALID_AMOUNT", exception.getErrorCode());
    }

    @Test
    void convertCurrency_shouldConvertAmountCorrectly() {
        // Given
        LocalDate date = LocalDate.now();
        ExchangeRate rate = new ExchangeRate("USD", date, BigDecimal.valueOf(1.1));
        when(repository.findByCurrencyAndDate("USD", date)).thenReturn(Optional.of(rate));
        
        // When
        ConversionResultDTO result = exchangeRateService.convertCurrency("USD", BigDecimal.valueOf(100), date);
        
        // Then
        assertNotNull(result);
        assertEquals("USD", result.getFromCurrency());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals(rate.getRate(), result.getRate());
        assertEquals(BigDecimal.valueOf(90.91), result.getConvertedAmount());
        assertEquals(date, result.getDate());
    }

    @Test
    void getExchangeRatesForCurrency_shouldReturnRatesForValidCurrency() {
        // Given
        ExchangeRate rate1 = new ExchangeRate("USD", LocalDate.now(), BigDecimal.valueOf(1.1));
        ExchangeRate rate2 = new ExchangeRate("USD", LocalDate.now().minusDays(1), BigDecimal.valueOf(1.2));
        when(repository.findByCurrencyOrderByDateDesc("USD")).thenReturn(Arrays.asList(rate1, rate2));
        
        // When
        List<ExchangeRate> result = exchangeRateService.getExchangeRatesForCurrency("USD");
        
        // Then
        assertEquals(2, result.size());
        assertEquals(rate1, result.get(0));
        assertEquals(rate2, result.get(1));
    }

    @Test
    void getExchangeRatesForCurrency_shouldThrowExceptionForNoRates() {
        // Given
        when(repository.findByCurrencyOrderByDateDesc("USD")).thenReturn(Collections.emptyList());
        
        // When & Then
        ExchangeRateException exception = assertThrows(ExchangeRateException.class, () -> 
            exchangeRateService.getExchangeRatesForCurrency("USD"));
        assertEquals("NO_RATES_FOUND", exception.getErrorCode());
    }

    @Test
    void getAllExchangeRates_shouldReturnEmptyListForNoRates() {
        // Given
        when(repository.findByCurrencyOrderByDateDesc("USD")).thenReturn(Collections.emptyList());
        
        // When
        List<ExchangeRateDTO> result = exchangeRateService.getAllExchangeRates("USD");
        
        // Then
        assertTrue(result.isEmpty());
    }
} 