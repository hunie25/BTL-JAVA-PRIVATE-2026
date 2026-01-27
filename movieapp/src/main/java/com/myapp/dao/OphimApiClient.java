package com.myapp.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.model.Episode;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OphimApiClient {
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    // API Nguồn Ophim1.com (Nguồn ổn định, không chặn Java)
    private final String LIST_API = "https://ophim1.com/danh-sach/phim-moi-cap-nhat?page=";
    private final String DETAIL_API = "https://ophim1.com/phim/";

    public OphimApiClient() {

        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
        // Jackson Mapper
        this.mapper = new ObjectMapper();
    }

    /**
     * Lấy danh sách phim mới
     */
    public List<Movie> getNewMovies(int page) {
        try {
            System.out.println("🌐 Đang tải trang " + page + "...");
            String json = fetchJson(LIST_API + page);

            if (json != null) {

                MovieResponse response = mapper.readValue(json, MovieResponse.class);
                return response.getItems() != null ? response.getItems() : new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi API List: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Lấy chi tiết tập phim
     */
    public List<Episode> getEpisodes(String slug) {
        try {
            System.out.println("🔍 Đang lấy tập phim: " + slug);
            String json = fetchJson(DETAIL_API + slug);

            if (json != null) {
                MovieResponse response = mapper.readValue(json, MovieResponse.class);

                // Logic lấy server đầu tiên (thường là Vietsub #1)
                if (response.getEpisodes() != null && !response.getEpisodes().isEmpty()) {
                    return response.getEpisodes().get(0).getServer_data();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi API Detail: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    // Hàm gửi request dùng OkHttp
    private String fetchJson(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                // Fake User-Agent để server tưởng là Chrome
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                System.err.println("⚠️ Server trả về lỗi: " + response.code());
                return null;
            }
        }
    }
}