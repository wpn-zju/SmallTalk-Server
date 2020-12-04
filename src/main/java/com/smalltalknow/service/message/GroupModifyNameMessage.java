package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class GroupModifyNameMessage {
    @JsonProperty(ClientConstant.CHAT_GROUP_MODIFY_NAME_GROUP_ID)
    private final int groupId;
    @JsonProperty(ClientConstant.CHAT_GROUP_MODIFY_NAME_NEW_GROUP_NAME)
    private final String newGroupName;
}
