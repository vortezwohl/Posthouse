package com.wohl.posthouse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wohl.posthouse.config.PosthouseConfig;
import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.server.Server;
import lombok.extern.log4j.Log4j2;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class Main {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, FileNotFoundException, JsonProcessingException {
        Yaml yaml = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        JentitiContext.init();
        Path config = Paths.get(System.getProperty("user.dir") + "\\posthouse.yml");
        if (!Files.exists(config))
            config = Paths.get(Objects.requireNonNull(Main.class.getClassLoader().getResource("posthouse.yml")).toURI());
        Map<String, Object> posthouseConfigMap = yaml.load(new FileInputStream(config.toFile()));
        String posthouseConfigJson = mapper.writeValueAsString(posthouseConfigMap);
        PosthouseConfig posthouseConfig = mapper.readValue(posthouseConfigJson, PosthouseConfig.class);
        int port = posthouseConfig.getServer().getPort();
        Server.run(port).sync();
        log.info("Posthouse boots up on port " + port);
    }
}
