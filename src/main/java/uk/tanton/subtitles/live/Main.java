package uk.tanton.subtitles.live;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(final String[] args) throws InterruptedException {
        LOG.info("Starting up...");
        final HttpServer httpServer = new HttpServer(new SubtitleCache());
        httpServer.start();
        LOG.info("web server started");

    }
}
