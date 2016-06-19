package com.example.remote.dto;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorResponse {

    public String messageType = "ERROR";
    public String messageText;
    public String moreInfo;

    public ErrorResponse(Throwable t) {
        messageText = t.toString();

        StringWriter buffer = new StringWriter();
        t.printStackTrace(new PrintWriter(buffer));
        buffer.flush();

        moreInfo = buffer.toString();
    }
}
