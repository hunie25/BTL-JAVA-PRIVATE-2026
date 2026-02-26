package com.myapp.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.model.ApiResponse;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OphimApiClient {

    private static final String BASE = "https://ophim1.com";
    private static final String API_BASE = BASE + "/v1/api";

    // TMDB image domain
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/";

    // Size gợi ý:
    // - HERO/backdrop: original để nét nhất (bạn có thể đổi về w1280 nếu muốn nhẹ hơn)
    // - Poster/card fallback: w780 là ổn
    private static final String HERO_BACKDROP_SIZE = "original"; // hoặc "w1280"
    private static final String POSTER_SIZE = "w780";

    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public OphimApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        this.mapper = new ObjectMapper();
    }

    // =========================
    // HOME: /v1/api/home
    // =========================
    public List<Movie> getHomeMovies() {
        HttpUrl url = HttpUrl.parse(API_BASE + "/home");
        if (url == null) return new ArrayList<>();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return new ArrayList<>();

            String json = response.body().string();
            ApiResponse<HomeData> res = mapper.readValue(
                    json,
                    mapper.getTypeFactory().constructParametricType(ApiResponse.class, HomeData.class)
            );

            if (res != null && res.isSuccess() && res.getData() != null && res.getData().getItems() != null) {
                return res.getData().getItems();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // =========================
    // DANH SÁCH: /v1/api/danh-sach/{slug}?page=&limit=
    // =========================
    public List<Movie> getMoviesByList(String listSlug, int page, int limit) {
        HttpUrl baseUrl = HttpUrl.parse(API_BASE + "/danh-sach/" + listSlug);
        if (baseUrl == null) return new ArrayList<>();

        HttpUrl url = baseUrl.newBuilder()
                .addQueryParameter("page", String.valueOf(Math.max(1, page)))
                .addQueryParameter("limit", String.valueOf(limit <= 0 ? 24 : limit))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return new ArrayList<>();

            String json = response.body().string();
            ApiResponse<ListData> res = mapper.readValue(
                    json,
                    mapper.getTypeFactory().constructParametricType(ApiResponse.class, ListData.class)
            );

            if (res != null && res.isSuccess() && res.getData() != null && res.getData().getItems() != null) {
                return res.getData().getItems();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    // =========================
    // CHI TIẾT PHIM: /v1/api/phim/{slug}
    // =========================
    public MovieResponse getMovieDetails(String slug) {
        if (slug == null || slug.isBlank()) return null;

        HttpUrl url = HttpUrl.parse(API_BASE + "/phim/" + slug.trim());
        if (url == null) return null;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            String json = response.body().string();
            return mapper.readValue(json, MovieResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================
    // ẢNH TMDB: /v1/api/phim/{slug}/images
    // =========================
    public MovieImages getMovieImages(String slug) {
        if (slug == null || slug.isBlank()) return new MovieImages();

        HttpUrl url = HttpUrl.parse(API_BASE + "/phim/" + slug.trim() + "/images");
        if (url == null) return new MovieImages();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return new MovieImages();

            String json = response.body().string();
            JsonNode root = mapper.readTree(json);

            MovieImages out = new MovieImages();
            JsonNode images = root.path("data").path("images");

            if (!images.isArray()) return out;

            for (JsonNode img : images) {
                String type = img.path("type").asText("");
                String filePath = img.path("file_path").asText("");

                if (filePath.isBlank()) continue;

                if ("backdrop".equalsIgnoreCase(type)) {
                    // Hero ưu tiên nét
                    String full = toTmdbImageUrl(filePath, HERO_BACKDROP_SIZE);
                    if (full != null) out.backdrops.add(full);

                    // (Tuỳ chọn) thêm fallback size nhỏ hơn nếu original lỗi:
                    // String fallback = toTmdbImageUrl(filePath, "w1280");
                    // if (fallback != null && !out.backdrops.contains(fallback)) out.backdrops.add(fallback);

                } else if ("poster".equalsIgnoreCase(type)) {
                    String full = toTmdbImageUrl(filePath, POSTER_SIZE);
                    if (full != null) out.posters.add(full);
                }
            }

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return new MovieImages();
        }
    }

    private String toTmdbImageUrl(String filePath, String size) {
        if (filePath == null || filePath.isBlank()) return null;

        // Nếu API trả full url thì dùng luôn
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            return filePath;
        }

        if (!filePath.startsWith("/")) filePath = "/" + filePath;
        if (size == null || size.isBlank()) size = "original";

        return TMDB_IMAGE_BASE + size + filePath;
    }

    // ===== DTOs nội bộ cho parse =====
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class HomeData {
        @com.fasterxml.jackson.annotation.JsonProperty("items")
        private List<Movie> items;

        public List<Movie> getItems() {
            return items;
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListData {
        @com.fasterxml.jackson.annotation.JsonProperty("items")
        private List<Movie> items;

        public List<Movie> getItems() {
            return items;
        }
    }

    public static class MovieImages {
        public List<String> posters = new ArrayList<>();
        public List<String> backdrops = new ArrayList<>();
    }
}