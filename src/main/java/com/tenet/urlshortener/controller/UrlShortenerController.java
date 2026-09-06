package com.tenet.urlshortener.controller;

import com.tenet.urlshortener.dto.ShortenUrlRequest;
import com.tenet.urlshortener.dto.ShortenUrlResponse;
import com.tenet.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * POST API to shorten a URL and save mapping in MySQL database table.
     * Endpoint: POST /api/urls/shorten or POST /api/urls
     */
    @PostMapping({"/api/urls/shorten", "/api/urls"})
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        ShortenUrlResponse response = urlShortenerService.shortenUrl(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET API to retrieve paginated list of shortened URLs.
     * Endpoint: GET /api/urls?page=0&size=10&search=
     */
    @GetMapping("/api/urls")
    public ResponseEntity<Page<ShortenUrlResponse>> getPaginatedUrls(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", required = false) String search) {
        Page<ShortenUrlResponse> response = urlShortenerService.getPaginatedUrls(page, size, search);
        return ResponseEntity.ok(response);
    }

    /**
     * GET API to retrieve shortened URL metadata using key from MySQL database table.
     * Endpoint: GET /api/urls/{shortKey}
     */
    @GetMapping("/api/urls/{shortKey:[^\\.]+}")
    public ResponseEntity<ShortenUrlResponse> getUrlDetails(@PathVariable(name = "shortKey") String shortKey) {
        String cleanKey = extractCleanKey(shortKey);
        ShortenUrlResponse response = urlShortenerService.getUrlByShortKey(cleanKey);
        return ResponseEntity.ok(response);
    }

    /**
     * GET API to redirect to original URL using key.
     * Excludes static files (index.html, styles.css, app.js, etc.) so Spring serves the Angular UI.
     * Endpoint: GET /{shortKey}
     */
    @GetMapping("/{shortKey:^(?!index\\.html$|styles\\.css$|app\\.js$|favicon\\.ico$)[^\\.]+$}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable(name = "shortKey") String shortKey) {
        String cleanKey = extractCleanKey(shortKey);
        String originalUrl = urlShortenerService.getOriginalUrl(cleanKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String extractCleanKey(String key) {
        if (key == null) return "";
        // If user typed 'shortKey:Vs4gk4', strip 'shortKey:' prefix
        if (key.startsWith("shortKey:")) {
            return key.substring("shortKey:".length()).trim();
        }
        return key.trim();
    }
}
