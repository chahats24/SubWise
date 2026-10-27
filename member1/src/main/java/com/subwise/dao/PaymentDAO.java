
package com.subwise.dao;

import com.subwise.database.DBConnection;
import com.subwise.model.Payment;
import com.subwise.model.User;
import com.subwise.utils.UserStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> getPaymentsForCurrentUser()
            throws SQLException {

        List<Payment> payments = new ArrayList<>();
        User user = UserStore.getCurrentUser();

        if (user == null) {
            return payments;
        }

        String sql = """
            SELECT p.payment_id,
                   p.subscription_id,
                   s.service_name,
                   p.amount,
                   p.payment_date,
                   p.status
            FROM payments p
            JOIN subscriptions s
              ON p.subscription_id = s.subscription_id
             AND p.user_id = s.user_id
            WHERE p.user_id = ?
            ORDER BY p.payment_date DESC
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, user.getUserId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {

                    Timestamp timestamp =
                            result.getTimestamp("payment_date");

                    
                    Payment payment = new Payment(
                            result.getInt("payment_id"),
                            result.getInt("subscription_id"),
                            result.getString("service_name"),
                            result.getDouble("amount"),
                            timestamp == null
                                    ? java.time.LocalDate.now()
                                    : timestamp.toLocalDateTime().toLocalDate(),
                            result.getString("status")
                    );
                    payments.add(payment);
                   
                }
            }
        }

        return payments;
    }

    public boolean addPayment(Payment payment)
            throws SQLException {

        User user = UserStore.getCurrentUser();

        if (user == null) {
            throw new SQLException(
                    "Please log in before recording a payment.");
        }

        if (payment.getAmount() < 0) {
            throw new SQLException(
                    "Payment amount cannot be negative.");
        }

        String sql = """
            INSERT INTO payments
                (user_id, subscription_id, amount,
                 payment_date, status)
            SELECT ?, s.subscription_id, ?, ?, ?
            FROM subscriptions s
            WHERE s.subscription_id = ?
              AND s.user_id = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            long userId = user.getUserId();

            statement.setLong(1, userId);
            statement.setDouble(2, payment.getAmount());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            payment.getPaymentDate().atStartOfDay()));
            statement.setString(
                    4, payment.getStatus().toUpperCase(
                            java.util.Locale.ROOT));
            statement.setInt(5, payment.getSubscriptionId());
            statement.setLong(6, userId);

            return statement.executeUpdate() == 1;
        }
    }
}
