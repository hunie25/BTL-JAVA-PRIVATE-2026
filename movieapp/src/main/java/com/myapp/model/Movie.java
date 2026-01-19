package com.myapp.model;

public class Movie {
    private String name;
    private String slug;
    private int year;
    private String thumbUrl;
    private String posterUrl;

    public Movie() {}

    public Movie(String name, String slug, int year, String thumbUrl, String posterUrl) {
        this.name = name;
        this.slug = slug;
        this.year = year;
        this.thumbUrl = thumbUrl;
        this.posterUrl = posterUrl;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public int getYear() { return year; }
    public String getThumbUrl() { return thumbUrl; }
    public String getPosterUrl() { return posterUrl; }

    public void setName(String name) { this.name = name; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setYear(int year) { this.year = year; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    @Override
    public String toString() {
        if (year > 0) return name + " (" + year + ")";
        return name;
    }
}
