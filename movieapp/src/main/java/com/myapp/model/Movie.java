package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("origin_name")
    private String originName;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("thumb_url")
    private String thumbUrl;

    @JsonProperty("poster_url")
    private String posterUrl;

    @JsonProperty("content")
    private String content;

    @JsonProperty("time")
    private String time;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("quality")
    private String quality;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("episode_current")
    private String episodeCurrent;

    @JsonProperty("episode_total")
    private String episodeTotal;

    @JsonProperty("type")
    private String type;

    @JsonProperty("status")
    private String status;

    @JsonProperty("category")
    private List<Taxonomy> category = new ArrayList<>();

    @JsonProperty("country")
    private List<Taxonomy> country = new ArrayList<>();

    @JsonProperty("episodes")
    private List<Episode> episodes = new ArrayList<>();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOriginName() { return originName; }
    public String getSlug() { return slug; }
    public String getThumbUrl() { return thumbUrl; }
    public String getPosterUrl() { return posterUrl; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public Integer getYear() { return year; }
    public String getQuality() { return quality; }
    public String getLang() { return lang; }
    public String getEpisodeCurrent() { return episodeCurrent; }
    public String getEpisodeTotal() { return episodeTotal; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public List<Taxonomy> getCategory() { return category; }
    public List<Taxonomy> getCountry() { return country; }
    public List<Episode> getEpisodes() { return episodes; }

    public String getFullThumbUrl() {
        return toFullImageUrl(thumbUrl);
    }

    public String getFullPosterUrl() {
        return toFullImageUrl(posterUrl);
    }

    private String toFullImageUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String v = raw.trim();

        if (v.startsWith("http://") || v.startsWith("https://")) return v;

        if (v.startsWith("/uploads/")) {
            return "https://ophim1.com" + v;
        }

        if (v.startsWith("/")) {
            return "https://img.ophim.cc/uploads/movies" + v;
        }

        return "https://img.ophim.cc/uploads/movies/" + v;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Taxonomy {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOriginName(String originName) { this.originName = originName; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setYear(Integer year) { this.year = year; }
}