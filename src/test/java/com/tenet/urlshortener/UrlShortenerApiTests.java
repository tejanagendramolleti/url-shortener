package com.tenet.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenet.urlshortener.controller.UrlShortenerController;
import com.tenet.urlshortener.dto.ShortenUrlRequest;
import com.tenet.urlshortener.exception.GlobalExceptionHandler;
import com.tenet.urlshortener.model.UrlMapping;
import com.tenet.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class UrlShortenerApiTests {

    private MockMvc mockMvc;

    @Autowired
    private UrlShortenerController controller;

    @Autowired
    private UrlMappingRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testShortenUrlPostApiWithDescription() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/long/path", null, "Example Test Description");

        mockMvc.perform(post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortKey", notNullValue()))
                .andExpect(jsonPath("$.originalUrl", is("https://example.com/long/path")))
                .andExpect(jsonPath("$.description", is("Example Test Description")));
    }

    @Test
    void testGetPaginatedUrlsApi() throws Exception {
        repository.save(new UrlMapping("key1", "https://site1.com", "First Link"));
        repository.save(new UrlMapping("key2", "https://site2.com", "Second Link"));

        mockMvc.perform(get("/api/urls?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void testSearchPaginatedUrlsApi() throws Exception {
        repository.save(new UrlMapping("key1", "https://site1.com", "Alpha Document"));
        repository.save(new UrlMapping("key2", "https://site2.com", "Beta Report"));

        mockMvc.perform(get("/api/urls?page=0&size=10&search=Alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description", is("Alpha Document")));
    }

    @Test
    void testGetUrlDetailsApi() throws Exception {
        repository.save(new UrlMapping("testkey123", "https://google.com", "Google Search"));

        mockMvc.perform(get("/api/urls/testkey123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortKey", is("testkey123")))
                .andExpect(jsonPath("$.originalUrl", is("https://google.com")))
                .andExpect(jsonPath("$.description", is("Google Search")));
    }

    @Test
    void testRedirectToOriginalUrlApi() throws Exception {
        repository.save(new UrlMapping("redirectKey", "https://github.com", "GitHub Repository"));

        mockMvc.perform(get("/redirectKey"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://github.com"));
    }
}
