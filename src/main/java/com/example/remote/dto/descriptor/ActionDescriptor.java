package com.example.remote.dto.descriptor;

import java.util.ArrayList;
import java.util.List;

public class ActionDescriptor {

    public String name;
    public String displayName;
    public String path;
    public List<InputDescriptor> inputs = new ArrayList<InputDescriptor>();
    // Every action must have a output, and server is expecting it
    public InputDescriptor output;
}
