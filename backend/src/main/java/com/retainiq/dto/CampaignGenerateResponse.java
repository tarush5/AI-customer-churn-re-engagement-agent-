package com.retainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from the Python ML service for campaign generation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignGenerateResponse {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("campaign_type")
    private String campaignType;

    private String subject;
    private String message;
    private String channel;

    @JsonProperty("strategy_reasoning")
    private String strategyReasoning;

    @JsonProperty("urgency_level")
    private String urgencyLevel;

    @JsonProperty("discount_offered")
    private String discountOffered;

    public CampaignGenerateResponse() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(String campaignType) {
        this.campaignType = campaignType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStrategyReasoning() {
        return strategyReasoning;
    }

    public void setStrategyReasoning(String strategyReasoning) {
        this.strategyReasoning = strategyReasoning;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getDiscountOffered() {
        return discountOffered;
    }

    public void setDiscountOffered(String discountOffered) {
        this.discountOffered = discountOffered;
    }

    @Override
    public String toString() {
        return "CampaignGenerateResponse{" +
                "customerId='" + customerId + '\'' +
                ", campaignType='" + campaignType + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
