package com.minicraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.scene.image.Image;

public final class Resources {
    private Resources() {
    }

    public static Image image(String path) {
        String resourcePath = normalize(path);

        try (InputStream input = Resources.class.getClassLoader().getResourceAsStream(resourcePath)) {
           File file = new File("resources/" + path);
            return new Image(file.toURI().toString());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close image resource: " + resourcePath, e);
        }
    }

    public static String mediaUrl(String path) {
        String resourcePath = normalize(path);
        URL resource = Resources.class.getClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return resource.toExternalForm();
    }

    private static String normalize(String path) {
        String normalized = path.replace('\\', '/');
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }
}
