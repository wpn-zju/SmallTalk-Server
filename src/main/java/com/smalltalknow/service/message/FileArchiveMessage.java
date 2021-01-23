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
public class FileArchiveMessage {
    @JsonProperty(ClientConstant.FILE_ARCHIVE_FIRST_SELECTOR)
    private final int firstSelector;
    @JsonProperty(ClientConstant.FILE_ARCHIVE_SECOND_SELECTOR)
    private final int secondSelector;
    @JsonProperty(ClientConstant.FILE_ARCHIVE_FILE_NAME)
    private final String fileName;
    @JsonProperty(ClientConstant.FILE_ARCHIVE_FILE_LINK)
    private final String fileLink;
    @JsonProperty(ClientConstant.FILE_ARCHIVE_FILE_UPLOADER)
    private final int fileUploader;
    @JsonProperty(ClientConstant.FILE_ARCHIVE_FILE_SIZE)
    private final int fileSize;
}
