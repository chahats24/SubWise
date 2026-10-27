
package com.subwise.dao;

import com.subwise.database.DBConnection;
import com.subwise.model.Subscription;
import com.subwise.utils.UserStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionDAO {

    // Add a subscription for the currently logged-in user
    public boolean addSubscription(Subscription subscription)
            throws SQLException {

        var currentUser = UserStore.getCurrentUser();

        if (currentUser == null) {
            throw new SQLException("No user is currently logged in.");
        }

        String categorySql =
            "SELECT category_id FROM categories WHERE category_name = ?";

        String insertSql = """
            INSERT INTO subscriptions
            (user_id, category_id, service_name, plan_name, price,
             billing_cycle, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection()) {

            int categoryId;

            try (PreparedStatement ps =
                    con.prepareStatement(categorySql)) {

                ps.setString(1, subscription.getCategory());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        categoryId = rs.getInt("category_id");
                    } else {
                        throw new SQLException(
                            "Category not found: "
                            + subscription.getCategory()
                        );
                    }
                }
            }

            try (PreparedStatement ps =
                    con.prepareStatement(insertSql)) {

                ps.setLong(1, currentUser.getUserId());
                ps.setInt(2, categoryId);
                ps.setString(3, subscription.getServiceName());
                ps.setString(4, null);
                ps.setDouble(5, subscription.getPrice());

                String billing = subscription.getBillingCycle()
                    .toUpperCase(Locale.ROOT);

                ps.setString(6, billing);
                ps.setDate(
                    7, Date.valueOf(subscription.getStartDate())
                );

                // The database requires an end date.
                java.time.LocalDate endDate = subscription.getEndDate();

                if (endDate == null) {
                    endDate = subscription.getStartDate()
                        .plusMonths(billing.equals("YEARLY") ? 12 : 1);
                }

                ps.setDate(8, Date.valueOf(endDate));
                ps.setString(9, "ACTIVE");

                return ps.executeUpdate() == 1;
            }
        }
    }

    // Fetch subscriptions belonging to the logged-in user
    public List<Subscription> getSubscriptionsForCurrentUser()
            throws SQLException {

        var currentUser = UserStore.getCurrentUser();

        if (currentUser == null) {
            return new ArrayList<>();
        }

        String sql = """
            SELECT s.subscription_id, s.user_id, s.service_name,
                   c.category_name, s.price, s.billing_cycle,
                   s.start_date, s.end_date, s.status
            FROM subscriptions s
            JOIN categories c ON s.category_id = c.category_id
            WHERE s.user_id = ?
            ORDER BY s.created_at DESC
            """;

        List<Subscription> result = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, currentUser.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Subscription(
                        rs.getInt("subscription_id"),
                        rs.getInt("user_id"),
                        rs.getString("service_name"),
                        rs.getString("category_name"),
                        rs.getDouble("price"),
                        rs.getString("billing_cycle"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("status")
                    ));
                }
            }
        }

        return result;
    }

    // Delete a subscription belonging to the logged-in user
    public boolean deleteSubscription(int subscriptionId)
            throws SQLException {

        var currentUser = UserStore.getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        String sql = """
            DELETE FROM subscriptions
            WHERE subscription_id = ? AND user_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subscriptionId);
            ps.setLong(2, currentUser.getUserId());

            return ps.executeUpdate() == 1;
        }
    }

    // Update a subscription belonging to the logged-in user
    public boolean updateSubscription(Subscription subscription)
            throws SQLException {

        var currentUser = UserStore.getCurrentUser();

        if (currentUser == null || subscription == null) {
            return false;
        }

        String sql = """
            UPDATE subscriptions s
            JOIN categories c ON c.category_name = ?
            SET s.category_id = c.category_id,
                s.service_name = ?,
                s.price = ?,
                s.billing_cycle = ?,
                s.status = ?
            WHERE s.subscription_id = ? AND s.user_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subscription.getCategory());
            ps.setString(2, subscription.getServiceName());
            ps.setDouble(3, subscription.getPrice());
            ps.setString(
                4,
                subscription.getBillingCycle().toUpperCase(Locale.ROOT)
            );
            ps.setString(
                5,
                subscription.getStatus().toUpperCase(Locale.ROOT)
            );
            ps.setInt(6, subscription.getSubscriptionId());
            ps.setLong(7, currentUser.getUserId());

            return ps.executeUpdate() == 1;
        }
    }
    
    public List<Subscription> getAllSubscriptionsForAdmin()
            throws SQLException {

        String sql = """
            SELECT s.subscription_id, s.user_id, s.service_name,
                c.category_name, s.price, s.billing_cycle,
                s.start_date, s.end_date, s.status
            FROM subscriptions s
            JOIN categories c ON s.category_id = c.category_id
            ORDER BY s.end_date ASC
            """;

        List<Subscription> result = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new Subscription(
                    rs.getInt("subscription_id"),
                    rs.getInt("user_id"),
                    rs.getString("service_name"),
                    rs.getString("category_name"),
                    rs.getDouble("price"),
                    rs.getString("billing_cycle"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date").toLocalDate(),
                    rs.getString("status")
                ));
            }
        }

        return result;
    }

}
