
package com.subwise.model;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private int subscriptionId;
    private String serviceName;
    private double amount;
    private LocalDate paymentDate;
    private String status;

    public Payment(int paymentId, int subscriptionId,
                   double amount, LocalDate paymentDate,
                   String status) {
        this.paymentId = paymentId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public Payment(int paymentId, int subscriptionId,
                   String serviceName, double amount,
                   LocalDate paymentDate, String status) {
        this.paymentId = paymentId;
        this.subscriptionId = subscriptionId;
        this.serviceName = serviceName;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getStatus() {
        return status;
    }
}
