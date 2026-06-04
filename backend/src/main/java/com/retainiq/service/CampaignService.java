package com.retainiq.service;

import com.retainiq.dto.CampaignGenerateRequest;
import com.retainiq.dto.CampaignGenerateResponse;
import com.retainiq.dto.DashboardStats;
import com.retainiq.model.CampaignLog;
import com.retainiq.model.Customer;
import com.retainiq.repository.CampaignLogRepository;
import com.retainiq.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private final WebClient webClient;
    private final CampaignLogRepository campaignLogRepository;
    private final CustomerRepository customerRepository;
    private final Random random = new Random();

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public CampaignService(WebClient.Builder webClientBuilder,
                           CampaignLogRepository campaignLogRepository,
                           CustomerRepository customerRepository) {
        this.webClient = webClientBuilder.build();
        this.campaignLogRepository = campaignLogRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Calls the Python ML service to generate a personalized campaign for a customer.
     */
    public CampaignGenerateResponse generateCampaign(Customer customer) {
        log.info("Generating campaign for customer: {}", customer.getCustomerId());

        CampaignGenerateRequest request = new CampaignGenerateRequest(
                customer.getCustomerId(),
                customer.getName(),
                customer.getChurnScore() != null ? customer.getChurnScore() : 0.5,
                customer.getRiskLevel() != null ? customer.getRiskLevel() : "UNKNOWN",
                customer.getPreferredChannel(),
                customer.getRecency(),
                customer.getFrequency(),
                customer.getMonetary(),
                customer.getLoyaltyPoints()
        );

        try {
            CampaignGenerateResponse response = webClient.post()
                    .uri(mlServiceUrl + "/generate-campaign")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CampaignGenerateResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null) {
                // Log the campaign
                CampaignLog campaignLog = logCampaign(customer, response);
                log.info("Campaign generated and logged: {} for customer {}",
                        campaignLog.getId(), customer.getCustomerId());
            }

            return response;

        } catch (WebClientRequestException e) {
            log.error("ML service unreachable for campaign generation: {}", e.getMessage());
            return createFallbackCampaign(customer,
                    "ML service unreachable. Please ensure the Python service is running at " + mlServiceUrl);

        } catch (WebClientResponseException e) {
            log.error("ML service error for campaign generation: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return createFallbackCampaign(customer, "ML service error: " + e.getStatusCode());

        } catch (Exception e) {
            log.error("Unexpected error during campaign generation: {}", e.getMessage(), e);
            return createFallbackCampaign(customer, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Logs a campaign into the in-memory store and returns the log entry.
     */
    public CampaignLog logCampaign(Customer customer, CampaignGenerateResponse response) {
        CampaignLog campaignLog = new CampaignLog(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                response.getCampaignType(),
                response.getSubject(),
                response.getMessage(),
                response.getChannel(),
                response.getStrategyReasoning()
        );

        return campaignLogRepository.save(campaignLog);
    }

    /**
     * Simulates sending a campaign. Updates status through lifecycle:
     * PENDING → SENT → (randomly) DELIVERED → OPENED → CLICKED
     */
    public CampaignLog sendCampaign(String campaignId) {
        Optional<CampaignLog> optLog = campaignLogRepository.findById(campaignId);
        if (optLog.isEmpty()) {
            log.warn("Campaign not found for sending: {}", campaignId);
            return null;
        }

        CampaignLog campaignLog = optLog.get();
        campaignLog.setStatus("SENT");
        campaignLog.setSentAt(LocalDateTime.now());

        // Simulate delivery and engagement with random probabilities
        if (random.nextDouble() < 0.85) { // 85% delivery rate
            campaignLog.setStatus("DELIVERED");

            if (random.nextDouble() < 0.45) { // 45% open rate
                campaignLog.setStatus("OPENED");
                campaignLog.setOpenedAt(LocalDateTime.now().plusMinutes(random.nextInt(120) + 5));

                if (random.nextDouble() < 0.25) { // 25% click rate
                    campaignLog.setStatus("CLICKED");
                    campaignLog.setClickedAt(LocalDateTime.now().plusMinutes(random.nextInt(180) + 10));
                }
            }
        }

        campaignLogRepository.save(campaignLog);
        log.info("Campaign {} sent. Final status: {}", campaignId, campaignLog.getStatus());
        return campaignLog;
    }

    /**
     * Returns all campaign logs from in-memory store.
     */
    public List<CampaignLog> getCampaignLogs() {
        return campaignLogRepository.findAll();
    }

    /**
     * Computes dashboard statistics from customer data and campaign logs.
     */
    public DashboardStats getCampaignStats() {
        List<Customer> allCustomers = customerRepository.findAll();
        long totalCustomers = allCustomers.size();

        long highRisk = allCustomers.stream()
                .filter(c -> "HIGH".equalsIgnoreCase(c.getRiskLevel()))
                .count();
        long mediumRisk = allCustomers.stream()
                .filter(c -> "MEDIUM".equalsIgnoreCase(c.getRiskLevel()))
                .count();
        long lowRisk = allCustomers.stream()
                .filter(c -> "LOW".equalsIgnoreCase(c.getRiskLevel()))
                .count();

        long campaignsSent = campaignLogRepository.count();

        double avgChurnScore = allCustomers.stream()
                .filter(c -> c.getChurnScore() != null)
                .mapToDouble(Customer::getChurnScore)
                .average()
                .orElse(0.0);
        avgChurnScore = Math.round(avgChurnScore * 100.0) / 100.0;

        // Re-engagement rate = campaigns that got OPENED or CLICKED / total campaigns sent
        long engagedCount = campaignLogRepository.countByStatus("OPENED")
                + campaignLogRepository.countByStatus("CLICKED");
        double reEngagementRate = campaignsSent > 0
                ? Math.round((double) engagedCount / campaignsSent * 100.0) / 100.0
                : 0.0;

        return new DashboardStats(
                totalCustomers, highRisk, mediumRisk, lowRisk,
                campaignsSent, avgChurnScore, reEngagementRate
        );
    }

    /**
     * Creates a fallback campaign when the ML service is unavailable.
     */
    private CampaignGenerateResponse createFallbackCampaign(Customer customer, String reason) {
        log.warn("Using fallback campaign for {}: {}", customer.getCustomerId(), reason);

        CampaignGenerateResponse fallback = new CampaignGenerateResponse();
        fallback.setCustomerId(customer.getCustomerId());
        fallback.setCampaignType("re-engagement");
        fallback.setSubject("We miss you, " + customer.getName() + "! Here's a special offer");
        fallback.setMessage("Hi " + customer.getName() +
                ", we noticed you haven't visited us in a while. " +
                "Come back and enjoy an exclusive 15% discount on your next purchase! " +
                "Use code: COMEBACK15. Valid for 7 days.");
        fallback.setChannel(customer.getPreferredChannel() != null
                ? customer.getPreferredChannel() : "email");
        fallback.setStrategyReasoning("Fallback campaign — " + reason);
        fallback.setUrgencyLevel("MEDIUM");
        fallback.setDiscountOffered("15%");

        // Still log the fallback campaign
        logCampaign(customer, fallback);

        return fallback;
    }
}
