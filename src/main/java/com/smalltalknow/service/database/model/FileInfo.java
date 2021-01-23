package com.smalltalknow.service.database.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.EntityConstant;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public final class FileInfo {
    @JsonProperty(EntityConstant.FILE_INFO_FILE_ID)
    private final int fileId;
    @JsonProperty(EntityConstant.FILE_INFO_FIRST_SELECTOR)
    private final int firstSelector;
    @JsonProperty(EntityConstant.FILE_INFO_SECOND_SELECTOR)
    private final int secondSelector;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_NAME)
    private final String fileName;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_LINK)
    private final String fileLink;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_UPLOADER)
    private final int fileUploader;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_UPLOAD_TIME)
    private final Instant fileUploadTime;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_EXPIRE_TIME)
    private final Instant fileExpireTime;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_SIZE)
    private final int fileSize;
    @JsonProperty(EntityConstant.FILE_INFO_FILE_DOWNLOADS)
    private final int fileDownloads;
}
