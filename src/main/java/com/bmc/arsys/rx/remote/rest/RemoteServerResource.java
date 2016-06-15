package com.bmc.arsys.rx.remote.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.bmc.arsys.rx.jira.remote.rest.JiraResource;
import com.bmc.arsys.rx.remote.dto.descriptor.ActionDescriptor;
import com.bmc.arsys.rx.remote.dto.descriptor.ConnectionInstancePropertyDescriptor;
import com.bmc.arsys.rx.remote.dto.descriptor.ConnectionInstancePropertyType;
import com.bmc.arsys.rx.remote.dto.descriptor.ConnectorDescriptor;
import com.bmc.arsys.rx.remote.dto.descriptor.InputDescriptor;
import com.bmc.arsys.rx.remote.dto.descriptor.InputOutputType;
import com.bmc.arsys.rx.remote.dto.descriptor.RemoteServerDescriptor;


@Path("remoteserver")
public class RemoteServerResource {
	
	private static final RemoteServerDescriptor DESCRIPTOR =
	        new RemoteServerDescriptor() {{
	            connectors.add(
	                new ConnectorDescriptor() {{
	                    name = "JIRA";
	                    type = "com.bmc.arsys.rx.jira";
	                    path = JiraResource.BASE_PATH;

	                    connectionInstanceProperties.add(
	                        new ConnectionInstancePropertyDescriptor() {{
	                            name = "url";
	                            displayName = "URL";
	                            helpText = "e.g. https://jira.example.com";
	                            type = ConnectionInstancePropertyType.STRING;
	                            required = true;
	                        }}
	                    );
	                    connectionInstanceProperties.add(
	                        new ConnectionInstancePropertyDescriptor() {{
	                            name = "login";
	                            displayName = "Login";
	                            type = ConnectionInstancePropertyType.STRING;
	                            required = true;
	                        }}
	                    );
	                    connectionInstanceProperties.add(
	                        new ConnectionInstancePropertyDescriptor() {{
	                            name = "password";
	                            displayName = "Password";
	                            type = ConnectionInstancePropertyType.STRING;
	                            //Encrypted is not yet supported in server, so far only String and Int
	                            //type = ConnectionInstancePropertyType.ENCRYPTED_STRING;
	                            required = false; // allow empty password
	                        }}
	                    );

	                    actions.add(
	                        new ActionDescriptor() {{
	                            name = "createIssue";
	                            displayName = "Create JIRA Issue";
	                            path = JiraResource.CREATE_ISSUE_PATH;
	                            inputs.add(
	                                    new InputDescriptor() {{
	                                        name = "summary";
	                                        type = InputOutputType.STRING;
	                                        required = true;
	                                    }}
	                            );
	                            inputs.add(
	                                    new InputDescriptor() {{
	                                        name = "customerName";
	                                        type = InputOutputType.STRING;
	                                        required = true;
	                                    }}
	                            );
	                            inputs.add(
	                                    new InputDescriptor() {{
	                                        name = "severity";
	                                        type = InputOutputType.STRING;
	                                        required = true;
	                                    }}
	                            );
	                            inputs.add(
	                                    new InputDescriptor() {{
	                                        name = "projectKey";
	                                        type = InputOutputType.STRING;
	                                        required = true;
	                                    }}
	                            );
	                            inputs.add(
	                                    new InputDescriptor() {{
	                                        name = "issueType";
	                                        type = InputOutputType.STRING;
	                                        required = true;
	                                    }}
	                            );
	                            output=new InputDescriptor() {{
	                                name = "result";
	                                type = InputOutputType.STRING;
	                                required = true;
	                            }};
	                        }}
	                    );
	                }}
	            );
	        }};
	
	@GET
    @Path("remoteserverdescriptor")
    @Produces(MediaType.APPLICATION_JSON)
    public Object get() {
        return DESCRIPTOR;
    }

    @GET
    @Path("remoteserverhealth")
    @Produces("text/plain")
    public String checkHealth() {
        return "OK";
    }
}
