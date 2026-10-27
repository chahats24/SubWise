
package com.subwise.model;

import java.time.LocalDate;

public class Subscription {
    private int subscriptionId;
    private int userId;
    private String serviceName;
    private String category;
    private double price;
    private String billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public Subscription(int subscriptionId, int userId,
                        String serviceName, String category,
                        double price, String billingCycle,
                        LocalDate startDate, LocalDate endDate,
                        String status) {
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.serviceName = serviceName;
        this.category = category;
        this.price = price;
        this.billingCycle = billingCycle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
