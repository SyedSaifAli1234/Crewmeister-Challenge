package com.crewmeister.cmcodingchallenge.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BundesbankApiClient {
    private static final Logger logger = LoggerFactory.getLogger(BundesbankApiClient.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public static class ExchangeRateData {
        private final LocalDate date;
        private final BigDecimal rate;

        public ExchangeRateData(LocalDate date, BigDecimal rate) {
            this.date = date;
            this.rate = rate;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getRate() {
            return rate;
        }
    }

    public BundesbankApiClient(
            @Value("${bundesbank.api.base-url:https://api.statistiken.bundesbank.de/rest}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public List<ExchangeRateData> fetchExchangeRates(String currency) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/data/BBEX3/D.{currency}.EUR.BB.AC.000")
                .queryParam("format", "csv")
                .queryParam("lang", "en")
                .queryParam("detail", "dataonly")
                .buildAndExpand(currency)
                .toUriString();

        logger.info("Fetching exchange rates from Bundesbank API for currency: {}", currency);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return parseCSVResponse(response.getBody());
    }

    private List<ExchangeRateData> parseCSVResponse(String csvContent) {
        List<ExchangeRateData> rates = new ArrayList<>();
        if (csvContent == null || csvContent.isEmpty()) {
            return rates;
        }

        String[] lines = csvContent.split("\n");
        // Skip the first two lines (header and last update)
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",", -1); // -1 to keep empty trailing fields
            if (parts.length < 2) continue;

            // Skip comment lines (they start with empty date field)
            if (parts[0].equals("\"\"") || parts[0].contains("Comment")) {
                logger.debug("Skipping comment line: {}", line);
                continue;
            }

            try {
                String dateStr = parts[0].replace("\"", "").trim();
                String rateStr = parts[1].replace("\"", "").trim();

                // Skip if rate is not available (marked as ".")
                if (".".equals(rateStr)) {
                    continue;
                }

                try {
                    LocalDate date = LocalDate.parse(dateStr);
                    BigDecimal rate = new BigDecimal(rateStr);
                    rates.add(new ExchangeRateData(date, rate));
                } catch (DateTimeParseException e) {
                    logger.debug("Skipping invalid date format: {}", dateStr);
                } catch (NumberFormatException e) {
                    logger.debug("Skipping invalid rate format: {}", rateStr);
                }
            } catch (Exception e) {
                logger.debug("Failed to parse line: {}", line);
            }
        }
        
        logger.debug("Parsed {} exchange rates from CSV", rates.size());
        return rates;
    }
} 