package com.crewmeister.cmcodingchallenge.controller;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import com.crewmeister.cmcodingchallenge.dto.ConversionResultDTO;
import com.crewmeister.cmcodingchallenge.facade.CurrencyFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for handling exchange rate operations.
 * 
 * Security Considerations:
 * In a production environment, I would:
 * - Implement proper authentication and authorization to protect the API endpoints
 * - Add input validation to prevent potential injection attacks
 * - Set up rate limiting to protect against abuse
 * - Configure comprehensive logging for security auditing
 * - Implement proper CORS policies
 * - Enforce HTTPS for all endpoints
 * 
 * Note: These security measures are not implemented in this demo as they're beyond the current requirements.
 */
@RestController
@RequestMapping("/api/exchange-rates")
@Tag(name = "Exchange Rates", description = "Operations related to EUR exchange rates")
public class ExchangeRateController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateController.class);
    private final CurrencyFacade currencyFacade;

    public ExchangeRateController(CurrencyFacade currencyFacade) {
        this.currencyFacade = currencyFacade;
        logger.info("ExchangeRateController initialized");
    }

    @GetMapping
    @Operation(summary = "Get all exchange rates for a currency", description = "Returns a list of all available EUR exchange rates for a specific currency across all dates.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ExchangeRate.class))))
    @ApiResponse(responseCode = "400", description = "Invalid currency code supplied", content = @Content)
    public ResponseEntity<List<ExchangeRate>> getExchangeRates(@Parameter(description = "3-letter ISO currency code", required = true, example = "USD") @RequestParam String currency) {
        logger.debug("Received request to get exchange rates for currency: {}", currency);
        try {
            List<ExchangeRate> rates = currencyFacade.getExchangeRatesForCurrency(currency);
            if (rates == null || rates.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid currency code: " + currency);
            }
            logger.debug("Returning {} exchange rates for currency: {}", rates.size(), currency);
            return ResponseEntity.ok(rates);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving exchange rates: " + e.getMessage());
        }
    }

    @GetMapping("/{date}")
    @Operation(summary = "Get exchange rate for a specific date", description = "Returns the EUR exchange rate for a specific currency on a particular date.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rate",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeRate.class)))
    @ApiResponse(responseCode = "400", description = "Invalid currency code or date format supplied", content = @Content)
    @ApiResponse(responseCode = "404", description = "Exchange rate not found for the given currency and date", content = @Content)
    public ResponseEntity<ExchangeRate> getExchangeRateForDate(
            @Parameter(description = "Date in YYYY-MM-DD format", required = true, example = "2023-10-26") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "3-letter ISO currency code", required = true, example = "USD") @RequestParam String currency) {
        logger.debug("Received request to get exchange rate for currency: {} on date: {}", currency, date);
        try {
            ExchangeRate rate = currencyFacade.getExchangeRateForDate(currency, date);
            if (rate == null) {
                if (date.isAfter(LocalDate.now())) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No exchange rate available for future date: " + date);
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid currency code: " + currency);
            }
            logger.debug("Returning exchange rate: {} for currency: {} on date: {}", rate.getRate(), currency, date);
            return ResponseEntity.ok(rate);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving exchange rate: " + e.getMessage());
        }
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert an amount from a foreign currency to EUR", description = "Converts a given amount of a specified foreign currency into EUR based on the exchange rate of a particular date.")
    @ApiResponse(responseCode = "200", description = "Successfully converted currency",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConversionResultDTO.class))) 
    @ApiResponse(responseCode = "400", description = "Invalid currency code, amount, or date format supplied", content = @Content)
    @ApiResponse(responseCode = "404", description = "Exchange rate not found for the given currency and date", content = @Content)
    public ResponseEntity<ConversionResultDTO> convertCurrency(
            @Parameter(description = "3-letter ISO currency code of the source currency", required = true, example = "USD") @RequestParam String currency,
            @Parameter(description = "Amount of the source currency to convert", required = true, example = "100.50") @RequestParam BigDecimal amount,
            @Parameter(description = "Date in YYYY-MM-DD format for the exchange rate", required = true, example = "2023-10-26") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.debug("Received conversion request: {} {} to EUR on date: {}", amount, currency, date);
        try {
            ConversionResultDTO result = currencyFacade.convertToEur(currency, amount, date);
            logger.debug("Conversion result: {} {} = {} EUR", amount, currency, result.getConvertedAmount());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error converting currency: " + e.getMessage());
        }
    }
} 