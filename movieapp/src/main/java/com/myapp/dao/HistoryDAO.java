package com.myapp.dao;

import com.myapp.model.Movie;
import com.myapp.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {
    public void saveOrUpdate(int userId, String slug, String name, String thumb) {
        String sql = "INSERT INTO watch_history (user_id, movie_slug, movie_name, movie_thumb, viewed_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE viewed_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, slug);
            pstmt.setString(3, name);
            pstmt.setString(4, thumb);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    public List<Movie> getWatchHistory(int userId) {
        List<Movie> historyList = new ArrayList<>();
        String sql = "SELECT movie_slug, movie_name, movie_thumb FROM watch_history " +
                "WHERE user_id = ? ORDER BY viewed_at DESC LIMIT 20";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Movie movie = new Movie();
                movie.setSlug(rs.getString("movie_slug"));
                movie.setName(rs.getString("movie_name"));
                movie.setThumbUrl(rs.getString("movie_thumb"));
                historyList.add(movie);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử: " + e.getMessage());
        }
        return historyList;
    }

    public void deleteHistory(int userId, String movieSlug) {
        String sql = "DELETE FROM watch_history WHERE user_id = ? AND movie_slug = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, movieSlug);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}