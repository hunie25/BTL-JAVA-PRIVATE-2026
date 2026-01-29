package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDetail extends Movie {
    @JsonProperty("content") private String content;
    @JsonProperty("type") private String type;
    @JsonProperty("status") private String status;
    @JsonProperty("episode_total") private String episodeTotal;
    @JsonProperty("actor") private List<String> actors;
    @JsonProperty("director") private List<String> directors;
    @JsonProperty("category") private List<Category> categories;
    @JsonProperty("country") private List<Country> countries;

    // Getters & Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEpisodeTotal() { return episodeTotal; }
    public void setEpisodeTotal(String episodeTotal) { this.episodeTotal = episodeTotal; }
    public List<String> getActors() { return actors; }
    public void setActors(List<String> actors) { this.actors = actors; }
    public List<String> getDirectors() { return directors; }
    public void setDirectors(List<String> directors) { this.directors = directors; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public List<Country> getCountries() { return countries; }
    public void setCountries(List<Country> countries) { this.countries = countries; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        @JsonProperty("name") private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country {
        @JsonProperty("name") private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}