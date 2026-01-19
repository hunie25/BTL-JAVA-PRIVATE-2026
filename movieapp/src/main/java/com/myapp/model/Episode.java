package com.myapp.model;

public class Episode {
    private String name;     // Tập 1
    private String fileUrl;  // link mp4/m3u8

    public Episode() {}

    public Episode(String name, String fileUrl) {
        this.name = name;
        this.fileUrl = fileUrl;
    }

    public String getName() { return name; }
    public String getFileUrl() { return fileUrl; }

    public void setName(String name) { this.name = name; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    @Override
    public String toString() {
        return name;
    }
}