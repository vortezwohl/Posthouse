package com.wohl.posthouse.util.impl;

import com.wohl.posthouse.store.PosthouseConfigStore;
import com.wohl.posthouse.util.intf.DataPersistenceProcessor;
import org.jentiti.annotation.Singleton;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

@Singleton
public class LocalDataPersistenceProcessor implements DataPersistenceProcessor {
    @Override
    public void append(String msg) {
        try (FileWriter writer = new FileWriter(PosthouseConfigStore.getMemoryFile(), true)) {
            writer.write(msg + "\n");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void discard() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(PosthouseConfigStore.getMemoryFile())) {
            fileOutputStream.getChannel().truncate(0);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
