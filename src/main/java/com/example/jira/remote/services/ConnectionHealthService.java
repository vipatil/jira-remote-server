package com.example.jira.remote.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public class ConnectionHealthService {

    private final JiraConnectorRestClientFactory clientFactory;

    public ConnectionHealthService() {
        clientFactory = JiraConnectorRestClientFactory.instance();
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
