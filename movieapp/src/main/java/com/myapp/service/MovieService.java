package com.myapp.service;

import com.myapp.dao.OphimApiClient;
import com.myapp.model.Movie;
import com.myapp.model.MovieResponse;

import java.util.ArrayList;
import java.util.List;

public class MovieService {

    private final OphimApiClient apiClient;

    public MovieService() {
        this.apiClient = new OphimApiClient();
    }

    // Home screen (hero / đề xuất)
    public List<Movie> getHomeMovies() {
        return apiClient.getHomeMovies();
    }

    // Danh sách theo slug (phim-moi, phim-bo, phim-le...)
    public List<Movie> getMoviesByList(String listSlug, int page) {
        return apiClient.getMoviesByList(listSlug, page, 24);
    }

    public List<Movie> getNewMovies() {
        return getMoviesByList("phim-moi", 1);
    }

    // Chi tiết phim + episodes
    public MovieResponse getMovieDetails(String slug) {
        return apiClient.getMovieDetails(slug);
    }

    // Ảnh TMDB (backdrop/poster)
    public OphimApiClient.MovieImages getMovieImages(String slug) {
        return apiClient.getMovieImages(slug);
    }
}