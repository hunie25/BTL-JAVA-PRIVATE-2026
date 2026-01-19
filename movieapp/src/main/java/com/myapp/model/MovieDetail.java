package com.myapp.model;

import java.util.ArrayList;
import java.util.List;

public class MovieDetail {
    private String name;
    private String originName;
    private String content;
    private int year;
    private String slug;
    private List<Episode> episodes = new ArrayList<>();

    public String getName() { return name; }
    public String getOriginName() { return originName; }
    public String getContent() { return content; }
    public int getYear() { return year; }
    public String getSlug() { return slug; }
    public List<Episode> getEpisodes() { return episodes; }

    public void setName(String name) { this.name = name; }
    public void setOriginName(String originName) { this.originName = originName; }
    public void setContent(String content) { this.content = content; }
    public void setYear(int year) { this.year = year; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setEpisodes(List<Episode> episodes) { this.episodes = episodes; }
}
