package com.example.remote;

import org.kohsuke.args4j.Option;

public class CliOptions {

    @Option(name = "-h", aliases = { "-help" })
    public boolean printHelp;

    @Option(name = "-p", aliases = { "-port" }, usage = "HTTP port")
    public int port = 8000;

}
