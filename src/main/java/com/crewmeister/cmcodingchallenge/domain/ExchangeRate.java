package com.crewmeister.cmcodingchallenge.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "exchange_rates",
    indexes = {
        @Index(name = "idx_currency", columnList = "currency"),
        @Index(name = "idx_date", columnList = "date"),
        @Index(name = "idx_currency_date", columnList = "currency,date", unique = true)
    })
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(nullable = false, length = 3)
    private final String currency;

    @Column(nullable = false)
    private final LocalDate date;

    @Column(nullable = false, precision = 19, scale = 4)
    private final BigDecimal rate;

    protected ExchangeRate() {
        // Required by JPA
        this.id = null;
        this.currency = null;
        this.date = null;
        this.rate = null;
    }

    public ExchangeRate(String currency, LocalDate date, BigDecimal rate) {
        this(null, currency, date, rate);
    }

    public ExchangeRate(Long id, String currency, LocalDate date, BigDecimal rate) {
        this.id = id;
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.date = Objects.requireNonNull(date, "Date must not be null");
        this.rate = Objects.requireNonNull(rate, "Rate must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getRate() {
        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRate)) return false;
        ExchangeRate that = (ExchangeRate) o;
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
        return "ExchangeRate{" +
               "currency='" + currency + '\'' +
               ", date=" + date +
               ", rate=" + rate +
               '}';
    }
} 