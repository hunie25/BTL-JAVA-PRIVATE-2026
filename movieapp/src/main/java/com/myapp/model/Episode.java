package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Episode {
    @JsonProperty("server_data") private List<ServerData> serverData;
    public List<ServerData> getServerData() { return serverData; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerData {
        @JsonProperty("name") private String name;
        @JsonProperty("slug") private String slug;

        // [FIX] Thêm link_m3u8
        @JsonProperty("link_m3u8") private String linkM3u8;

        public String getName() { return name; }
        public String getLinkM3u8() { return linkM3u8; }
    }
}