package com.wohl.posthouse.config;

import lombok.Data;

/**
 * @Author 吴子豪
 */
@Data
public class PosthouseConfig {
    public Server server;
    @Data
    public static class Server {
        private int port;
        private Persistence persistence;

        @Data
        public static class Persistence {
            private boolean enable;
            // retain, discard
            private boolean retain;
        }
    }
}
