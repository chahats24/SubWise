package com.subwise.member3;

import com.subwise.model.Subscription;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Analytics helpers based on the current in-memory SubscriptionStore data. */
public final class ExpenseAnalyzer {
    private ExpenseAnalyzer() { }

    public static double monthlyEquivalent(Subscription s) {
        if (s == null || s.getPrice() < 0) return 0;
        String cycle = s.getBillingCycle() == null ? "monthly" : s.getBillingCycle().trim();
        if (cycle.equalsIgnoreCase("yearly") || cycle.equalsIgnoreCase("annual") || cycle.equalsIgnoreCase("annually")) return s.getPrice() / 12.0;
        if (cycle.equalsIgnoreCase("weekly")) return s.getPrice() * 52.0 / 12.0;
        if (cycle.equalsIgnoreCase("quarterly")) return s.getPrice() / 3.0;
        return s.getPrice();
    }

    public static double activeMonthlySpend(List<Subscription> subscriptions) {
        double total = 0;
        if (subscriptions == null) return total;
        for (Subscription s : subscriptions) {
            if (s != null && "Active".equalsIgnoreCase(s.getStatus())) total += monthlyEquivalent(s);
        }
        return total;
    }

    public static Map<String, Double> spendingByCategory(List<Subscription> subscriptions) {
        Map<String, Double> result = new LinkedHashMap<>();
        if (subscriptions == null) return result;
        for (Subscription s : subscriptions) {
            if (s == null || !"Active".equalsIgnoreCase(s.getStatus())) continue;
            String category = s.getCategory();
            if (category == null || category.isBlank()) category = "Other";
            result.merge(category, monthlyEquivalent(s), Double::sum);
        }
        return result;
    }
}
