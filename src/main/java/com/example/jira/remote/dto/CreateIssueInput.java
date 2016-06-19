package com.example.jira.remote.dto;

public class CreateIssueInput {
    public String projectKey;
    public String issueType;
    public String summary;
    public String description;
    public String assignee;
    public String affectsVersions;
    public String fixVersions;
    public String customerName;
    public String severity;
}
