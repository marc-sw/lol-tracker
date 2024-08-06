package com.fumano.crawler;

import com.fumano.crawler.exception.MissingConfigFieldException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

public record Config(String riotApiKey, String riotApiRegion, String crawlerSeedGameName, String crawlerSeedTagLine) {

    public static final String RIOTAPI_KEY_FIELD = "riotapi.key";
    public static final String RIOTAPI_REGION_FIELD = "riotapi.region";
    public static final String CRAWLER_SEED_GAMENAME = "crawler.seed.gamename";
    public static final String CRAWLER_SEED_TAGLINE = "crawler.seed.tagline";
    public static final String CRAWLER_FOLDER = Paths.get(System.getProperty("user.home"), "lol-tools", "crawler").toString();
    public static final String DATA_SOURCE_FILE = Paths.get(CRAWLER_FOLDER, "data.sqlite").toString();
    public static final String CONFIG_FILE = Paths.get(CRAWLER_FOLDER, "application.properties").toString();

    public static Config Load() {
        Properties properties = new Properties();
        File file = new File(CONFIG_FILE);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (var writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
                writer.write(String.join(System.lineSeparator(),
                        RIOTAPI_KEY_FIELD + "=",
                        RIOTAPI_REGION_FIELD + "=",
                        CRAWLER_SEED_GAMENAME + "=",
                        CRAWLER_SEED_TAGLINE + "="
                        ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (var inputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return new Config(
                    getOrThrowMissing(properties, RIOTAPI_KEY_FIELD),
                    getOrThrowMissing(properties, RIOTAPI_REGION_FIELD),
                    getOrThrowMissing(properties, CRAWLER_SEED_GAMENAME),
                    getOrThrowMissing(properties, CRAWLER_SEED_TAGLINE)
            );
        } catch (MissingConfigFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getOrThrowMissing(Properties properties, String key) throws MissingConfigFieldException {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new MissingConfigFieldException(key, CONFIG_FILE);
        }
        return value;
    }
}
