package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Hứng response danh sách phim (Home)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    @JsonProperty("status") private boolean status;
    @JsonProperty("items") private List<Movie> items;
    @JsonProperty("pathImage") private String pathImage;

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public List<Movie> getItems() { return items; }
    public void setItems(List<Movie> items) { this.items = items; }
    public String getPathImage() { return pathImage; }
    public void setPathImage(String pathImage) { this.pathImage = pathImage; }
}