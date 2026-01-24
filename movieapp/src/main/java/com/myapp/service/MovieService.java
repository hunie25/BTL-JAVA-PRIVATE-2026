package com.myapp.service;

import com.myapp.dao.OphimApiClient;
import com.myapp.model.Episode;
import com.myapp.model.Movie;
import java.util.List;

public class MovieService {

    // Service sở hữu một đối tượng DAO
    private final OphimApiClient apiClient;

    public MovieService() {
        this.apiClient = new OphimApiClient();
    }

    public List<Movie> getNewMovies(int page) {
        // Có thể thêm logic nghiệp vụ ở đây (ví dụ: lọc phim, cache...)
        // Hiện tại chỉ gọi thẳng xuống DAO
        return apiClient.getNewMovies(page);
    }

    public List<Episode> getEpisodes(String slug) {
        return apiClient.getEpisodes(slug);
    }
}