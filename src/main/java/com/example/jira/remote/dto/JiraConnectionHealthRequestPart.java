package com.example.jira.remote.dto;

import com.example.jira.remote.services.ConnectionHealth;
import com.example.jira.remote.services.JiraConnection;
import com.example.jira.remote.services.JiraService;

public class JiraConnectionHealthRequestPart {

    public String id;
    public JiraConnection properties;

    public ConnectionHealth performOn(JiraService service) {
        return service.checkHealth(id, properties);
    }
}
