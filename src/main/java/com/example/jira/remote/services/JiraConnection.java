package com.example.jira.remote.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraConnection {

    public String url;
    public String login;
    public String password;
}
