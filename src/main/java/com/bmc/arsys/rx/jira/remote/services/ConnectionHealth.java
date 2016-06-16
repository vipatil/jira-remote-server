package com.bmc.arsys.rx.jira.remote.services;

import javax.ws.rs.core.Response;

import com.atlassian.jira.rest.client.api.RestClientException;

public class ConnectionHealth {

    public String id;
    public ConnectionHealthState state;
    public String message;

    public ConnectionHealth(String id, ConnectionHealthState state, String message) {
        this.id = id;
        this.state = state;
        this.message = message;
    }

    public ConnectionHealth(String id, Throwable t) {
        this.id = id;
        state = ConnectionHealthState.CONNECTION_FAILED;

        if (t instanceof RestClientException) {
            RestClientException restClientException = (RestClientException) t;
            Integer statusCode = restClientException.getStatusCode().orNull();
            if (statusCode != null
                    && (Response.Status.FORBIDDEN.getStatusCode() == statusCode || Response.Status.UNAUTHORIZED.getStatusCode() == statusCode)) {
                state = ConnectionHealthState.UNAUTHORIZED;
            }
        }

        message = t.getMessage();
    }
}
