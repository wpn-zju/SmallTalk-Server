package com.smalltalknow.service.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class WebRTCCallMessage {
    @JsonProperty(ClientConstant.CHAT_WEBRTC_CALL_SENDER)
    private final int sender;
    @JsonProperty(ClientConstant.CHAT_WEBRTC_CALL_RECEIVER)
    private final int receiver;
    @JsonProperty(ClientConstant.CHAT_WEBRTC_CALL_WEBRTC_COMMAND)
    private final String webRTCCommand;
    @JsonProperty(ClientConstant.CHAT_WEBRTC_CALL_WEBRTC_SESSION_DESCRIPTION)
    private final String webRTCSessionDescription;
}
