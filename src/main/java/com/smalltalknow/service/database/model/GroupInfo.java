package com.smalltalknow.service.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.EntityConstant;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class GroupInfo {
    @JsonProperty(EntityConstant.GROUP_INFO_GROUP_ID)
    private final int groupId;
    @JsonProperty(EntityConstant.GROUP_INFO_GROUP_HOST_ID)
    private final int groupHostId;
    @JsonProperty(EntityConstant.GROUP_INFO_GROUP_NAME)
    private final String groupName;
    @JsonProperty(EntityConstant.GROUP_INFO_GROUP_INFO)
    private final String groupInfo;
    @JsonProperty(EntityConstant.GROUP_INFO_GROUP_AVATAR_LINK)
    private final String groupAvatarLink;
    @JsonProperty(EntityConstant.GROUP_INFO_MEMBER_LIST)
    private final List<Integer> memberList;
}
