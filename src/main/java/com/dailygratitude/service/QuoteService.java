package com.dailygratitude.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.dailygratitude.model.Quote;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuoteService {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random;
    
    // Fallback quotes in case API is unavailable
    private final List<Quote> fallbackQuotes = Arrays.asList(
        new Quote("Gratitude turns what we have into enough.", "Anonymous"),
        new Quote("The unthankful heart discovers no mercies; but the thankful heart will find, in every hour, some heavenly blessings.", "Henry Ward Beecher"),
        new Quote("Gratitude is not only the greatest of virtues but the parent of all others.", "Cicero"),
        new Quote("Be thankful for what you have; you'll end up having more.", "Oprah Winfrey"),
        new Quote("Gratitude makes sense of our past, brings peace for today, and creates a vision for tomorrow.", "Melody Beattie"),
        new Quote("Reflect upon your present blessings, of which every man has many - not on your past misfortunes, of which all men have some.", "Charles Dickens"),
        new Quote("Give thanks not just on Thanksgiving Day, but every day of your life.", "Catherine Pulsifer"),
        new Quote("Gratitude is a powerful catalyst for happiness. It's the spark that lights a fire of joy in your soul.", "Amy Collette"),
        new Quote("Count your blessings, not your problems.", "Roy T. Bennett"),
        new Quote("Gratitude is the fairest blossom which springs from the soul.", "Henry Ward Beecher"),
        new Quote("Every day is a gift. Be grateful for today.", "DailyGratitude")
    );
    
    public QuoteService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
    }
    
    /**
     * Gets today's inspirational quote from API or fallback
     * @return Quote object with text and author
     */
    public Quote getTodaysQuote() {
        try {
            // Try ZenQuotes API first (daily quote)
            Quote quote = fetchFromZenQuotes();
            if (quote != null) {
                return quote;
            }
            
            // Fallback to Quotable.io (random quote)
            quote = fetchFromQuotable();
            if (quote != null) {
                return quote;
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching quote from API: " + e.getMessage());
        }
        
        // Use fallback quote if all APIs fail
        return getFallbackQuote();
    }
    
    /**
     * Get a random quote (useful for testing or refresh functionality)
     */
    public Quote getRandomQuote() {
        try {
            Quote quote = fetchFromQuotable();
            if (quote != null) {
                return quote;
            }
        } catch (Exception e) {
            System.err.println("Error fetching random quote: " + e.getMessage());
        }
        
        return getFallbackQuote();
    }
    
    /**
     * Fetch quote from ZenQuotes API (same quote all day)
     */
    private Quote fetchFromZenQuotes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://zenquotes.io/api/today"))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "DailyGratitude/1.0")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseZenQuotesResponse(response.body());
            }
            
        } catch (Exception e) {
            System.err.println("ZenQuotes API error: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Fetch quote from Quotable.io API (random quote each time)
     */
    private Quote fetchFromQuotable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.quotable.io/random?tags=motivational,inspirational"))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "DailyGratitude/1.0")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseQuotableResponse(response.body());
            }
            
        } catch (Exception e) {
            System.err.println("Quotable API error: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parse ZenQuotes JSON response
     * Expected format: [{"q": "quote text", "a": "author", "h": "html"}]
     */
    private Quote parseZenQuotesResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode quoteNode = rootNode.get(0);
                String text = quoteNode.get("q").asText();
                String author = quoteNode.get("a").asText();
                
                return new Quote(text, author);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing ZenQuotes response: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parse Quotable.io JSON response  
     * Expected format: {"content": "quote text", "author": "author name"}
     */
    private Quote parseQuotableResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String text = rootNode.get("content").asText();
            String author = rootNode.get("author").asText();
            
            return new Quote(text, author);
            
        } catch (Exception e) {
            System.err.println("Error parsing Quotable response: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get a random fallback quote when APIs are unavailable
     */
    private Quote getFallbackQuote() {
        int index = random.nextInt(fallbackQuotes.size());
        return fallbackQuotes.get(index);
    }
}