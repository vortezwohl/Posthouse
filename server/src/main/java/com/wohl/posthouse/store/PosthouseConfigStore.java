package com.wohl.posthouse.store;

import com.wohl.posthouse.config.PosthouseConfig;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @Author 吴子豪
 */
public class PosthouseConfigStore {
    public static PosthouseConfig POSTHOUSE_CONFIG;
    private static File MEM;

    public static File getMemoryFile() throws URISyntaxException {
        if (MEM == null)
            MEM = Paths.get(Objects.requireNonNull(PosthouseConfigStore.class.getClassLoader().getResource(".memory")).toURI()).toFile();
        return MEM;
    }

    public static void set(PosthouseConfig config) {
        POSTHOUSE_CONFIG = config;
    }
}
