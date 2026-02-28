package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Data data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public boolean isSuccess() {
        return status != null && status.equalsIgnoreCase("success");
    }

    // tiện cho code cũ gọi response.getMovie()
    public Movie getMovie() {
        return data != null ? data.item : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("item")
        private Movie item;

        public Movie getItem() { return item; }
    }
}