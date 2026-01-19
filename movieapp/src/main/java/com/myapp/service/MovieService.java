package com.myapp.service;

import com.myapp.dao.OphimApiClient;
import com.myapp.model.Episode;
import com.myapp.model.Movie;
import com.myapp.model.MovieDetail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class MovieService {
    private static final String BASE = "https://ophim1.com";

    private final OphimApiClient api = new OphimApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // 1) Danh sách phim mới cập nhật
    public List<Movie> getLatest(int page) throws Exception {
        String url = BASE + "/danh-sach/phim-moi-cap-nhat?page=" + page;
        String json = api.getJson(url);

        JsonNode root = mapper.readTree(json);
        JsonNode items = root.path("items");

        String pathImage = root.path("pathImage").asText("");

        List<Movie> res = new ArrayList<>();
        if (items.isArray()) {
            for (JsonNode it : items) {
                Movie m = new Movie();
                m.setName(it.path("name").asText(""));
                m.setSlug(it.path("slug").asText(""));
                m.setYear(it.path("year").asInt(0));

                String thumb = it.path("thumb_url").asText("");
                String poster = it.path("poster_url").asText("");

                m.setThumbUrl(joinImage(pathImage, thumb));
                m.setPosterUrl(joinImage(pathImage, poster));

                if (!m.getSlug().isBlank()) res.add(m);
            }
        }
        return res;
    }

    // 2) Chi tiết phim + tập (ĐÚNG format Ophim: episodes -> server_data -> link_m3u8/link_embed)
    public MovieDetail getDetail(String slug) throws Exception {
        String url = BASE + "/phim/" + slug;
        String json = api.getJson(url);

        JsonNode root = mapper.readTree(json);

        JsonNode movieNode = root.path("movie");
        MovieDetail d = new MovieDetail();
        d.setName(movieNode.path("name").asText(""));
        d.setOriginName(movieNode.path("origin_name").asText(""));
        d.setContent(stripHtml(movieNode.path("content").asText("")));
        d.setYear(movieNode.path("year").asInt(0));
        d.setSlug(movieNode.path("slug").asText(slug));

        List<Episode> eps = new ArrayList<>();
        JsonNode episodes = root.path("episodes");

        if (episodes.isArray()) {
            for (JsonNode server : episodes) {
                String serverName = server.path("server_name").asText(""); // nếu muốn show server
                JsonNode serverData = server.path("server_data");

                if (serverData.isArray()) {
                    for (JsonNode ep : serverData) {
                        String epName = ep.path("name").asText("Tập");
                        String slugEp = ep.path("slug").asText("");

                        // Ophim hay dùng link_m3u8 hoặc link_embed (file_url thường rỗng)
                        String link = ep.path("link_m3u8").asText("");
                        if (link.isBlank()) link = ep.path("link_embed").asText("");
                        if (link.isBlank()) link = ep.path("file_url").asText(""); // fallback cuối

                        if (!link.isBlank()) {
                            // Nếu Episode của bạn chỉ có (name, link) thì giữ vậy:
                            Episode e = new Episode(epName, link);

                            // Nếu Episode của bạn có thêm field serverName/slug thì bạn tự set thêm ở đây
                            // ví dụ:
                            // e.setServerName(serverName);
                            // e.setSlug(slugEp);

                            eps.add(e);
                        }
                    }
                }
            }
        }

        d.setEpisodes(eps);
        return d;
    }

    private String stripHtml(String s) {
        return s.replaceAll("<[^>]*>", "").trim();
    }

    private String joinImage(String pathImage, String url) {
        if (url == null) return "";
        if (url.startsWith("http")) return url;
        if (pathImage == null || pathImage.isBlank()) return url;
        if (pathImage.endsWith("/") && url.startsWith("/")) return pathImage + url.substring(1);
        if (!pathImage.endsWith("/") && !url.startsWith("/")) return pathImage + "/" + url;
        return pathImage + url;
    }
}
