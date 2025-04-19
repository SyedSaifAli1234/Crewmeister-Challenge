package com.crewmeister.cmcodingchallenge.service;

import com.crewmeister.cmcodingchallenge.domain.Currency;
import com.crewmeister.cmcodingchallenge.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private static final String BUNDESBANK_CURRENCY_URL = 
        "https://api.statistiken.bundesbank.de/rest/data/BBEX3/D..EUR.BB.AC.000?detail=serieskeyonly&format=csv";
    
    // Pattern to match currency codes in the format BBEX3.D.XXX.EUR.BB.AC.000
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("BBEX3\\.D\\.(\\w{3})\\.EUR\\.BB\\.AC\\.000");

    @Autowired
    private CurrencyRepository currencyRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    public CurrencyService() {
    }

    @PostConstruct
    @Transactional
    @Profile("!test") // This method will not run when the "test" profile is active
    public void initializeCurrencies() {
        if (currencyRepository.count() == 0) {
            logger.info("Initializing currency data...");
            updateCurrencies();
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    @Profile("!test") // This method will not run when the "test" profile is active
    public void updateCurrencies() {
        try {
            String csvResponse = restTemplate.getForObject(BUNDESBANK_CURRENCY_URL, String.class);
            List<Currency> currencies = parseCSVResponse(csvResponse);
            currencyRepository.saveAll(currencies);
            logger.info("Successfully updated {} currencies", currencies.size());
        } catch (Exception e) {
            logger.error("Failed to update currencies: {}", e.getMessage());
        }
    }

    List<Currency> parseCSVResponse(String csvContent) {
        List<Currency> currencies = new ArrayList<>();
        if (csvContent == null || csvContent.isEmpty()) {
            return currencies;
        }

        // Get the first line which contains all the currency codes
        String headerLine = csvContent.split("\n")[0];
        
        // Find all currency codes using regex
        Matcher matcher = CURRENCY_PATTERN.matcher(headerLine);
        Set<String> uniqueCodes = new HashSet<>();
        while (matcher.find()) {
            String currencyCode = matcher.group(1);
            // Skip invalid entries: empty, _FLAGS, or non-standard currency codes
            if (isValidCurrencyFormat(currencyCode) && !headerLine.contains(currencyCode + "_FLAGS")) {
                uniqueCodes.add(currencyCode);
            }
        }

        // Convert unique codes to Currency objects
        currencies.addAll(uniqueCodes.stream()
            .map(Currency::new)
            .collect(Collectors.toList()));

        return currencies;
    }

    boolean isValidCurrencyFormat(String currencyCode) {
        // Currency code must be exactly 3 uppercase letters
        return currencyCode != null && currencyCode.matches("[A-Z]{3}");
    }

    @Cacheable(value = "currencies")
    public List<String> getAllCurrencies() {
        logger.debug("Fetching all currencies");
        List<String> currencies = currencyRepository.findAll().stream()
                .map(Currency::getCode)
                .filter(this::isValidCurrencyFormat)  // Only return currencies with valid format
                .collect(Collectors.toList());
        logger.debug("Found {} valid currencies", currencies.size());
        return currencies;
    }

    public boolean isValidCurrency(String currencyCode) {
        return currencyCode != null && 
               isValidCurrencyFormat(currencyCode) && 
               currencyRepository.existsById(currencyCode);
    }
} 