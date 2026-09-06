package com.tenet.urlshortener.service;

import com.tenet.urlshortener.dto.ShortenUrlRequest;
import com.tenet.urlshortener.dto.ShortenUrlResponse;
import com.tenet.urlshortener.exception.DuplicateKeyException;
import com.tenet.urlshortener.exception.UrlNotFoundException;
import com.tenet.urlshortener.model.UrlMapping;
import com.tenet.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;

@Service
public class UrlShortenerService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 6;

    private final UrlMappingRepository urlMappingRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.base-url:http://192.168.1.2:8080}")
    private String baseUrl;

    public UrlShortenerService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @Transactional
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        String shortKey;

        if (request.getCustomKey() != null && !request.getCustomKey().trim().isEmpty()) {
            shortKey = request.getCustomKey().trim();
            if (urlMappingRepository.existsByShortKey(shortKey)) {
                throw new DuplicateKeyException("Custom key '" + shortKey + "' is already in use.");
            }
        } else {
            shortKey = generateUniqueShortKey();
        }

        String desc = request.getDescription() != null ? request.getDescription().trim() : null;
        UrlMapping urlMapping = new UrlMapping(shortKey, request.getOriginalUrl().trim(), desc);
        UrlMapping savedMapping = urlMappingRepository.save(urlMapping);

        return mapToResponse(savedMapping);
    }

    @Transactional(readOnly = true)
    public ShortenUrlResponse getUrlByShortKey(String shortKey) {
        UrlMapping mapping = urlMappingRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with key '" + shortKey + "' not found."));
        return mapToResponse(mapping);
    }

    @Transactional(readOnly = true)
    public Page<ShortenUrlResponse> getPaginatedUrls(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UrlMapping> mappingsPage = urlMappingRepository.searchMappings(search, pageable);
        return mappingsPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortKey) {
        UrlMapping mapping = urlMappingRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with key '" + shortKey + "' not found."));
        return mapping.getOriginalUrl();
    }

    private String generateUniqueShortKey() {
        String key;
        int attempts = 0;
        do {
            key = generateRandomKey();
            attempts++;
            if (attempts > 10) {
                key = generateRandomKey() + generateRandomKey();
            }
        } while (urlMappingRepository.existsByShortKey(key));

        return key;
    }

    private String generateRandomKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private ShortenUrlResponse mapToResponse(UrlMapping mapping) {
        String base;
        try {
            base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            base = baseUrl;
        }
        String fullShortUrl = base + "/" + mapping.getShortKey();
        return new ShortenUrlResponse(
                mapping.getShortKey(),
                fullShortUrl,
                mapping.getOriginalUrl(),
                mapping.getDescription(),
                mapping.getCreatedAt()
        );
    }
}
