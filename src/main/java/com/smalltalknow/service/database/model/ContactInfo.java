package com.smalltalknow.service.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.EntityConstant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class ContactInfo {
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_ID)
    private final int contactId;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_EMAIL)
    private final String contactEmail;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_NAME)
    private final String contactName;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_GENDER)
    private final int contactGender;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_AVATAR_LINK)
    private final String contactAvatarLink;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_INFO)
    private final String contactInfo;
    @JsonProperty(EntityConstant.CONTACT_INFO_CONTACT_LOCATION)
    private final String contactLocation;
}
