package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    private String name;

    @JsonProperty("origin_name")
    private String originName; // Tên tiếng Anh

    private String slug;

    @JsonProperty("thumb_url")
    private String thumbUrl;

    @JsonProperty("content")
    private String content;

    @JsonProperty("time")
    private String time; // Thời lượng

    private int year;

    // Danh sách thể loại và quốc gia (API trả về dạng Object list)
    private List<Category> category;
    private List<Category> country;

    // --- Getters & Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginName() { return originName; }
    public void setOriginName(String originName) { this.originName = originName; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public List<Category> getCategory() { return category; }
    public void setCategory(List<Category> category) { this.category = category; }

    public List<Category> getCountry() { return country; }
    public void setCountry(List<Category> country) { this.country = country; }

    public String getFullThumbUrl() {
        if (thumbUrl != null && !thumbUrl.startsWith("http")) {
            return "https://img.ophim1.com/uploads/movies/" + thumbUrl;
        }
        return thumbUrl;
    }

    // Class phụ để hứng category/country
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        public String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}