package com.subwise.member3;

/** Transparent rule-based heuristic; it is not an AI model. */
public final class SubscriptionHealth {
    private SubscriptionHealth() { }

    /**
     * @param usageDays number of days the user used the service in the last 30 days (0..30)
     * @param monthlyCost monthly-equivalent cost in the user's currency
     * @return score from 0 to 100; higher means usage appears to justify the cost
     */
    public static int calculateScore(int usageDays, double monthlyCost) {
        int days = Math.max(0, Math.min(30, usageDays));
        if (monthlyCost < 0 || Double.isNaN(monthlyCost) || Double.isInfinite(monthlyCost)) {
            throw new IllegalArgumentException("Monthly cost must be a valid non-negative number.");
        }
        // Usage is the main signal; cost adds a small penalty when the cost is high.
        double usageScore = (days / 30.0) * 85.0;
        double costPenalty = Math.min(15.0, monthlyCost / 100.0);
        return (int) Math.round(Math.max(0, Math.min(100, usageScore + 15.0 - costPenalty)));
    }

    public static String recommendation(int score) {
        if (score >= 70) return "KEEP — usage appears worthwhile";
        if (score >= 40) return "REVIEW — compare usage and cost";
        return "CONSIDER CANCELLING — usage appears low";
    }
}
