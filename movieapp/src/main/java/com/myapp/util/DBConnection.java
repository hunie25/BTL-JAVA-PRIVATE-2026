package com.myapp.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/movie_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Koala8506@@";

    public static Connection getConnection() {
        try {
            // Nạp Driver thủ công để chắc chắn
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println(">>> KẾT NỐI DATABASE THÀNH CÔNG!");
            return conn;
        } catch (Exception e) {
            System.err.println("!!! LỖI KẾT NỐI DATABASE: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}