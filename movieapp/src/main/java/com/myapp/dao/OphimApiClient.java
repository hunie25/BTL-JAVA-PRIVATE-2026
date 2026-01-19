package com.myapp.dao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OphimApiClient {
    private final HttpClient client = HttpClient.newHttpClient();

    public String getJson(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("User-Agent", "Mozilla/5.0")
                .build();

        return client.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}
