package com.smalltalknow.service.database.model;

import com.smalltalknow.service.tool.JsonObject;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RequestInfo {
    private final int requestId;
    private final String requestStatus;
    private final String requestType;
    private final JsonObject requestMetadata;
    private final List<Integer> visibleUserList;
}
