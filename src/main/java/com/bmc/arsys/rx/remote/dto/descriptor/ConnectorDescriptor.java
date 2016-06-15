package com.bmc.arsys.rx.remote.dto.descriptor;

import java.util.ArrayList;
import java.util.List;

public class ConnectorDescriptor {

    public String name;
    public String type;
    public String path;
    public List<ConnectionInstancePropertyDescriptor> connectionInstanceProperties = new ArrayList<ConnectionInstancePropertyDescriptor>();
    public List<ActionDescriptor> actions = new ArrayList<ActionDescriptor>();

}
