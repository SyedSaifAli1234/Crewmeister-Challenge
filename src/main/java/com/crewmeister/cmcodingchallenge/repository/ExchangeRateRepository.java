package com.crewmeister.cmcodingchallenge.repository;

import com.crewmeister.cmcodingchallenge.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findByCurrencyOrderByDateDesc(String currency);
    
    Optional<ExchangeRate> findByCurrencyAndDate(String currency, LocalDate date);
    
    List<ExchangeRate> findByCurrency(String currency);
    
    @Query("SELECT DISTINCT e.currency FROM ExchangeRate e")
    List<String> findDistinctCurrencies();
    
    Optional<ExchangeRate> findFirstByCurrencyOrderByDateDesc(String currency);
} 