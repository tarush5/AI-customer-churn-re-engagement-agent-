package com.retainiq.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from the Python ML service for churn prediction.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChurnPredictionResponse {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("churn_probability")
    private double churnProbability;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("risk_factors")
    private String riskFactors;

    @JsonProperty("recommended_action")
    private String recommendedAction;

    public ChurnPredictionResponse() {
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(String riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    @Override
    public String toString() {
        return "ChurnPredictionResponse{" +
                "customerId='" + customerId + '\'' +
                ", churnProbability=" + churnProbability +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
