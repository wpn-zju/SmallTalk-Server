package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageForwardGroupMessage {
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_GROUP_SENDER)
    private final int sender;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_GROUP_RECEIVER)
    private final int receiver;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_GROUP_CONTENT)
    private final String content;
    @JsonProperty(ClientConstant.CHAT_MESSAGE_FORWARD_GROUP_CONTENT_TYPE)
    private final String contentType;
    @JsonProperty(ClientConstant.TIMESTAMP)
    private final String timestamp;
}
