
package com.subwise.member3;

import com.subwise.dao.SubscriptionDAO;
import com.subwise.model.Subscription;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiryChecker implements AutoCloseable {

    private final ScheduledExecutorService executor =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "subwise-expiry-checker");
            thread.setDaemon(true);
            return thread;
        });

    private final Set<Integer> alreadyNotified = new HashSet<>();

    public void start() {
        // Run immediately, then check again every hour.
        executor.scheduleWithFixedDelay(
            this::checkSafely,
            0,
            1,
            TimeUnit.HOURS
        );
    }

    // Allows the admin to trigger a check manually.
    public void checkNow() {
        checkSafely();
    }

    private void checkSafely() {
        try {
            // Retrieve subscriptions from the shared MySQL database.
            List<Subscription> subscriptions =
                new SubscriptionDAO().getAllSubscriptionsForAdmin();

            LocalDate today = LocalDate.now();

            for (Subscription subscription : subscriptions) {

                if (subscription == null
                        || subscription.getEndDate() == null
                        || subscription.getStatus() == null) {
                    continue;
                }

                if (!subscription.getStatus().equalsIgnoreCase("ACTIVE")) {
                    continue;
                }

                LocalDate endDate = subscription.getEndDate();
                long daysRemaining =
                    ChronoUnit.DAYS.between(today, endDate);

                // Notify for subscriptions expiring today or within 7 days.
                if (daysRemaining >= 0 && daysRemaining <= 7) {

                    int subscriptionId =
                        subscription.getSubscriptionId();

                    synchronized (alreadyNotified) {
                        if (!alreadyNotified.add(subscriptionId)) {
                            continue;
                        }
                    }

                    String message =
                        subscription.getServiceName()
                        + " (User ID: " + subscription.getUserId() + ")"
                        + " expires "
                        + (daysRemaining == 0
                            ? "today."
                            : "in " + daysRemaining + " day(s).");

                    System.out.println(
                        "[Subwise expiry reminder] " + message
                    );

                    Platform.runLater(() -> {
                        Alert alert =
                            new Alert(Alert.AlertType.INFORMATION);

                        alert.setTitle("Upcoming Subscription Expiry");
                        alert.setHeaderText("Subscription Reminder");
                        alert.setContentText(message);
                        alert.show();
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println(
                "Could not retrieve subscriptions for expiry checking: "
                + e.getMessage()
            );
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println(
                "Expiry check failed: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
