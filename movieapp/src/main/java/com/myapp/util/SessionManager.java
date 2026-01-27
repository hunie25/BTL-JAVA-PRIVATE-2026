package com.myapp.util;

import com.myapp.model.User;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    private static final Map<String, Object> session = new HashMap<>();

    public static void set(String key, Object value) {
        session.put(key, value);
    }

    public static Object get(String key) {
        return session.get(key);
    }

    public static void remove(String key) {
        session.remove(key);
    }

    public static void clear() {
        session.clear();
    }
}
