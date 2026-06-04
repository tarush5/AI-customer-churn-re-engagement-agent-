package com.retainiq.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String customerId;

    private String name;
    private String email;
    private String phone;
    private int age;
    private int recency;
    private int frequency;
    private double monetary;
    private int loyaltyPoints;
    private String lastPurchaseDate;
    private String signupDate;
    private String preferredChannel;

    @Column(nullable = true)
    private Double churnScore;

    @Column(nullable = true)
    private String riskLevel;

    public Customer() {
    }

    public Customer(String customerId, String name, String email, String phone, int age,
                    int recency, int frequency, double monetary, int loyaltyPoints,
                    String lastPurchaseDate, String signupDate, String preferredChannel) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.recency = recency;
        this.frequency = frequency;
        this.monetary = monetary;
        this.loyaltyPoints = loyaltyPoints;
        this.lastPurchaseDate = lastPurchaseDate;
        this.signupDate = signupDate;
        this.preferredChannel = preferredChannel;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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

    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(String lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public String getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(String signupDate) {
        this.signupDate = signupDate;
    }

    public String getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(String preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    public Double getChurnScore() {
        return churnScore;
    }

    public void setChurnScore(Double churnScore) {
        this.churnScore = churnScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", churnScore=" + churnScore +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
