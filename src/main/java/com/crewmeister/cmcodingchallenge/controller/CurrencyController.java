package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.facade.CurrencyFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currencies", description = "Operations related to available currencies")
public class CurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);
    private final CurrencyFacade currencyFacade;

    public CurrencyController(CurrencyFacade currencyFacade) {
        this.currencyFacade = currencyFacade;
        logger.info("CurrencyController initialized");
    }

    @GetMapping
    @Operation(summary = "Get all available currencies", description = "Returns a list of all unique currency codes available for exchange rates.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of currencies", 
                content = @Content(mediaType = "application/json", 
                array = @ArraySchema(schema = @Schema(type = "string"))))
    @ApiResponse(responseCode = "500", description = "Internal server error occurred", 
                content = @Content(mediaType = "application/json"))
    public ResponseEntity<List<String>> getAllCurrencies() {
        try {
            logger.debug("Received request to get all currencies");
            List<String> currencies = currencyFacade.getAllCurrencies();
            
            if (currencies == null) {
                logger.error("Currency facade returned null");
                return ResponseEntity.status(500).build();
            }
            
            // Filter out null values and check if any were removed
            List<String> filteredCurrencies = currencies.stream()
                .filter(currency -> currency != null)
                .collect(Collectors.toList());
            
            if (filteredCurrencies.size() != currencies.size()) {
                logger.error("Currency list contained null values");
                return ResponseEntity.status(500).build();
            }
            
            logger.debug("Returning {} currencies", filteredCurrencies.size());
            return ResponseEntity.ok(filteredCurrencies);
        } catch (Exception e) {
            logger.error("Error occurred while fetching currencies", e);
            return ResponseEntity.status(500).build();
        }
    }
} 