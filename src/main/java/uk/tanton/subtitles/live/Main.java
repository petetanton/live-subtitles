package uk.tanton.subtitles.live;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] rawArgs) throws InterruptedException, CmdLineException {
        final Args args = new Args();
        final CmdLineParser clp = new CmdLineParser(args);
        clp.parseArgument(rawArgs);

        LOG.info("Starting up...");
        final HttpServer httpServer = new HttpServer(new SubtitleCache(), args);
        httpServer.start();
        LOG.info("web server started");

    }
}
