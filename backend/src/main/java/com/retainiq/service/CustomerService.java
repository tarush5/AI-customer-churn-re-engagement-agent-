package com.retainiq.service;

import com.retainiq.model.Customer;
import com.retainiq.repository.CustomerRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Load customers from the bundled CSV on application startup.
     * Skips rows that already exist (by customerId) to avoid duplicates on re-ingestion.
     */
    @PostConstruct
    public void loadCustomersFromCsv() {
        log.info("Loading customers from CSV...");
        try {
            ClassPathResource resource = new ClassPathResource("data/customers.csv");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            String headerLine = reader.readLine(); // skip header
            if (headerLine == null) {
                log.warn("CSV file is empty");
                return;
            }

            String line;
            int loaded = 0;
            int skipped = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 12) {
                        log.warn("Skipping malformed CSV row: {}", line);
                        skipped++;
                        continue;
                    }

                    String customerId = parts[0].trim();

                    // Skip if customer already exists
                    if (customerRepository.findByCustomerId(customerId).isPresent()) {
                        skipped++;
                        continue;
                    }

                    Customer customer = new Customer();
                    customer.setCustomerId(customerId);
                    customer.setName(parts[1].trim());
                    customer.setEmail(parts[2].trim());
                    customer.setPhone(parts[3].trim());
                    customer.setAge(parseIntSafe(parts[4].trim(), 0));
                    customer.setRecency(parseIntSafe(parts[5].trim(), 0));
                    customer.setFrequency(parseIntSafe(parts[6].trim(), 0));
                    customer.setMonetary(parseDoubleSafe(parts[7].trim(), 0.0));
                    customer.setLoyaltyPoints(parseIntSafe(parts[8].trim(), 0));
                    customer.setLastPurchaseDate(parts[9].trim());
                    customer.setSignupDate(parts[10].trim());
                    customer.setPreferredChannel(parts[11].trim());

                    customerRepository.save(customer);
                    loaded++;
                } catch (Exception e) {
                    log.warn("Error parsing CSV row: {}. Error: {}", line, e.getMessage());
                    skipped++;
                }
            }

            reader.close();
            log.info("CSV loading complete. Loaded: {}, Skipped: {}, Total in DB: {}",
                    loaded, skipped, customerRepository.count());

        } catch (Exception e) {
            log.error("Failed to load customers from CSV: {}", e.getMessage(), e);
        }
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(String customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    public Customer updateChurnScore(String customerId, Double score, String riskLevel) {
        Optional<Customer> optCustomer = customerRepository.findByCustomerId(customerId);
        if (optCustomer.isPresent()) {
            Customer customer = optCustomer.get();
            customer.setChurnScore(score);
            customer.setRiskLevel(riskLevel);
            customerRepository.save(customer);
            log.info("Updated churn score for {}: score={}, risk={}", customerId, score, riskLevel);
            return customer;
        }
        log.warn("Customer not found for churn update: {}", customerId);
        return null;
    }

    /**
     * Simple CSV line parser that handles quoted fields containing commas.
     */
    private String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDoubleSafe(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
