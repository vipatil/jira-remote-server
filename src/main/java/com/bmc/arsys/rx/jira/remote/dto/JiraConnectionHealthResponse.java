package com.bmc.arsys.rx.jira.remote.dto;

import com.bmc.arsys.rx.jira.remote.services.ConnectionHealth;

import java.util.List;

public class JiraConnectionHealthResponse {

    public List<ConnectionHealth> connectionInstanceHealths;


    public JiraConnectionHealthResponse(List<ConnectionHealth> connectionInstanceHealths) {
        this.connectionInstanceHealths = connectionInstanceHealths;
    }
}
