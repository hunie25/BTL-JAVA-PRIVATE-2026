package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Episode {
    private String name;
    private String slug;

    @JsonProperty("link_m3u8") // Map chính xác key này
    private String linkM3u8;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getLinkM3u8() { return linkM3u8; }
    public void setLinkM3u8(String linkM3u8) { this.linkM3u8 = linkM3u8; }
}