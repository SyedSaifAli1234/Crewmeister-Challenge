package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.domain.Currency;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    private static final String SAMPLE_CSV_HEADER =
        "BBEX3.D.USD.EUR.BB.AC.000;BBEX3.D.USD.EUR.BB.AC.000_FLAGS;BBEX3.D.GBP.EUR.BB.AC.000;BBEX3.D.GBP.EUR.BB.AC.000_FLAGS";
    private static final String SAMPLE_CSV = SAMPLE_CSV_HEADER + "\n" +
        "Some;Other;Lines;Irrelevant";
    private static final String BUNDESBANK_CURRENCY_URL = 
        "https://api.statistiken.bundesbank.de/rest/data/BBEX3/D..EUR.BB.AC.000?detail=serieskeyonly&format=csv";

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        // No additional setup needed as Mockito injects mocks
    }

    @Test
    void parseCSVResponse_shouldExtractOnlyUniqueCurrencyCodes() {
        List<Currency> result = currencyService.parseCSVResponse(SAMPLE_CSV);

        // Should only pick up USD and GBP, skipping the _FLAGS ones
        assertThat(result).extracting(Currency::getCode)
            .containsExactlyInAnyOrder("USD", "GBP");
    }

    @Test
    void updateCurrencies_shouldFetchAndSaveParsedCurrencies() {
        // Arrange
        when(restTemplate.getForObject(eq(BUNDESBANK_CURRENCY_URL), eq(String.class)))
            .thenReturn(SAMPLE_CSV);

        // Act
        currencyService.updateCurrencies();

        // Assert: capture saved currencies
        ArgumentCaptor<List<Currency>> captor = ArgumentCaptor.forClass(List.class);
        verify(currencyRepository).saveAll(captor.capture());
        List<Currency> saved = captor.getValue();

        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Currency::getCode)
            .containsExactlyInAnyOrder("USD", "GBP");
    }

    @Test
    void isValidCurrencyFormat_acceptsThreeUppercaseLetters() {
        assertThat(currencyService.isValidCurrencyFormat("ABC")).isTrue();
        assertThat(currencyService.isValidCurrencyFormat("abcd")).isFalse();
        assertThat(currencyService.isValidCurrencyFormat("12#")).isFalse();
    }

    @Test
    void getAllCurrencies_cachedAndFilteredByRepository() {
        // Given repository returns some currencies, including an invalid one
        List<Currency> repoList = List.of(
            new Currency("EUR"),
            new Currency("XYZ"),  // invalid, not in DB
            new Currency("USD")
        );
        when(currencyRepository.findAll()).thenReturn(repoList);

        List<String> codes = currencyService.getAllCurrencies();

        assertThat(codes).containsExactlyInAnyOrder("EUR", "XYZ", "USD");
    }

    @Test
    void isValidCurrency_checksRepositoryExistence() {
        when(currencyRepository.existsById("EUR")).thenReturn(true);
        when(currencyRepository.existsById("FOO")).thenReturn(false);

        assertThat(currencyService.isValidCurrency("EUR")).isTrue();
        assertThat(currencyService.isValidCurrency("FOO")).isFalse();
    }
}