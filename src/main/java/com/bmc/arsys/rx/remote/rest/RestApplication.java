package com.bmc.arsys.rx.remote.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

import com.bmc.arsys.rx.jira.remote.rest.JiraResource;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@ApplicationPath("/")
public class RestApplication extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(ApplicationExceptionHandler.class);
        classes.add(JacksonJaxbJsonProvider.class);
        classes.add(TopResource.class);
        classes.add(RemoteServerResource.class);
        classes.add(JiraResource.class);
        return classes;
    }
}
