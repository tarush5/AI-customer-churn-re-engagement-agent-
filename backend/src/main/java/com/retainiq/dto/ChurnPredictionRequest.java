package com.retainiq.dto;

/**
 * Request payload sent to Python ML service for churn prediction.
 */
public class ChurnPredictionRequest {

    private String customerId;
    private int recency;
    private int frequency;
    private double monetary;
    private int loyaltyPoints;
    private int age;

    public ChurnPredictionRequest() {
    }

    public ChurnPredictionRequest(String customerId, int recency, int frequency,
                                   double monetary, int loyaltyPoints, int age) {
        this.customerId = customerId;
        this.recency = recency;
        this.frequency = frequency;
        this.monetary = monetary;
        this.loyaltyPoints = loyaltyPoints;
        this.age = age;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
