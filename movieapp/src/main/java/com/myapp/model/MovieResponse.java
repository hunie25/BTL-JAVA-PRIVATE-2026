package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponse {
    @JsonProperty("status") private boolean status;
    @JsonProperty("movie") private Movie movie; // Dùng Movie (vì đã thêm đủ trường ở trên)
    @JsonProperty("episodes") private List<Episode> episodes;

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public List<Episode> getEpisodes() { return episodes; }
    public void setEpisodes(List<Episode> episodes) { this.episodes = episodes; }
}