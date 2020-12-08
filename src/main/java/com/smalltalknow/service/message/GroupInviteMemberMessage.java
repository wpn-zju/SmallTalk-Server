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
public class GroupInviteMemberMessage {
    @JsonProperty(ClientConstant.CHAT_GROUP_INVITE_MEMBER_GROUP_ID)
    private final int groupId;
    @JsonProperty(ClientConstant.CHAT_GROUP_INVITE_MEMBER_MEMBER_ID)
    private final int memberId;
}
