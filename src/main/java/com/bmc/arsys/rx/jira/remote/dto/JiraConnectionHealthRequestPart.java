package com.bmc.arsys.rx.jira.remote.dto;

import com.bmc.arsys.rx.jira.remote.services.ConnectionHealth;
import com.bmc.arsys.rx.jira.remote.services.JiraConnection;
import com.bmc.arsys.rx.jira.remote.services.JiraService;

public class JiraConnectionHealthRequestPart {

    public String id;
    public JiraConnection properties;

    public ConnectionHealth performOn(JiraService service) {
        return service.checkHealth(id, properties);
    }
}
