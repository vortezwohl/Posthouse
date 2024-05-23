package com.wohl.posthouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wohl.posthouse.config.PosthouseConfig;
import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.server.Server;
import com.wohl.posthouse.store.PosthouseConfigStore;
import com.wohl.posthouse.store.RemoteDictStore;
import com.wohl.posthouse.util.impl.LocalDataPersistenceProcessor;
import com.wohl.posthouse.util.intf.DataPersistenceProcessor;
import com.wohl.posthouse.util.intf.RemoteDictInstructionAnalyser;
import lombok.extern.log4j.Log4j2;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class Main {
    public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException {
        // initialize all
        Yaml yaml = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        JentitiContext.init();

        // load configurations
        Path config = Paths.get(System.getProperty("user.dir") + "\\posthouse.yaml");
        if (!Files.exists(config))
            config = Paths.get(System.getProperty("user.dir") + "\\posthouse.yml");
        if (!Files.exists(config))
            config = Paths.get(Objects.requireNonNull(Main.class.getClassLoader().getResource("posthouse.yml")).toURI());
        Map<String, Object> posthouseConfigMap = yaml.load(new FileInputStream(config.toFile()));
        String posthouseConfigJson = mapper.writeValueAsString(posthouseConfigMap);
        PosthouseConfig posthouseConfig = mapper.readValue(posthouseConfigJson, PosthouseConfig.class);
        PosthouseConfigStore.set(posthouseConfig);

        // restore previous data
        DataPersistenceProcessor localDataPersistenceProcessor = (DataPersistenceProcessor) JentitiContext.ctx().get(LocalDataPersistenceProcessor.class);
        BufferedReader reader = new BufferedReader(new FileReader(PosthouseConfigStore.getMemoryFile()));
        RemoteDictInstructionAnalyser remoteDictInstructionAnalyser = (RemoteDictInstructionAnalyser) JentitiContext.ctx().get("remoteDictInstructionAnalyser");
        if (PosthouseConfigStore.POSTHOUSE_CONFIG.getServer().getPersistence().isEnable()) {
            if (!PosthouseConfigStore.POSTHOUSE_CONFIG.getServer().getPersistence().isRetain())
                localDataPersistenceProcessor.discard();
            while (true) {
                String msgBody = reader.readLine();
                if (msgBody != null)
                    remoteDictInstructionAnalyser.exec(msgBody);
                else
                    break;
            }
            log.info("Data restored");
            log.debug("key=" + RemoteDictStore.keySet);
        } else {
            if (!PosthouseConfigStore.POSTHOUSE_CONFIG.getServer().getPersistence().isRetain())
                localDataPersistenceProcessor.discard();
        }


        // boot up the server
        int port = PosthouseConfigStore.POSTHOUSE_CONFIG.getServer().getPort();
        Server.run(port).sync();
        log.info("Posthouse boots up on port " + port);
    }
}
