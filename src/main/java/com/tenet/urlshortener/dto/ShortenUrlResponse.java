package com.tenet.urlshortener.dto;

import java.time.LocalDateTime;

public class ShortenUrlResponse {

    private String shortKey;
    private String shortUrl;
    private String originalUrl;
    private String description;
    private LocalDateTime createdAt;

    public ShortenUrlResponse() {
    }

    public ShortenUrlResponse(String shortKey, String shortUrl, String originalUrl, LocalDateTime createdAt) {
        this(shortKey, shortUrl, originalUrl, null, createdAt);
    }

    public ShortenUrlResponse(String shortKey, String shortUrl, String originalUrl, String description, LocalDateTime createdAt) {
        this.shortKey = shortKey;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getShortKey() {
        return shortKey;
    }

    public void setShortKey(String shortKey) {
        this.shortKey = shortKey;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
