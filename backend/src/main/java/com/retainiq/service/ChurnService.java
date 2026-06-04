package com.retainiq.service;

import com.retainiq.dto.ChurnPredictionRequest;
import com.retainiq.dto.ChurnPredictionResponse;
import com.retainiq.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
public class ChurnService {

    private static final Logger log = LoggerFactory.getLogger(ChurnService.class);

    private final WebClient webClient;
    private final CustomerService customerService;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public ChurnService(WebClient.Builder webClientBuilder, CustomerService customerService) {
        this.webClient = webClientBuilder.build();
        this.customerService = customerService;
    }

    /**
     * Calls the Python ML service to predict churn probability for a customer.
     * Updates the customer's churn score and risk level in the database.
     */
    public ChurnPredictionResponse analyzeCustomer(Customer customer) {
        log.info("Analyzing churn for customer: {}", customer.getCustomerId());

        ChurnPredictionRequest request = new ChurnPredictionRequest(
                customer.getCustomerId(),
                customer.getRecency(),
                customer.getFrequency(),
                customer.getMonetary(),
                customer.getLoyaltyPoints(),
                customer.getAge()
        );

        try {
            ChurnPredictionResponse response = webClient.post()
                    .uri(mlServiceUrl + "/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChurnPredictionResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null) {
                // Update customer in H2 with the prediction results
                customerService.updateChurnScore(
                        customer.getCustomerId(),
                        response.getChurnProbability(),
                        response.getRiskLevel()
                );
                log.info("Churn analysis complete for {}: score={}, risk={}",
                        customer.getCustomerId(),
                        response.getChurnProbability(),
                        response.getRiskLevel());
            }

            return response;

        } catch (WebClientRequestException e) {
            log.error("ML service unreachable at {}: {}", mlServiceUrl, e.getMessage());
            return createFallbackResponse(customer, "ML service is unreachable. Please ensure the Python service is running at " + mlServiceUrl);

        } catch (WebClientResponseException e) {
            log.error("ML service returned error for {}: {} - {}",
                    customer.getCustomerId(), e.getStatusCode(), e.getResponseBodyAsString());
            return createFallbackResponse(customer, "ML service error: " + e.getStatusCode());

        } catch (Exception e) {
            log.error("Unexpected error during churn analysis for {}: {}",
                    customer.getCustomerId(), e.getMessage(), e);
            return createFallbackResponse(customer, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Creates a fallback response when the ML service is unavailable.
     * Uses a simple heuristic based on recency and frequency.
     */
    private ChurnPredictionResponse createFallbackResponse(Customer customer, String reason) {
        log.warn("Using fallback churn prediction for {}: {}", customer.getCustomerId(), reason);

        ChurnPredictionResponse fallback = new ChurnPredictionResponse();
        fallback.setCustomerId(customer.getCustomerId());

        // Simple heuristic: high recency + low frequency = high churn risk
        double score = 0.0;
        if (customer.getRecency() > 90) score += 0.4;
        else if (customer.getRecency() > 30) score += 0.2;

        if (customer.getFrequency() <= 1) score += 0.3;
        else if (customer.getFrequency() <= 3) score += 0.15;

        if (customer.getMonetary() < 2000) score += 0.2;
        else if (customer.getMonetary() < 5000) score += 0.1;

        score = Math.min(score, 1.0);
        fallback.setChurnProbability(Math.round(score * 100.0) / 100.0);

        if (score >= 0.7) fallback.setRiskLevel("HIGH");
        else if (score >= 0.4) fallback.setRiskLevel("MEDIUM");
        else fallback.setRiskLevel("LOW");

        fallback.setRiskFactors("Fallback prediction — " + reason);
        fallback.setRecommendedAction("Connect ML service for accurate predictions");

        // Still update the customer record with fallback scores
        customerService.updateChurnScore(
                customer.getCustomerId(),
                fallback.getChurnProbability(),
                fallback.getRiskLevel()
        );

        return fallback;
    }
}
