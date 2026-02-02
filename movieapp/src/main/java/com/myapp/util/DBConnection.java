package com.myapp.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/movie_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Koala8506@@";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}