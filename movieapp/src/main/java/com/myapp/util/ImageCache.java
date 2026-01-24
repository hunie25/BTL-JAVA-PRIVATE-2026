package com.myapp.util;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image get(String url, double w, double h) {
        if (url == null || url.isEmpty()) return null;
        if (cache.containsKey(url)) return cache.get(url);


        Image img = new Image(url, w, h, true, true);
        cache.put(url, img);
        return img;
    }
}