package com.myapp.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".movieapp";
    private static final String DB_PATH = DB_DIR + File.separator + "movieapp.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private static volatile boolean initialized = false;

    public static Connection getConnection() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) dir.mkdirs();

            Connection conn = DriverManager.getConnection(URL);

            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }

            initSchema(conn);
            return conn;
        } catch (Exception e) {
            System.err.println("!!! LỖI DATABASE LOCAL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể mở database local (SQLite). " +
                    "Hãy kiểm tra pom.xml đã thêm sqlite-jdbc chưa.", e);
        }
    }

    private static synchronized void initSchema(Connection conn) throws SQLException {
        if (initialized) return;

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    verified INTEGER NOT NULL DEFAULT 1
                )
            """);

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS watch_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    movie_slug TEXT NOT NULL,
                    movie_name TEXT,
                    movie_thumb TEXT,
                    viewed_at TEXT DEFAULT (datetime('now')),
                    UNIQUE(user_id, movie_slug),
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);
        }

        initialized = true;
        System.out.println(">>> SQLite DB ready: " + DB_PATH);
    }
}