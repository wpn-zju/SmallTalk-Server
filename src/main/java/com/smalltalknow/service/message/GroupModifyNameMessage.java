package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientStrings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupModifyNameMessage {
    @JsonProperty(ClientStrings.CHAT_GROUP_MODIFY_NAME_GROUP_ID)
    private final int groupId;
    @JsonProperty(ClientStrings.CHAT_GROUP_MODIFY_NAME_NEW_GROUP_NAME)
    private final String newGroupName;
}
