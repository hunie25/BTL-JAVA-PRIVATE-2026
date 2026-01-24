package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponse {
    // Dùng cho API danh sách
    private List<Movie> items;

    // Dùng cho API chi tiết
    private Movie movie;
    private List<ServerData> episodes;

    public List<Movie> getItems() { return items; }
    public void setItems(List<Movie> items) { this.items = items; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public List<ServerData> getEpisodes() { return episodes; }
    public void setEpisodes(List<ServerData> episodes) { this.episodes = episodes; }

    // Class con để hứng cấu trúc episode của Ophim
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        private List<Episode> server_data;
        public List<Episode> getServer_data() { return server_data; }
        public void setServer_data(List<Episode> server_data) { this.server_data = server_data; }
    }
}