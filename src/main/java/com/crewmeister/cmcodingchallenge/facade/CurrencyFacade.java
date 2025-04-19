package com.crewmeister.cmcodingchallenge.facade;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CurrencyFacade {
    /**
     * Get all available currencies
     * @return List of currency codes
     */
    List<String> getAllCurrencies();

    /**
     * Get exchange rates for a specific currency
     * @param currency The currency code
     * @return List of exchange rates
     */
    List<ExchangeRate> getExchangeRatesForCurrency(String currency);

    /**
     * Get exchange rate for a specific currency and date
     * @param currency The currency code
     * @param date The date
     * @return The exchange rate
     */
    ExchangeRate getExchangeRateForDate(String currency, LocalDate date);

    /**
     * Convert an amount from a currency to EUR
     * @param currency The source currency
     * @param amount The amount to convert
     * @param date The date for the conversion
     * @return The conversion result
     */
    ConversionResultDTO convertToEur(String currency, BigDecimal amount, LocalDate date);
} 