package com.crewmeister.cmcodingchallenge.repository;

import com.crewmeister.cmcodingchallenge.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
} 