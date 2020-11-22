package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupAddRequestMessage {
    @JsonProperty(ClientStrings.CHAT_GROUP_ADD_REQUEST_GROUP_ID)
    private final int groupId;
}
