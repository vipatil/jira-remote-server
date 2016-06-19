package com.example.remote.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class TopResource {

    /**
     * Top level resource to test is remote server is reachable.
     *
     * @return Remote Server identification string.
     */
    @GET
    public String hello() {
        return "Remote JIRA Connector";
    }
}