package com.myapp.dao;

import com.myapp.model.Movie;
import com.myapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    private static volatile boolean schemaEnsured = false;

    private void ensureSchema(Connection conn) {
        if (schemaEnsured || conn == null) return;

        synchronized (HistoryDAO.class) {
            if (schemaEnsured) return;

            tryAddColumn(conn, "watch_history", "episode_index", "INT NOT NULL DEFAULT 1");
            tryAddColumn(conn, "watch_history", "position_seconds", "INT NOT NULL DEFAULT 0");
            tryAddColumn(conn, "watch_history", "duration_seconds", "INT NOT NULL DEFAULT 0");

            schemaEnsured = true;
        }
    }

    private void tryAddColumn(Connection conn, String table, String col, String ddlType) {
        try {
            if (hasColumn(conn, table, col)) return;
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + col + " " + ddlType);
            }
        } catch (Exception ignored) {
            // If DB already has it or DB engine differs, ignore safely
        }
    }

    private boolean hasColumn(Connection conn, String table, String col) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, col)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = meta.getColumns(null, null, table.toUpperCase(), col.toUpperCase())) {
            return rs.next();
        }
    }

    public void saveOrUpdate(int userId, String slug, String name, String thumb) {
        saveOrUpdate(userId, slug, name, thumb, 1, 0, 0);
    }

    public void saveOrUpdate(int userId, String slug, String name, String thumb,
                             int episodeIndex, int positionSeconds, int durationSeconds) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            ensureSchema(conn);

            String updateSql = "UPDATE watch_history SET movie_name=?, movie_thumb=?, " +
                    "episode_index=?, position_seconds=?, duration_seconds=?, viewed_at=CURRENT_TIMESTAMP " +
                    "WHERE user_id=? AND movie_slug=?";

            int rows;
            try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                up.setString(1, name);
                up.setString(2, thumb);
                up.setInt(3, Math.max(1, episodeIndex));
                up.setInt(4, Math.max(0, positionSeconds));
                up.setInt(5, Math.max(0, durationSeconds));
                up.setInt(6, userId);
                up.setString(7, slug);
                rows = up.executeUpdate();
            }

            if (rows == 0) {
                String insertSql = "INSERT INTO watch_history " +
                        "(user_id, movie_slug, movie_name, movie_thumb, episode_index, position_seconds, duration_seconds, viewed_at) " +
                        "VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    ins.setInt(1, userId);
                    ins.setString(2, slug);
                    ins.setString(3, name);
                    ins.setString(4, thumb);
                    ins.setInt(5, Math.max(1, episodeIndex));
                    ins.setInt(6, Math.max(0, positionSeconds));
                    ins.setInt(7, Math.max(0, durationSeconds));
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lưu lịch sử/progress: " + e.getMessage());
        }
    }

    public ProgressInfo getProgress(int userId, String slug) {
        String sql = "SELECT episode_index, position_seconds, duration_seconds " +
                "FROM watch_history WHERE user_id=? AND movie_slug=? LIMIT 1";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            ensureSchema(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, slug);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new ProgressInfo(
                                rs.getInt("episode_index"),
                                rs.getInt("position_seconds"),
                                rs.getInt("duration_seconds")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy progress: " + e.getMessage());
        }
        return null;
    }

    public List<Movie> getWatchHistory(int userId) {
        List<Movie> historyList = new ArrayList<>();

        String sql = "SELECT movie_slug, movie_name, movie_thumb, episode_index, position_seconds, duration_seconds, viewed_at " +
                "FROM watch_history WHERE user_id=? ORDER BY viewed_at DESC LIMIT 50";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return historyList;
            ensureSchema(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movie m = new Movie();
                        m.setSlug(rs.getString("movie_slug"));
                        m.setName(rs.getString("movie_name"));
                        m.setThumbUrl(rs.getString("movie_thumb"));

                        m.setHistoryEpisodeIndex(rs.getInt("episode_index"));
                        m.setHistoryPositionSeconds(rs.getInt("position_seconds"));
                        m.setHistoryDurationSeconds(rs.getInt("duration_seconds"));

                        String ts = rs.getString("viewed_at");
                        m.setHistoryViewedAt(ts);

                        historyList.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử: " + e.getMessage());
        }
        return historyList;
    }

    public void deleteHistory(int userId, String movieSlug) {
        String sql = "DELETE FROM watch_history WHERE user_id=? AND movie_slug=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return;
            ps.setInt(1, userId);
            ps.setString(2, movieSlug);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAllHistory(int userId) {
        String sql = "DELETE FROM watch_history WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return;
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ProgressInfo {
        public final int episodeIndex;
        public final int positionSeconds;
        public final int durationSeconds;

        public ProgressInfo(int ep, int pos, int dur) {
            this.episodeIndex = ep;
            this.positionSeconds = pos;
            this.durationSeconds = dur;
        }
    }
}