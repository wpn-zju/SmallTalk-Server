package com.smalltalknow.service.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.EntityConstant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestInfo {
    @JsonProperty(EntityConstant.REQUEST_INFO_REQUEST_ID)
    private final int requestId;
    @JsonProperty(EntityConstant.REQUEST_INFO_REQUEST_STATUS)
    private final String requestStatus;
    @JsonProperty(EntityConstant.REQUEST_INFO_REQUEST_TYPE)
    private final String requestType;
    @JsonProperty(EntityConstant.REQUEST_INFO_REQUEST_METADATA)
    private final String requestMetadata;
}
