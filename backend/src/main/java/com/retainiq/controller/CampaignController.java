package com.retainiq.controller;

import com.retainiq.dto.CampaignGenerateResponse;
import com.retainiq.dto.DashboardStats;
import com.retainiq.model.CampaignLog;
import com.retainiq.model.Customer;
import com.retainiq.service.CampaignService;
import com.retainiq.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private static final Logger log = LoggerFactory.getLogger(CampaignController.class);

    private final CampaignService campaignService;
    private final CustomerService customerService;

    public CampaignController(CampaignService campaignService, CustomerService customerService) {
        this.campaignService = campaignService;
        this.customerService = customerService;
    }

    /**
     * POST /api/campaigns/generate/{customerId} — Generate a personalized campaign for a customer.
     */
    @PostMapping("/generate/{customerId}")
    public ResponseEntity<?> generateCampaign(@PathVariable String customerId) {
        Optional<Customer> optCustomer = customerService.getCustomerById(customerId);
        if (optCustomer.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer not found: " + customerId);
            return ResponseEntity.status(404).body(error);
        }

        CampaignGenerateResponse response = campaignService.generateCampaign(optCustomer.get());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/campaigns/send/{campaignId} — Simulate sending a campaign.
     */
    @PostMapping("/send/{campaignId}")
    public ResponseEntity<?> sendCampaign(@PathVariable String campaignId) {
        CampaignLog result = campaignService.sendCampaign(campaignId);
        if (result == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Campaign not found: " + campaignId);
            return ResponseEntity.status(404).body(error);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/campaigns/logs — Get all campaign logs.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<CampaignLog>> getCampaignLogs() {
        List<CampaignLog> logs = campaignService.getCampaignLogs();
        log.info("Returning {} campaign logs", logs.size());
        return ResponseEntity.ok(logs);
    }

    /**
     * GET /api/campaigns/stats — Get dashboard statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        DashboardStats stats = campaignService.getCampaignStats();
        return ResponseEntity.ok(stats);
    }
}
