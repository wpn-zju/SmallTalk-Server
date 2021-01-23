package com.smalltalknow.service.controller.websocket;

import com.smalltalknow.service.database.DatabaseService;
import com.smalltalknow.service.database.exception.SessionExpiredException;
import com.smalltalknow.service.database.exception.SessionInvalidException;
import com.smalltalknow.service.database.exception.SessionRevokedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Objects;

@Component
@Import(WebSocketController.class)
public class WebSocketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private static final boolean REPORT_SUBSCRIBE = false;

    @Autowired
    private WebSocketController webSocketController;

    @EventListener
    private void handleSessionConnect(SessionConnectEvent event) {
        logger.info(String.format("Connect - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionConnected(SessionConnectedEvent event) {
        logger.info(String.format("Connected - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        logger.info(String.format("Disconnected - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));

        try {
            String session = Objects.requireNonNull(event.getUser()).getName();
            int userId = DatabaseService.queryUserIdBySession(session);
            webSocketController.cleanUser(userId);
        } catch (SessionInvalidException | SessionExpiredException | SessionRevokedException e) {
            logger.info(e.getMessage());
        }
    }

    @EventListener
    private void handleSessionSubscribed(SessionSubscribeEvent event) {
        if (REPORT_SUBSCRIBE) {
            logger.info(String.format("Subscribed - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
        }
    }

    @EventListener
    private void handleSessionUnsubscribed(SessionUnsubscribeEvent event) {
        if (REPORT_SUBSCRIBE) {
            logger.info(String.format("Unsubscribed - Session ID = %s", Objects.requireNonNull(event.getUser()).getName()));
        }
    }
}
