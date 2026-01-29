package com.myapp.service;

import com.myapp.dao.OphimApiClient;
import com.myapp.model.ApiResponse;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;

import java.util.ArrayList;
import java.util.List;

public class MovieService {
    private final OphimApiClient apiClient;

    public MovieService() {
        this.apiClient = new OphimApiClient();
    }

    public List<Movie> getNewMovies() {
        ApiResponse response = apiClient.getLatestMovies(1); // Mặc định trang 1
        if (response != null && response.isStatus()) {
            return response.getItems();
        }
        return new ArrayList<>();
    }

    public MovieResponse getMovieDetails(String slug) {
        return apiClient.getMovieDetails(slug);
    }
}