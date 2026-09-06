package com.tenet.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class ShortenUrlRequest {

    @NotBlank(message = "Original URL must not be blank")
    @URL(message = "Invalid URL format")
    private String originalUrl;

    private String customKey;

    private String description;

    public ShortenUrlRequest() {
    }

    public ShortenUrlRequest(String originalUrl, String customKey) {
        this(originalUrl, customKey, null);
    }

    public ShortenUrlRequest(String originalUrl, String customKey, String description) {
        this.originalUrl = originalUrl;
        this.customKey = customKey;
        this.description = description;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomKey() {
        return customKey;
    }

    public void setCustomKey(String customKey) {
        this.customKey = customKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
