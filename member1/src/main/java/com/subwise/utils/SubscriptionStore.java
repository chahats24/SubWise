
package com.subwise.utils;

import com.subwise.dao.SubscriptionDAO;
import com.subwise.model.Subscription;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionStore {

    public static List<Subscription> getSubscriptions() {
        try {
            return new SubscriptionDAO().getSubscriptionsForCurrentUser();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void addSubscription(Subscription subscription) {
        if (subscription == null) {
            return;
        }

        try {
            new SubscriptionDAO().addSubscription(subscription);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeSubscription(Subscription subscription) {
        if (subscription == null) {
            return;
        }

        try {
            new SubscriptionDAO().deleteSubscription(
                subscription.getSubscriptionId()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getMonthlySpending() {
        double total = 0;

        for (Subscription subscription : getSubscriptions()) {
            if (isActive(subscription)) {
                total += getMonthlyPrice(subscription);
            }
        }

        return total;
    }

    public static long getActiveCount() {
        return getSubscriptions().stream()
            .filter(SubscriptionStore::isActive)
            .count();
    }

    public static Map<String, Double> getCategoryWiseSpending() {
        Map<String, Double> categoryTotals = new LinkedHashMap<>();

        for (Subscription subscription : getSubscriptions()) {
            if (isActive(subscription)) {
                String category = subscription.getCategory();

                if (category == null || category.isBlank()) {
                    category = "Other";
                }

                double monthlyPrice = getMonthlyPrice(subscription);

                categoryTotals.put(
                    category,
                    categoryTotals.getOrDefault(category, 0.0)
                        + monthlyPrice
                );
            }
        }

        return categoryTotals;
    }

    private static double getMonthlyPrice(Subscription subscription) {
        double price = subscription.getPrice();

        if (subscription.getBillingCycle() != null
                && subscription.getBillingCycle()
                    .equalsIgnoreCase("Yearly")) {
            price /= 12.0;
        }

        return price;
    }

    private static boolean isActive(Subscription subscription) {
        return subscription != null
            && subscription.getStatus() != null
            && subscription.getStatus().equalsIgnoreCase("Active");
    }
}
