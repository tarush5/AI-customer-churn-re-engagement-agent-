package com.retainiq.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Campaign log entry — stored in-memory (ConcurrentHashMap), not in a database.
 * Represents a single campaign action taken for a customer.
 */
public class CampaignLog {

    private String id;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String campaignType;
    private String subject;
    private String message;
    private String channel;
    private String strategyReasoning;
    private String status; // SENT, DELIVERED, OPENED, CLICKED
    private LocalDateTime sentAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;

    public CampaignLog() {
        this.id = UUID.randomUUID().toString();
        this.status = "PENDING";
        this.sentAt = LocalDateTime.now();
    }

    public CampaignLog(String customerId, String customerName, String customerEmail,
                       String campaignType, String subject, String message,
                       String channel, String strategyReasoning) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.campaignType = campaignType;
        this.subject = subject;
        this.message = message;
        this.channel = channel;
        this.strategyReasoning = strategyReasoning;
        this.status = "PENDING";
        this.sentAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }

    @Override
    public String toString() {
        return "CampaignLog{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", campaignType='" + campaignType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
