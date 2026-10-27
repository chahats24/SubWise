
package com.subwise.dao;

import com.subwise.database.DBConnection;
import com.subwise.model.Customer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean registerCustomer(
            String name, String email, String password) throws SQLException {

        String sql = """
            INSERT INTO users (name, email, password_hash, role)
            VALUES (?, ?, ?, 'CUSTOMER')
            """;

        String hashedPassword = BCrypt.hashpw(
            password, BCrypt.gensalt()
        );

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            return ps.executeUpdate() == 1;
        }
    }

    public Customer loginCustomer(
            String email, String password) throws SQLException {

        String sql = """
            SELECT user_id, name, email, password_hash
            FROM users
            WHERE email = ? AND role = 'CUSTOMER'
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    if (BCrypt.checkpw(password, storedHash)) {
                        return new Customer(
                            rs.getLong("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            "",
                            ""
                        );
                    }
                }
            }
        }

        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
