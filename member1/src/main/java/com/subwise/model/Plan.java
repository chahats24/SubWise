
package com.subwise.model;

public class Plan {
    private int planId;
    private String planName;
    private double price;
    private String billingCycle;

    public Plan(int planId, String planName,
                double price, String billingCycle) {
        this.planId = planId;
        this.planName = planName;
        this.price = price;
        this.billingCycle = billingCycle;
    }

    public int getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public double getPrice() {
        return price;
    }

    public String getBillingCycle() {
        return billingCycle;
    }
}
