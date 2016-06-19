package com.example.jira.remote.dto;

import java.util.List;

import com.example.jira.remote.services.ConnectionHealth;

public class JiraConnectionHealthResponse {

    public List<ConnectionHealth> connectionInstanceHealths;

    public JiraConnectionHealthResponse(List<ConnectionHealth> connectionInstanceHealths) {
        this.connectionInstanceHealths = connectionInstanceHealths;
    }
}
