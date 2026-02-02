package com.myapp.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.model.ApiResponse;
import com.myapp.model.MovieResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OphimApiClient {
    private static final String BASE_URL = "https://ophim1.com";
    private static final String LATEST_URL = BASE_URL + "/danh-sach/phim-moi-cap-nhat";
    private static final String DETAIL_URL = BASE_URL + "/phim/";

    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public OphimApiClient() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    // Lấy danh sách phim mới (Home)
    public ApiResponse getLatestMovies(int page) {
        String url = LATEST_URL + "?page=" + page;
        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    return mapper.readValue(json, ApiResponse.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy chi tiết phim (Watch)
    public MovieResponse getMovieDetails(String slug) {
        String url = DETAIL_URL + slug;
        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    return mapper.readValue(json, MovieResponse.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}