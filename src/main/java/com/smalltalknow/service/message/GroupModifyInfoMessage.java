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
public class GroupModifyInfoMessage {
    @JsonProperty(ClientConstant.GROUP_MODIFY_INFO_GROUP_ID)
    private final int groupId;
    @JsonProperty(ClientConstant.GROUP_MODIFY_INFO_GROUP_NAME)
    private final String groupName;
    @JsonProperty(ClientConstant.GROUP_MODIFY_INFO_GROUP_INFO)
    private final String groupInfo;
    @JsonProperty(ClientConstant.GROUP_MODIFY_INFO_GROUP_AVATAR_LINK)
    private final String groupAvatarLink;
}
