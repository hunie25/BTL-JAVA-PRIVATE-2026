package com.myapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    @JsonProperty("status")
    private String status; // "success" / "error"

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public boolean isSuccess() {
        return status != null && status.equalsIgnoreCase("success");
    }
}