package com.retainiq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload sent to Python ML service for campaign generation.
 */
public class CampaignGenerateRequest {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("churn_probability")
    private double churnProbability;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("preferred_channel")
    private String preferredChannel;

    private int recency;
    private int frequency;
    private double monetary;

    @JsonProperty("loyalty_points")
    private int loyaltyPoints;

    public CampaignGenerateRequest() {
    }

    public CampaignGenerateRequest(String customerId, String customerName,
                                    double churnProbability, String riskLevel,
                                    String preferredChannel, int recency,
                                    int frequency, double monetary, int loyaltyPoints) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.churnProbability = churnProbability;
        this.riskLevel = riskLevel;
        this.preferredChannel = preferredChannel;
        this.recency = recency;
        this.frequency = frequency;
        this.monetary = monetary;
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getChurnProbability() {
        return churnProbability;
    }

    public void setChurnProbability(double churnProbability) {
        this.churnProbability = churnProbability;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(String preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    public int getRecency() {
        return recency;
    }

    public void setRecency(int recency) {
        this.recency = recency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public double getMonetary() {
        return monetary;
    }

    public void setMonetary(double monetary) {
        this.monetary = monetary;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}
