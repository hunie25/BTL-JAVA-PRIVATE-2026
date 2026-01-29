package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    @JsonProperty("_id") private String id;
    @JsonProperty("name") private String name;
    @JsonProperty("slug") private String slug;
    @JsonProperty("origin_name") private String originName;
    @JsonProperty("poster_url") private String posterUrl;
    @JsonProperty("thumb_url") private String thumbUrl;
    @JsonProperty("year") private int year;
    @JsonProperty("time") private String time;
    @JsonProperty("content") private String content;

    // [FIX] Thêm trường country để hết báo đỏ
    @JsonProperty("country") private List<Category> country;
    @JsonProperty("category") private List<Category> category;

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getOriginName() { return originName; }
    public int getYear() { return year; }
    public String getTime() { return time; }
    public String getContent() { return content; }

    // [FIX] Getter
    public List<Category> getCountry() { return country; }
    public List<Category> getCategory() { return category; }

    public String getFullThumbUrl() {
        if (thumbUrl != null && thumbUrl.startsWith("http")) return thumbUrl;
        return "https://img.ophim.live/uploads/movies/" + thumbUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        @JsonProperty("name") private String name;
        public String getName() { return name; }
    }
}