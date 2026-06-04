package com.retainiq.dto;

/**
 * Dashboard statistics DTO — aggregates key metrics for the frontend dashboard.
 */
public class DashboardStats {

    private long totalCustomers;
    private long highRiskCount;
    private long mediumRiskCount;
    private long lowRiskCount;
    private long campaignsSent;
    private double avgChurnScore;
    private double reEngagementRate;

    public DashboardStats() {
    }

    public DashboardStats(long totalCustomers, long highRiskCount, long mediumRiskCount,
                          long lowRiskCount, long campaignsSent, double avgChurnScore,
                          double reEngagementRate) {
        this.totalCustomers = totalCustomers;
        this.highRiskCount = highRiskCount;
        this.mediumRiskCount = mediumRiskCount;
        this.lowRiskCount = lowRiskCount;
        this.campaignsSent = campaignsSent;
        this.avgChurnScore = avgChurnScore;
        this.reEngagementRate = reEngagementRate;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getHighRiskCount() {
        return highRiskCount;
    }

    public void setHighRiskCount(long highRiskCount) {
        this.highRiskCount = highRiskCount;
    }

    public long getMediumRiskCount() {
        return mediumRiskCount;
    }

    public void setMediumRiskCount(long mediumRiskCount) {
        this.mediumRiskCount = mediumRiskCount;
    }

    public long getLowRiskCount() {
        return lowRiskCount;
    }

    public void setLowRiskCount(long lowRiskCount) {
        this.lowRiskCount = lowRiskCount;
    }

    public long getCampaignsSent() {
        return campaignsSent;
    }

    public void setCampaignsSent(long campaignsSent) {
        this.campaignsSent = campaignsSent;
    }

    public double getAvgChurnScore() {
        return avgChurnScore;
    }

    public void setAvgChurnScore(double avgChurnScore) {
        this.avgChurnScore = avgChurnScore;
    }

    public double getReEngagementRate() {
        return reEngagementRate;
    }

    public void setReEngagementRate(double reEngagementRate) {
        this.reEngagementRate = reEngagementRate;
    }
}
