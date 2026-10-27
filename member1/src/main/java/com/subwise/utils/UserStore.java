
package com.subwise.utils;

import com.subwise.model.User;
import java.util.HashMap;
import java.util.Map;

public class UserStore {
    
    public static void setCurrentUser(User user) {
    currentUser = user;
    }

    private static final Map<String, User> users = new HashMap<>();
    static {
        User demoUser = new User(
            0,
            "Demo User",
            "demo@subwise.com",
            "demo123"
        );

        users.put("demo@subwise.com", demoUser);
    }
    private static User currentUser;
    private static int nextUserId = 1;

    public static boolean register(User user) {
    if (user == null || user.getEmail() == null) {
        return false;
    }

    String email = user.getEmail().trim().toLowerCase();

    if (email.isEmpty() || users.containsKey(email)) {
        return false;
    }

    users.put(email, user);
    return true;
    }

    public static boolean login(String email, String password) {
        if (email == null || password == null) {
            return false;
        }

        User user = users.get(email.trim().toLowerCase());

        if (user != null && password.equals(user.getPassword())) {
            currentUser = user;
            return true;
        }

        return false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static int generateUserId() {
        return nextUserId++;
    }

    public static boolean updateCurrentUser(String name, String email) {
        if (currentUser == null || name == null || email == null
                || name.isBlank() || email.isBlank()) {
            return false;
        }

        String newEmail = email.trim().toLowerCase();
        String oldEmail = currentUser.getEmail().trim().toLowerCase();

        User existingUser = users.get(newEmail);

        if (existingUser != null && existingUser != currentUser) {
            return false;
        }

        users.remove(oldEmail);
        currentUser.setName(name.trim());
        currentUser.setEmail(email.trim());
        users.put(newEmail, currentUser);

        return true;
    }
}
