package com.myapp.util;

import javafx.scene.image.Image;

import java.util.LinkedHashMap;
import java.util.Map;

public class ImageCache {
    private static final int MAX = 120; // cache tối đa 120 ảnh
    private static final Map<String, Image> CACHE = new LinkedHashMap<>(MAX, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > MAX;
        }
    };

    private ImageCache() {}

    public static Image get(String url) {
        if (url == null || url.isBlank()) return null;
        synchronized (CACHE) {
            return CACHE.get(url);
        }
    }

    public static void put(String url, Image img) {
        if (url == null || url.isBlank() || img == null) return;
        synchronized (CACHE) {
            CACHE.put(url, img);
        }
    }
}
