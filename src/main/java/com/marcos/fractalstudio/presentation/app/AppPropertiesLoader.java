package com.marcos.fractalstudio.presentation.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Loads desktop runtime settings from the classpath.
 */
public final class AppPropertiesLoader {

    /**
     * Reads the application properties file and maps it to {@link AppProperties}.
     *
     * @return immutable runtime settings
     */
    public AppProperties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/app.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("app.properties was not found on the classpath.");
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not load app.properties", exception);
        }

        return new AppProperties(
                properties.getProperty("app.title", "Fractal Render Studio"),
                Double.parseDouble(properties.getProperty("app.window.width", "1440")),
                Double.parseDouble(properties.getProperty("app.window.height", "900")),
                properties.getProperty("app.storage.root", "storage"),
                Integer.parseInt(properties.getProperty("app.preview.threads", "2")),
                Integer.parseInt(properties.getProperty("app.render.threads", "1"))
        );
    }
}
