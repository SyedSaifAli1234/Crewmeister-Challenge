package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.dto.ExchangeRateDTO;
import com.crewmeister.cmcodingchallenge.exception.ExchangeRateException;
import com.crewmeister.cmcodingchallenge.integration.BundesbankApiClient;
import com.crewmeister.cmcodingchallenge.integration.BundesbankApiClient.ExchangeRateData;
import com.crewmeister.cmcodingchallenge.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ExchangeRateService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final int BATCH_SIZE = 1000;
    
    private final ExchangeRateRepository repository;
    private final BundesbankApiClient bundesbankApiClient;
    private final CurrencyService currencyService;

    public ExchangeRateService(
            ExchangeRateRepository repository, 
            BundesbankApiClient bundesbankApiClient,
            CurrencyService currencyService) {
        this.repository = repository;
        this.bundesbankApiClient = bundesbankApiClient;
        this.currencyService = currencyService;
        logger.info("ExchangeRateService initialized");
    }

    @PostConstruct
    @Transactional
    public void initializeData() {
        if (repository.count() == 0) {
            logger.info("Initializing exchange rate data...");
            updateExchangeRatesParallel();
        } else {
            logger.info("Exchange rate data already exists in database");
        }
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI")
    @Transactional
    @CacheEvict(cacheNames = {"exchangeRates", "exchangeRate"}, allEntries = true)
    public void updateExchangeRates() {
        logger.info("Starting scheduled exchange rates update");
        updateExchangeRatesParallel();
    }

    public void updateExchangeRatesParallel() {
        logger.info("Starting parallel exchange rates update");
        long startTime = System.currentTimeMillis();

        List<String> currencies = currencyService.getAllCurrencies();
        logger.info("Processing {} currencies in parallel", currencies.size());
        
        // Process currencies in parallel
        currencies.parallelStream()
            .forEach(currency -> {
                try {
                    logger.debug("Processing currency: {}", currency);
                    List<ExchangeRateData> rates = bundesbankApiClient.fetchExchangeRates(currency);
                    logger.debug("Fetched {} rates for currency: {}", rates.size(), currency);
                    
                    Set<LocalDate> existingDates = repository.findByCurrency(currency).stream()
                        .map(ExchangeRate::getDate)
                        .collect(Collectors.toSet());

                    // Process rates in parallel
                    List<ExchangeRate> newRates = rates.parallelStream()
                        .filter(rate -> !existingDates.contains(rate.getDate()))
                        .map(rate -> new ExchangeRate(currency, rate.getDate(), rate.getRate()))
                        .collect(Collectors.toList());

                    if (!newRates.isEmpty()) {
                        repository.saveAll(newRates);
                        logger.info("Saved {} rates for currency {}", newRates.size(), currency);
                    } else {
                        logger.debug("No new rates for currency: {}", currency);
                    }
                } catch (Exception e) {
                    logger.error("Error processing rates for {}: {}", currency, e.getMessage(), e);
                }
            });
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Exchange rates update completed in {} seconds", duration / 1000.0);
    }

    @Cacheable(value = "exchangeRates", key = "#currency")
    public List<ExchangeRateDTO> getAllExchangeRates(String currency) {
        logger.debug("Fetching all exchange rates for currency: {}", currency);
        List<ExchangeRateDTO> rates = repository.findByCurrencyOrderByDateDesc(currency).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.debug("Found {} exchange rates for currency: {}", rates.size(), currency);
        return rates;
    }

    @Cacheable(value = "exchangeRate", key = "#currency + '_' + #date")
    public ExchangeRateDTO getExchangeRate(String currency, LocalDate date) {
        logger.debug("Fetching exchange rate for currency: {} on date: {}", currency, date);
        return repository.findByCurrencyAndDate(currency, date)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Converts an amount in a foreign currency to EUR using the exchange rate for the specified date.
     * The exchange rate represents how many units of the foreign currency you get for 1 EUR.
     * For example, if the rate is 1.1360 for USD, it means 1 EUR = 1.1360 USD.
     *
     * @param currency The source currency code (e.g., "USD")
     * @param amount The amount to convert
     * @param date The date for which to use the exchange rate
     * @return ConversionResultDTO containing the conversion details
     */
    public ConversionResultDTO convertCurrency(String currency, BigDecimal amount, LocalDate date) {
        logger.debug("Converting {} {} to EUR on date: {}", amount, currency, date);
        validateDate(date);  // Check for future date first
        validateCurrency(currency);
        validateAmount(amount);

        ExchangeRate rate = getExchangeRateForDate(currency, date);
        logger.debug("Using exchange rate: 1 EUR = {} {}", rate.getRate(), currency);
        
        // Convert to EUR (divide by rate since rate represents foreign currency per EUR)
        BigDecimal result = amount.divide(rate.getRate(), 2, RoundingMode.HALF_UP);
        logger.debug("Conversion result: {} {} = {} EUR (rate: {})", amount, currency, result, rate.getRate());

        return new ConversionResultDTO(
                currency,
                amount,
                rate.getRate(),
                result,
                date
        );
    }

    public List<ExchangeRate> getExchangeRatesForCurrency(String currency) {
        logger.debug("Fetching exchange rates for currency: {}", currency);
        validateCurrency(currency);
        List<ExchangeRate> rates = repository.findByCurrencyOrderByDateDesc(currency);
        if (rates.isEmpty()) {
            logger.error("No exchange rates found for currency: {}", currency);
            throw new ExchangeRateException("NO_RATES_FOUND", 
                String.format("No exchange rates found for currency: %s", currency));
        }
        logger.debug("Found {} exchange rates for currency: {}", rates.size(), currency);
        return rates;
    }

    public ExchangeRate getExchangeRateForDate(String currency, LocalDate date) {
        logger.debug("Fetching exchange rate for currency: {} on date: {}", currency, date);
        validateDate(date);  // Check for future date first
        validateCurrency(currency);
        
        Optional<ExchangeRate> rate = repository.findByCurrencyAndDate(currency, date);
        if (rate.isEmpty()) {
            logger.error("No exchange rate found for currency {} on date {}", currency, date);
            throw new ExchangeRateException("RATE_NOT_FOUND",
                String.format("No exchange rate found for currency %s on date %s", currency, date));
        }
        return rate.get();
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.length() != 3) {
            logger.error("Invalid currency code format: {}", currency);
            throw new IllegalArgumentException("Currency code must be 3 characters long");
        }
        // EUR is always valid as it's our base currency
        if (!"EUR".equals(currency) && !currencyService.isValidCurrency(currency)) {
            logger.error("Invalid currency code: {}", currency);
            throw new ExchangeRateException("INVALID_CURRENCY", 
                String.format("Invalid currency code: %s", currency));
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            logger.error("Date cannot be null");
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            logger.error("Cannot fetch exchange rate for future date: {}", date);
            throw new ExchangeRateException("FUTURE_DATE",
                String.format("Cannot fetch exchange rate for future date: %s", date));
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            logger.error("Amount cannot be null");
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Amount must be greater than zero: {}", amount);
            throw new ExchangeRateException("INVALID_AMOUNT",
                "Amount must be greater than zero");
        }
    }

    private ExchangeRateDTO convertToDTO(ExchangeRate exchangeRate) {
        return new ExchangeRateDTO(
                exchangeRate.getCurrency(),
                exchangeRate.getDate(),
                exchangeRate.getRate()
        );
    }
} 