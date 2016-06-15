package com.bmc.arsys.rx.jira.remote.rest;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.bmc.arsys.rx.jira.remote.dto.CreateIssueRequest;
import com.bmc.arsys.rx.jira.remote.dto.JiraConnectionHealthRequest;
import com.bmc.arsys.rx.jira.remote.dto.JiraConnectionHealthRequestPart;
import com.bmc.arsys.rx.jira.remote.dto.JiraConnectionHealthResponse;
import com.bmc.arsys.rx.jira.remote.services.ConnectionHealth;
import com.bmc.arsys.rx.jira.remote.services.JiraService;
import com.bmc.arsys.rx.remote.dto.descriptor.ActionOutput;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path(JiraResource.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JiraResource {

    public static final String BASE_PATH = "jira";
    public static final String CREATE_ISSUE_PATH = "createIssue";

    @POST
    @Path(JiraResource.CREATE_ISSUE_PATH)
    public ActionOutput createIssue(CreateIssueRequest request) {
        JiraService service = new JiraService();
        BasicIssue issue = service.createIssue(
            request.connectionInstanceId,
            request.inputs.projectKey,
            request.inputs.issueType,
            request.inputs.summary,
            request.inputs.description,
            request.inputs.assignee,
            request.inputs.affectsVersions,
            request.inputs.fixVersions,
            ImmutableMap.<String, Object>builder()
                        .put("Customer Name", request.inputs.customerName)
                        .put("Severity", request.inputs.severity)
                        .build());
        /**
         * Need to handle response that server understands
         */
        ActionOutput output = new ActionOutput(){{
            outputs.put("issueID", issue.getKey());
        }};

        return output;
    }

    @POST
    @Path("connectioninstancehealth")
    public JiraConnectionHealthResponse checkHealths(JiraConnectionHealthRequest request) {
        JiraService service = new JiraService();
        List<ConnectionHealth> healths;
        if (request.connectionInstances == null) {
            healths = Collections.emptyList();
        } else {
            healths = request.connectionInstances
                .parallelStream()
                .map((JiraConnectionHealthRequestPart requestPart) -> requestPart.performOn(service))
                .collect(Collectors.toList());
        }
        return new JiraConnectionHealthResponse(healths);
    }
}
