package com.bmc.arsys.rx.jira.remote.dto;

import java.util.List;

import com.bmc.arsys.rx.jira.remote.services.ConnectionHealth;

public class JiraConnectionHealthResponse {

    public List<ConnectionHealth> connectionInstanceHealths;

    public JiraConnectionHealthResponse(List<ConnectionHealth> connectionInstanceHealths) {
        this.connectionInstanceHealths = connectionInstanceHealths;
    }
}
