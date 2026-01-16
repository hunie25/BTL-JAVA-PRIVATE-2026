package com.myapp.util;

import com.myapp.model.User;

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
}
