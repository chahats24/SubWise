package com.subwise.member3;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Demonstration only: simulates an asynchronous payment task; it does not charge or persist money. */
public class PaymentProcessor implements AutoCloseable {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "subwise-payment-demo");
        t.setDaemon(true);
        return t;
    });

    public void processDemoPayment(double amount, Runnable onSuccess) {
        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        executor.submit(() -> {
            try {
                System.out.println("Demo payment started for amount: " + amount);
                Thread.sleep(1500); // Simulate a slow operation; no real payment is made.
                System.out.println("Demo payment completed.");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Payment demo");
                    alert.setHeaderText("Simulated payment completed");
                    alert.setContentText(String.format(java.util.Locale.ROOT,
                            "₹%.2f processed in demo mode. No money was charged.", amount));
                    alert.show();
                    if (onSuccess != null) onSuccess.run();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
