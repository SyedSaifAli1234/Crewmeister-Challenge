package com.crewmeister.cmcodingchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class ConversionResultDTO {
    private final String fromCurrency;
    private final BigDecimal amount;
    private final BigDecimal rate;
    private final BigDecimal convertedAmount;
    private final LocalDate date;

    public ConversionResultDTO(String fromCurrency, BigDecimal amount, BigDecimal rate,
            BigDecimal convertedAmount, LocalDate date) {
        this.fromCurrency = Objects.requireNonNull(fromCurrency, "Currency must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.rate = Objects.requireNonNull(rate, "Rate must not be null");
        this.convertedAmount = Objects.requireNonNull(convertedAmount, "Converted amount must not be null");
        this.date = Objects.requireNonNull(date, "Date must not be null");
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversionResultDTO)) return false;
        ConversionResultDTO that = (ConversionResultDTO) o;
        return Objects.equals(fromCurrency, that.fromCurrency) &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(rate, that.rate) &&
               Objects.equals(convertedAmount, that.convertedAmount) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, amount, rate, convertedAmount, date);
    }

    @Override
    public String toString() {
        return "ConversionResultDTO{" +
               "fromCurrency='" + fromCurrency + '\'' +
               ", amount=" + amount +
               ", rate=" + rate +
               ", convertedAmount=" + convertedAmount +
               ", date=" + date +
               '}';
    }
} 