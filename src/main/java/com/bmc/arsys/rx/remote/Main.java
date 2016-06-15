package com.bmc.arsys.rx.remote;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;


public class Main {

    private final CliOptions options = new CliOptions();

    public void run() throws Exception {
        new ServerRunner(options.port).run();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        CmdLineParser parser = new CmdLineParser(main.options);
        try {
            parser.parseArgument(args);
            if (!main.options.printHelp) {
                main.run();
                System.exit(0);
            }

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
        }

        parser.printUsage(System.err);
    }
}
