package com.bmc.arsys.rx.remote.dto.descriptor;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;

public enum InputOutputType {
    STRING, INTEGER, NUMBER, DATE, TIME, DATETIME, OBJECT;

    @JsonValue
    public String toJson() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
    }
}
