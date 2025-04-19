package com.crewmeister.cmcodingchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class ExchangeRateDTO {
    private final String currency;
    private final LocalDate date;
    private final BigDecimal rate;

    public ExchangeRateDTO(String currency, LocalDate date, BigDecimal rate) {
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.date = Objects.requireNonNull(date, "Date must not be null");
        this.rate = Objects.requireNonNull(rate, "Rate must not be null");
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRateDTO)) return false;
        ExchangeRateDTO that = (ExchangeRateDTO) o;
        return Objects.equals(currency, that.currency) &&
               Objects.equals(date, that.date) &&
               Objects.equals(rate, that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, date, rate);
    }

    @Override
    public String toString() {
        return "ExchangeRateDTO{" +
               "currency='" + currency + '\'' +
               ", date=" + date +
               ", rate=" + rate +
               '}';
    }
} 