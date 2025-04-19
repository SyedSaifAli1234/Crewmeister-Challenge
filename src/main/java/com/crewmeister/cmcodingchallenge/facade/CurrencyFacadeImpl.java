package com.crewmeister.cmcodingchallenge.facade;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.service.CurrencyService;
import com.crewmeister.cmcodingchallenge.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CurrencyFacadeImpl implements CurrencyFacade {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyFacadeImpl.class);
    
    private final ExchangeRateService exchangeRateService;
    private final CurrencyService currencyService;

    public CurrencyFacadeImpl(ExchangeRateService exchangeRateService, CurrencyService currencyService) {
        this.exchangeRateService = exchangeRateService;
        this.currencyService = currencyService;
        logger.info("CurrencyFacade initialized");
    }

    @Override
    public List<String> getAllCurrencies() {
        logger.debug("Getting all currencies");
        return currencyService.getAllCurrencies();
    }

    @Override
    public List<ExchangeRate> getExchangeRatesForCurrency(String currency) {
        logger.debug("Getting exchange rates for currency: {}", currency);
        return exchangeRateService.getExchangeRatesForCurrency(currency);
    }

    @Override
    public ExchangeRate getExchangeRateForDate(String currency, LocalDate date) {
        logger.debug("Getting exchange rate for currency: {} on date: {}", currency, date);
        return exchangeRateService.getExchangeRateForDate(currency, date);
    }

    @Override
    public ConversionResultDTO convertToEur(String currency, BigDecimal amount, LocalDate date) {
        logger.debug("Converting {} {} to EUR on date: {}", amount, currency, date);
        return exchangeRateService.convertCurrency(currency, amount, date);
    }
} 