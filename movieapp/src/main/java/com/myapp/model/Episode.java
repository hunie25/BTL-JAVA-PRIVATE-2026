package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Episode {

    @JsonProperty("server_name")
    private String serverName;

    @JsonProperty("is_ai")
    private Boolean isAi;

    @JsonProperty("server_data")
    private List<ServerData> serverData = new ArrayList<>();

    public String getServerName() { return serverName; }
    public Boolean getIsAi() { return isAi; }
    public List<ServerData> getServerData() { return serverData; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("filename")
        private String filename;

        @JsonProperty("link_embed")
        private String linkEmbed;

        @JsonProperty("link_m3u8")
        private String linkM3u8;

        public String getName() { return name; }
        public String getSlug() { return slug; }
        public String getFilename() { return filename; }
        public String getLinkEmbed() { return linkEmbed; }
        public String getLinkM3u8() { return linkM3u8; }
    }
}