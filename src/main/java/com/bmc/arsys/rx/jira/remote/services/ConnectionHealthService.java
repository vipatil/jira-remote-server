package com.bmc.arsys.rx.jira.remote.services;

import com.atlassian.jira.rest.client.api.JiraRestClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ConnectionHealthService {

    private final JiraConnectorRestClientFactory clientFactory;


    public ConnectionHealthService() {
        this.clientFactory = JiraConnectorRestClientFactory.instance();
    }

    public void assessHealth(JiraConnection connection) throws IOException {
        try (JiraRestClient client = clientFactory.create(connection)) {
            client.getSessionClient().getCurrentSession().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }
}
