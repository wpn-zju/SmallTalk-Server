package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupAddRefuseMessage {
    @JsonProperty(ClientStrings.CHAT_GROUP_ADD_REFUSE_REQUEST_ID)
    private final int requestId;
}
