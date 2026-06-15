package com.internship.portal.service;

import com.internship.portal.entity.Quote;
import com.internship.portal.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quoteRepository;

    public String getRandomQuote() {
        return quoteRepository.findRandomActiveQuote()
                .map(Quote::getText)
                .orElse("Code. Debug. Repeat.");
    }

    public List<Quote> getAllActiveQuotes() {
        return quoteRepository.findByActiveTrue();
    }
}
