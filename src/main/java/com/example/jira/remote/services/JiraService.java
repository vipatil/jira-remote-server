package com.example.jira.remote.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.EntityHelper;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class JiraService {

    private final JiraConnectorRestClientFactory clientFactory;

    public JiraService(JiraConnectorRestClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public JiraService() {
        this(JiraConnectorRestClientFactory.instance());
    }

    public ConnectionHealth checkHealth(String id, JiraConnection connection) {
        try (JiraRestClient client = clientFactory.create(connection)) {
            client.getSessionClient().getCurrentSession().get();
            return new ConnectionHealth(id, ConnectionHealthState.CONNECTION_SUCCESSFUL, null);

        } catch (InterruptedException e) {
            return new ConnectionHealth(id, ConnectionHealthState.CONNECTION_FAILED, null);

        } catch (ExecutionException e) {
            Throwable t = e.getCause() == null ? e : e.getCause();
            return new ConnectionHealth(id, t);

        } catch (Exception e) {
            return new ConnectionHealth(id, ConnectionHealthState.CONNECTION_FAILED, e.toString());
        }
    }

    public BasicIssue createIssue(JiraConnection connection, String projectKey,
            String issueTypeName, String summary, String description, String assignee,
            String affectsVersions, String fixVersions, Map<String, Object> customFields) {
        try {
            try (JiraRestClient restClient = clientFactory.create(connection)) {
                CimProject cimProject = getCreateIssueMetadata(restClient, projectKey);
                CimIssueType issueType = EntityHelper.findEntityByName(cimProject.getIssueTypes(),
                        issueTypeName);

                IssueInputBuilder issueBuilder = new IssueInputBuilder(cimProject, issueType,
                        summary);
                issueBuilder.setDescription(description);

                setAssignee(restClient, issueBuilder, connection, assignee);
                setAffectsVersions(issueBuilder, issueType, affectsVersions);
                setFixVersions(issueBuilder, issueType, fixVersions);
                setCustomFields(issueBuilder, issueType, customFields);

                return restClient.getIssueClient().createIssue(issueBuilder.build()).claim();
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
    }

    private void setCustomFields(IssueInputBuilder issueBuilder, CimIssueType issueType,
            Map<String, Object> customFields) {
        for (Map.Entry<String, Object> customField : customFields.entrySet()) {
            Object value = customField.getValue();
            setCustomField(issueBuilder, issueType, customField.getKey(),
                    Objects.toString(value, null));
        }
    }

    private void setAffectsVersions(IssueInputBuilder issueBuilder, CimIssueType issueType,
            String affectsVersions) {
        if (Strings.isNullOrEmpty(affectsVersions)) {
            return;
        }

        List<String> versionNames = parseListOfStrings(affectsVersions);
        CimFieldInfo fieldInfo = issueType.getField(IssueFieldId.AFFECTS_VERSIONS_FIELD);
        if (fieldInfo != null) {
            List<Version> issueAffectsVersions = getVersionsByName(versionNames, fieldInfo);
            issueBuilder.setAffectedVersions(issueAffectsVersions);
        }
    }

    private void setFixVersions(IssueInputBuilder issueBuilder, CimIssueType issueType,
            String fixVersions) {
        if (Strings.isNullOrEmpty(fixVersions)) {
            return;
        }

        List<String> versionNames = parseListOfStrings(fixVersions);
        CimFieldInfo fieldInfo = issueType.getField(IssueFieldId.FIX_VERSIONS_FIELD);
        if (fieldInfo != null) {
            List<Version> issueFixVersions = getVersionsByName(versionNames, fieldInfo);
            issueBuilder.setFixVersions(issueFixVersions);
        }
    }

    private List<Version> getVersionsByName(List<String> versionNames, CimFieldInfo fieldInfo) {
        List<Version> issueAffectsVersions = new ArrayList<>();
        List<Version> allowedVersions = castToList(fieldInfo.getAllowedValues(), Version.class);
        for (String versionName : versionNames) {
            if (containsVersion(allowedVersions, versionName)) {
                issueAffectsVersions.add(getVersionByName(allowedVersions, versionName));
            } else {
                throw new BadRequestException("No such version '" + versionName + "'");
            }
        }
        return issueAffectsVersions;
    }

    private static boolean containsVersion(List<Version> versions, String versionName) {
        for (Version version : versions) {
            if (versionName.equals(version.getName())) {
                return true;
            }
        }
        return false;
    }

    private static Version getVersionByName(List<Version> versions, String versionName) {
        for (Version version : versions) {
            if (versionName.equals(version.getName())) {
                return version;
            }
        }
        return null;
    }

    private void setCustomField(IssueInputBuilder issueBuilder, CimIssueType issueType,
            String fieldName, String fieldValue) {
        CimFieldInfo cimFieldInfo = getCustomField(issueType, fieldName);
        if (cimFieldInfo != null) {
            List<CustomFieldOption> allowedValues = castToList(cimFieldInfo.getAllowedValues(),
                    CustomFieldOption.class);
            if (Strings.isNullOrEmpty(fieldValue)) {
                CustomFieldOption firstOption = allowedValues.get(0);
                issueBuilder.setFieldValue(cimFieldInfo.getId(), firstOption.getValue());
            } else {
                if ("array".equalsIgnoreCase(cimFieldInfo.getSchema().getType())) {
                    List<CustomFieldOption> fieldOptions;
                    if (fieldValue.contains(",")) {
                        List<String> fieldValues = parseListOfStrings(fieldValue);
                        fieldOptions = new ArrayList<>(fieldValues.size());
                        for (String oneFieldValue : fieldValues) {
                            CustomFieldOption fieldOption = findFieldOptionOrFail(cimFieldInfo,
                                    oneFieldValue);
                            fieldOptions.add(fieldOption);
                        }
                    } else {
                        CustomFieldOption fieldOption = findFieldOptionOrFail(cimFieldInfo,
                                fieldValue);
                        fieldOptions = Collections.singletonList(fieldOption);
                    }
                    issueBuilder.setFieldValue(cimFieldInfo.getId(), fieldOptions);
                } else {
                    CustomFieldOption fieldOption = findFieldOptionOrFail(cimFieldInfo, fieldValue);
                    issueBuilder.setFieldValue(cimFieldInfo.getId(), fieldOption);
                }
            }
        }
    }

    private static List<String> parseListOfStrings(String value) {
        return Strings.isNullOrEmpty(value) ? null : Arrays.asList(value.split(",", -1));
    }

    private static CustomFieldOption findFieldOptionOrFail(CimFieldInfo fieldInfo, String value) {
        CustomFieldOption fieldOption = findFieldOption(fieldInfo, value);
        if (fieldOption == null) {
            throw new BadRequestException("Value '" + value + "' is not allowed for field "
                    + fieldInfo.getId() + " / " + fieldInfo.getName());
        }
        return fieldOption;
    }

    private static CustomFieldOption findFieldOption(CimFieldInfo fieldInfo, String value) {
        for (Object o : fieldInfo.getAllowedValues()) {
            CustomFieldOption customFieldOption = (CustomFieldOption) o;
            if (Objects.equals(value, customFieldOption.getValue())) {
                return customFieldOption;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castToList(Iterable rawOptions, Class<T> entityType) {
        return Lists.newArrayList(rawOptions);
    }

    private static CimFieldInfo getCustomField(CimIssueType issueType, String name) {
        for (CimFieldInfo field : issueType.getFields().values()) {
            if (name.equalsIgnoreCase(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private void setAssignee(JiraRestClient restClient, IssueInputBuilder issueBuilder,
            JiraConnection connection, String assignee) {
        String username = defaultIfEmpty(assignee, connection.login);
        BasicUser user = getUser(restClient, username);
        issueBuilder.setAssignee(user);
    }

    private BasicUser getUser(JiraRestClient restClient, String username) {
        return restClient.getUserClient().getUser(username).claim();
    }

    private static String defaultIfEmpty(String value, String defaultValue) {
        return Strings.isNullOrEmpty(value) ? defaultValue : value;
    }

    private CimProject getCreateIssueMetadata(JiraRestClient restClient, String projectKey) {
        Iterable<CimProject> cimProjects = restClient.getIssueClient().getCreateIssueMetadata(
                new GetCreateIssueMetadataOptionsBuilder().withProjectKeys(projectKey).withExpandedIssueTypesFields().build()).claim();
        Iterator<CimProject> cimProjectIterator = cimProjects.iterator();
        if (cimProjectIterator.hasNext()) {
            return cimProjectIterator.next();
        } else {
            throw new BadRequestException("No such project with projectKey " + projectKey);
        }
    }
}
