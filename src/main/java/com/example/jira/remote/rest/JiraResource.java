package com.example.jira.remote.rest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.example.jira.remote.dto.CreateIssueRequest;
import com.example.jira.remote.dto.JiraConnectionHealthRequest;
import com.example.jira.remote.dto.JiraConnectionHealthRequestPart;
import com.example.jira.remote.dto.JiraConnectionHealthResponse;
import com.example.jira.remote.services.ConnectionHealth;
import com.example.jira.remote.services.JiraService;
import com.example.remote.dto.descriptor.ActionOutput;
import com.google.common.collect.ImmutableMap;

@Path(JiraResource.BASE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JiraResource {

    public static final String BASE_PATH = "jira";
    public static final String CREATE_ISSUE_PATH = "createIssue";

    /**
     * To create Bug in JIRA.
     *
     * @param issueDetails
     *            Details about Jira Bug.
     * @return ID of the newly create Bug.
     */
    @POST
    @Path(JiraResource.CREATE_ISSUE_PATH)
    public ActionOutput createIssue(CreateIssueRequest issueDetails) {
        JiraService service = new JiraService();
        BasicIssue issue = service.createIssue(
                issueDetails.connectionInstanceId,
                issueDetails.inputs.projectKey,
                issueDetails.inputs.issueType,
                issueDetails.inputs.summary,
                issueDetails.inputs.description,
                issueDetails.inputs.assignee,
                issueDetails.inputs.affectsVersions,
                issueDetails.inputs.fixVersions,
                ImmutableMap.<String, Object> builder().put("Customer Name",
                        issueDetails.inputs.customerName).put("Severity",
                        issueDetails.inputs.severity).build());
        /**
         * Need to handle response that server understands
         */
        ActionOutput output = new ActionOutput() {
            {
                outputs.put("issueID", issue.getKey());
            }
        };

        return output;
    }

    /**
     * Check Health of JIRA connector
     *
     * @param request
     *            list of JIRA connection instances.
     * @return Details about status
     */
    @POST
    @Path("connectioninstancehealth")
    public JiraConnectionHealthResponse checkHealths(JiraConnectionHealthRequest request) {
        JiraService service = new JiraService();
        List<ConnectionHealth> healths;
        if (request.connectionInstances == null) {
            healths = Collections.emptyList();
        } else {
            healths = request.connectionInstances.parallelStream().map(
                    (JiraConnectionHealthRequestPart requestPart) -> requestPart.performOn(service)).collect(
                    Collectors.toList());
        }
        return new JiraConnectionHealthResponse(healths);
    }
}
