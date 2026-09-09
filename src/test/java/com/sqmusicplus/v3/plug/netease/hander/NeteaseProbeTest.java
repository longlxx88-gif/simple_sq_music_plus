package com.sqmusicplus.v3.plug.netease.hander;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class NeteaseProbeTest {
    @Test
    void stalledProviderIsBoundedAndNextProviderCanBeProbed() throws Exception {
        var executor = Executors.newCachedThreadPool();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/slow/inner/version", exchange -> {
            try { Thread.sleep(5000); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        server.createContext("/ok/inner/version", exchange -> {
            byte[] body = "{\"code\":200,\"data\":{}}".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            var api = new SQNeteaseCloudMusicInfo();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            api.init(base + "/slow");
            assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                    assertThrows(IllegalStateException.class, () -> api.innerVersion(150)));
            api.init(base + "/ok");
            assertEquals(200, api.innerVersion(1000).getIntValue("code"));
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
