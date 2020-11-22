package com.smalltalknow.service.database.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class GroupInfo {
    private final int groupId;
    private final String groupName;
    private final int groupHostId;
    private final List<Integer> memberList;
}
