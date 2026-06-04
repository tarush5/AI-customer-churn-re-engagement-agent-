package com.retainiq.controller;

import com.retainiq.dto.ChurnPredictionResponse;
import com.retainiq.model.Customer;
import com.retainiq.service.ChurnService;
import com.retainiq.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final ChurnService churnService;

    public CustomerController(CustomerService customerService, ChurnService churnService) {
        this.customerService = customerService;
        this.churnService = churnService;
    }

    /**
     * GET /api/customers — List all customers.
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        log.info("Returning {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    /**
     * GET /api/customers/{customerId} — Get a single customer by their customerId.
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId) {
        Optional<Customer> customer = customerService.getCustomerById(customerId);
        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Customer not found: " + customerId);
        return ResponseEntity.status(404).body(error);
    }

    /**
     * POST /api/customers/ingest — Re-trigger CSV reload into H2.
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestCustomers() {
        log.info("Triggering CSV re-ingestion...");
        customerService.loadCustomersFromCsv();
        long count = customerService.getAllCustomers().size();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "CSV ingestion complete");
        result.put("totalCustomers", count);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/customers/{customerId}/analyze — Run churn analysis for one customer.
     */
    @PostMapping("/{customerId}/analyze")
    public ResponseEntity<?> analyzeCustomer(@PathVariable String customerId) {
        Optional<Customer> optCustomer = customerService.getCustomerById(customerId);
        if (optCustomer.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer not found: " + customerId);
            return ResponseEntity.status(404).body(error);
        }

        ChurnPredictionResponse response = churnService.analyzeCustomer(optCustomer.get());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/customers/analyze-all — Run churn analysis for ALL customers (batch).
     */
    @PostMapping("/analyze-all")
    public ResponseEntity<Map<String, Object>> analyzeAllCustomers() {
        log.info("Starting batch churn analysis for all customers...");
        List<Customer> customers = customerService.getAllCustomers();
        List<ChurnPredictionResponse> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Customer customer : customers) {
            try {
                ChurnPredictionResponse response = churnService.analyzeCustomer(customer);
                if (response != null) {
                    results.add(response);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("Failed to analyze customer {}: {}", customer.getCustomerId(), e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProcessed", customers.size());
        summary.put("successful", successCount);
        summary.put("failed", failCount);
        summary.put("results", results);

        log.info("Batch analysis complete. Success: {}, Failed: {}", successCount, failCount);
        return ResponseEntity.ok(summary);
    }
}
