package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupCreateRequestMessage {
    @JsonProperty(ClientConstant.CHAT_GROUP_CREATE_REQUEST_GROUP_NAME)
    private final String groupName;
}
