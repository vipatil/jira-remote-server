package com.bmc.arsys.rx.remote;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.bmc.arsys.rx.remote.rest.RestApplication;

public class ServerRunner {

    private static final String DEFAULT_WEBAPP_PATH = "/";

    public static ServerRunner instance;

    private final int port;
    private Server server;

    public ServerRunner(int port) {
        this.port = port;
        instance = this;
    }

    public void run() throws Exception {
        server = startServer();
        try {
            // Attempt to stop the server on CTRL-C and such
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        ServerRunner.this.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            server.join();
        } finally {
            server.destroy();
        }
    }

    public Server startServer() throws Exception {
        if (server != null) {
            throw new IllegalStateException("server already started");
        }

        server = new Server(port);

        ServletContextHandler webAppContext = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        webAppContext.setContextPath(DEFAULT_WEBAPP_PATH);
        ServletHolder jerseyServlet = webAppContext.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("javax.ws.rs.Application", RestApplication.class.getName());

        server.setHandler(webAppContext);

        server.start();

        URI serverUri = server.getURI();
        if (serverUri == null) {
            serverUri = URI.create("http://127.0.0.1:" + port);
        }
        System.out.println("server ready at "
                + UriBuilder.fromUri(serverUri).path(DEFAULT_WEBAPP_PATH).build());

        return server;
    }

    public void stop() throws Exception {
        try {
            server.stop();
            server.join();
        } finally {
            server.destroy();
        }

    }
}
