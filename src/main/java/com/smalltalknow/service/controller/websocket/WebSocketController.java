package com.smalltalknow.service.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.smalltalknow.service.controller.enums.EnumRequestStatus;
import com.smalltalknow.service.controller.patterns.PatternChecker;
import com.smalltalknow.service.controller.patterns.exceptions.*;
import com.smalltalknow.service.database.DatabaseService;
import com.smalltalknow.service.database.exception.*;
import com.smalltalknow.service.database.model.GroupInfo;
import com.smalltalknow.service.database.model.RequestInfo;
import com.smalltalknow.service.database.model.UserInfo;
import com.smalltalknow.service.message.*;
import com.smalltalknow.service.tool.EmailHelper;
import com.smalltalknow.service.tool.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Controller
@SuppressWarnings("unused")
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void proceedToSignIn(int userId, String session) {
        messagingTemplate.convertAndSendToUser(session,
                ServerConstant.DIR_USER_SIGN_IN_SUCCESS, new Object());

        DatabaseService.updateSession(userId, session);
        DatabaseService.updateLoginRecord(userId);

        sendUserInfo(userId);
        DatabaseService.popOfflineMessageAsList(userId).forEach(m -> forwardMessage(session, m.toString()));
    }

    private void forwardMessage(String session, String content) {
        messagingTemplate.convertAndSendToUser(session, ServerConstant.DIR_NEW_MESSAGE, content);
    }

    @MessageMapping(ClientConstant.API_USER_SIGN_UP)
    public void userSignUp(SimpMessageHeaderAccessor sha, UserSignUpMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();
        String userPassword = message.getUserPassword();
        String passcode = message.getPasscode();

        try {
            PatternChecker.checkUserEmail(userEmail);
            PatternChecker.checkUserPassword(userPassword);
            PatternChecker.checkPasscode(passcode);

            DatabaseService.checkPasscode(passcode, new HashMap<String, String>() {{
                put(ServerConstant.PSC_TYPE, ServerConstant.PSC_TYPE_USER_SIGN_UP);
                put(ServerConstant.PSC_USER_EMAIL, userEmail);
            }});

            int userId = DatabaseService.newAccount(userEmail);
            DatabaseService.modifyUserName(userId, String.format("User %s", userId));
            DatabaseService.modifyUserPassword(userId, userPassword);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_SUCCESS, new Object());
            EmailHelper.sendNewUserNotification(userEmail);
            proceedToSignIn(userId, session);
        } catch (PasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_FAILED_PASSCODE_INCORRECT, new Object());
        } catch (UserEmailExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_FAILED_EMAIL_EXISTS, new Object());
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, new Object());
        } catch (InvalidPasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_PASSCODE, new Object());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @MessageMapping(ClientConstant.API_USER_SIGN_UP_PASSCODE_REQUEST)
    public void userSignUpPasscodeRequest(SimpMessageHeaderAccessor sha, UserSignUpPasscodeRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();

        try {
            PatternChecker.checkUserEmail(userEmail);

            if (DatabaseService.hasUserWithEmail(userEmail)) {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_EMAIL_EXISTS, new Object());
            } else {
                String passcode = DatabaseService.newPasscode(new HashMap<String, String>() {{
                    put(ServerConstant.PSC_TYPE, ServerConstant.PSC_TYPE_USER_SIGN_UP);
                    put(ServerConstant.PSC_USER_EMAIL, userEmail);
                }});

                EmailHelper.sendPasscode(userEmail, "Creating New Account", passcode);

                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_SUCCESS, new Object());
            }
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        } catch (UnirestException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_SERVER_ERROR, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_RECOVER_PASSWORD)
    public void userRecoverPassword(SimpMessageHeaderAccessor sha, UserRecoverPasswordMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();
        String userPassword = message.getUserPassword();
        String passcode = message.getPasscode();

        try {
            PatternChecker.checkUserEmail(userEmail);
            PatternChecker.checkUserPassword(userPassword);
            PatternChecker.checkPasscode(passcode);

            int userId = DatabaseService.queryUserIdByEmail(userEmail);
            DatabaseService.checkPasscode(passcode, new HashMap<String, String>() {{
                put(ServerConstant.PSC_TYPE, ServerConstant.PSC_TYPE_USER_RECOVER_PASSWORD);
                put(ServerConstant.PSC_USER_EMAIL, userEmail);
            }});
            DatabaseService.modifyUserPassword(userId, userPassword);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_SUCCESS, new Object());
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_FAILED_USER_NOT_FOUND, new Object());
        } catch (PasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_FAILED_PASSCODE_INCORRECT, new Object());
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, new Object());
        } catch (InvalidPasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_PASSCODE, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_RECOVER_PASSWORD_PASSCODE_REQUEST)
    public void userRecoverPasswordPasscodeRequest(
            SimpMessageHeaderAccessor sha, UserRecoverPasswordPasscodeRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();

        try {
            PatternChecker.checkUserEmail(userEmail);

            if (DatabaseService.hasUserWithEmail(userEmail)) {
                String passcode = DatabaseService.newPasscode(new HashMap<String, String>() {{
                    put(ServerConstant.PSC_TYPE, ServerConstant.PSC_TYPE_USER_RECOVER_PASSWORD);
                    put(ServerConstant.PSC_USER_EMAIL, userEmail);
                }});

                EmailHelper.sendPasscode(userEmail, "Recovering Password", passcode);

                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_SUCCESS, new Object());
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_USER_NOT_FOUND, new Object());
            }
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        } catch (UnirestException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_SERVER_ERROR, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_SIGN_IN)
    public void userSignIn(SimpMessageHeaderAccessor sha, UserSignInMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();
        String userPassword = message.getUserPassword();

        try {
            PatternChecker.checkUserEmail(userEmail);
            PatternChecker.checkUserPassword(userPassword);

            int userId = DatabaseService.queryUserIdByEmail(userEmail);
            UserInfo userInfo = DatabaseService.getUserInfo(userId);
            if (userInfo.getUserPassword().equals(userPassword)) {
                proceedToSignIn(userId, session);
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_IN_FAILED_PASSWORD_INCORRECT, new Object());
            }
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Sign In - User not found!");
            e.printStackTrace();
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_IN_FAILED_USER_NOT_FOUND, new Object());
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_SESSION_SIGN_IN)
    public void userSessionSignIn(SimpMessageHeaderAccessor sha, UserSessionSignInMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String sessionOld = message.getSessionToken();

        try {
            PatternChecker.checkSessionToken(sessionOld);

            int userId = DatabaseService.queryUserIdBySession(sessionOld);
            proceedToSignIn(userId, session);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidSessionException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_SESSION, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_SESSION_SIGN_OUT)
    public void userSessionSignOut(SimpMessageHeaderAccessor sha, UserSessionSignOutMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();

        try {
            int userId = DatabaseService.queryUserIdBySession(session);
            DatabaseService.updateLogoutRecord(userId);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_OUT_SUCCESS, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_MODIFY_NAME)
    public void userModifyName(SimpMessageHeaderAccessor sha, UserModifyNameMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String newUserName = message.getNewUserName();

        try {
            PatternChecker.checkUserName(newUserName);

            int userId = DatabaseService.queryUserIdBySession(session);
            DatabaseService.modifyUserName(userId, newUserName);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_MODIFY_NAME_SUCCESS, new Object());
            sendUserInfo(userId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidUserNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_NAME, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_USER_MODIFY_PASSWORD)
    public void userModifyPassword(SimpMessageHeaderAccessor sha, UserModifyPasswordMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String newUserPassword = message.getNewUserPassword();

        try {
            PatternChecker.checkUserPassword(newUserPassword);

            int userId = DatabaseService.queryUserIdBySession(session);
            DatabaseService.modifyUserPassword(userId, newUserPassword);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_MODIFY_PASSWORD_SUCCESS, new Object());
            sendUserInfo(userId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, new Object());
        }
    }

    private void sendUserInfo(int userId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_USER_SYNC, buildUserMessage(userId));
            }
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - User not found!");
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Group not found!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Request not found!");
            e.printStackTrace();
        }
    }

    private void sendContactInfo(int userId, int contactId) {
        try {
            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                    ServerConstant.DIR_USER_SYNC, buildUserMessage(userId));
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - User not found!");
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Group not found!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Request not found!");
            e.printStackTrace();
        }
    }

    private void sendGroupInfo(int groupId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_USER_SYNC, buildUserMessage(userId));
            }
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - User not found!");
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Group not found!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Request not found!");
            e.printStackTrace();
        }
    }

    private void sendRequestInfo(int requestId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_USER_SYNC, buildUserMessage(userId));
            }
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - User not found!");
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Group not found!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - Request not found!");
            e.printStackTrace();
        }
    }

    @MessageMapping(ClientConstant.API_LOAD_USER)
    public void loadUser(SimpMessageHeaderAccessor sha, LoadUserMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();

        try {
            sendUserInfo(DatabaseService.queryUserIdBySession(session));
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_LOAD_CONTACT)
    public void loadContact(SimpMessageHeaderAccessor sha, LoadContactMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int contactId = message.getContactId();

        try {
            sendContactInfo(DatabaseService.queryUserIdBySession(session), contactId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping({ClientConstant.API_LOAD_GROUP})
    public void loadGroup(SimpMessageHeaderAccessor sha, LoadGroupMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();

        try {
            sendGroupInfo(DatabaseService.queryUserIdBySession(session), groupId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_LOAD_REQUEST)
    public void loadRequest(SimpMessageHeaderAccessor sha, LoadRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            sendUserInfo(DatabaseService.queryUserIdBySession(session));
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_MESSAGE_FORWARD)
    public void messageForward(SimpMessageHeaderAccessor sha, MessageForwardMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int sender = message.getSender();
        int receiver = message.getReceiver();

        try {
            if (DatabaseService.checkSession(sender, session)) {
                String messageSerialized = new ObjectMapper().writeValueAsString(message);
                if (DatabaseService.isOnline(sender)) {
                    forwardMessage(session, messageSerialized);
                } else {
                    DatabaseService.pushOfflineMessage(sender, messageSerialized);
                }
                if (DatabaseService.isOnline(receiver)) {
                    forwardMessage(DatabaseService.queryLastSessionById(receiver), messageSerialized);
                } else {
                    DatabaseService.pushOfflineMessage(receiver, messageSerialized);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
            }
        } catch (IOException e) {
            logger.error("Forward Message Failed - IOException");
            e.printStackTrace();
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_MESSAGE_FORWARD_GROUP)
    public void messageForwardGroup(SimpMessageHeaderAccessor sha, MessageForwardGroupMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int sender = message.getSender();
        int receiver = message.getReceiver();

        try {
            if (DatabaseService.checkSession(sender, session)) {
                GroupInfo groupInfo = DatabaseService.getGroupInfo(receiver);
                String messageSerialized = new ObjectMapper().writeValueAsString(message);
                for (int memberId : groupInfo.getMemberList()) {
                    if (DatabaseService.isOnline(memberId)) {
                        forwardMessage(DatabaseService.queryLastSessionById(memberId), messageSerialized);
                    } else {
                        DatabaseService.pushOfflineMessage(memberId, messageSerialized);
                    }
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
            }
        } catch (GroupNotExistsException e) {
            logger.error("Forward Group Message Failed - Group not found!");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Forward Group Message Failed - IOException");
            e.printStackTrace();
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_REQUEST)
    public void contactAddRequest(SimpMessageHeaderAccessor sha, ContactAddRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String contactEmail = message.getContactEmail();

        try {
            PatternChecker.checkUserEmail(contactEmail);

            int userId = DatabaseService.queryUserIdBySession(session);
            int contactId = DatabaseService.queryUserIdByEmail(contactEmail);
            if (userId == contactId || DatabaseService.isFriend(userId, contactId)) {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_CONTACT_ADD_REQUEST_FAILED_ALREADY_CONTACT, new Object());
            } else {
                int requestId = DatabaseService.newContactRequest(userId, contactId);
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_CONTACT_ADD_REQUEST_SUCCESS, new Object());
                sendUserInfo(userId);
                sendUserInfo(contactId);
            }
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_CONTACT_ADD_REQUEST_FAILED_USER_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_CONFIRM)
    public void contactAddConfirm(SimpMessageHeaderAccessor sha, ContactAddConfirmMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_CONTACT_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                int sender = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
                int receiver = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isFriend(sender, receiver)
                        && receiver != sender) {
                    DatabaseService.newContactConfirm(requestId, sender, receiver);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_REFUSE)
    public void contactAddRefuse(SimpMessageHeaderAccessor sha, ContactAddRefuseMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_CONTACT_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                int sender = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
                int receiver = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isFriend(sender, receiver)
                        && receiver != sender) {
                    DatabaseService.newContactRefuse(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_CREATE_REQUEST)
    public void groupCreateRequest(SimpMessageHeaderAccessor sha, GroupCreateRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String groupName = message.getGroupName();

        try {
            PatternChecker.checkGroupName(groupName);

            int userId = DatabaseService.queryUserIdBySession(session);
            int groupId = DatabaseService.newGroup(userId);
            DatabaseService.modifyGroupName(groupId, groupName);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_GROUP_CREATE_REQUEST_SUCCESS, String.format("{\"group_id\":%s}", groupId));
            sendUserInfo(userId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidGroupNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_GROUP_NAME, new Object());
        }
    }

    private boolean isGroupHost(int userId, int groupId) throws GroupNotExistsException {
        return DatabaseService.getGroupInfo(groupId).getGroupHostId() == userId;
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_MODIFY_NAME)
    public void groupModifyName(SimpMessageHeaderAccessor sha, GroupModifyNameMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();
        String newGroupName = message.getNewGroupName();

        try {
            PatternChecker.checkGroupName(newGroupName);

            int userId = DatabaseService.queryUserIdBySession(session);
            if (isGroupHost(userId, groupId)) {
                DatabaseService.modifyGroupName(groupId, newGroupName);
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_GROUP_MODIFY_NAME_SUCCESS, new Object());
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_GROUP_MODIFY_NAME_FAILED_PERMISSION_DENIED, new Object());
            }
        } catch (GroupNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_GROUP_MODIFY_NAME_FAILED_GROUP_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        } catch (InvalidGroupNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_GROUP_NAME, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_REQUEST)
    public void groupAddRequest(SimpMessageHeaderAccessor sha, GroupAddRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();

        try {
            int userId = DatabaseService.queryUserIdBySession(session);
            if (DatabaseService.isMember(groupId, userId)) {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_GROUP_ADD_REQUEST_FAILED_ALREADY_MEMBER, new Object());
            } else {
                GroupInfo groupInfo = DatabaseService.getGroupInfo(groupId);
                int hostId = groupInfo.getGroupHostId();
                int requestId = DatabaseService.newMemberRequest(userId, groupId, hostId);
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_GROUP_ADD_REQUEST_SUCCESS, new Object());
                sendUserInfo(userId);
                sendUserInfo(hostId);
            }
        } catch (GroupNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_GROUP_ADD_REQUEST_FAILED_GROUP_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_CONFIRM)
    public void groupAddConfirm(SimpMessageHeaderAccessor sha, GroupAddConfirmMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_GROUP_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                int sender = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
                int receiver = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt();
                int groupId = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isMember(groupId, sender)
                        && receiver == DatabaseService.getGroupInfo(groupId).getGroupHostId()) {
                    DatabaseService.newMemberConfirm(requestId, sender, groupId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
            }
        } catch (GroupNotExistsException e) {
            logger.error("Group Add Failed - Unknown Error!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_REFUSE)
    public void groupAddRefuse(SimpMessageHeaderAccessor sha, GroupAddRefuseMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_GROUP_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                int sender = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
                int receiver = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt();
                int groupId = requestInfo.getRequestMetadata().get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isMember(groupId, sender)
                        && receiver == DatabaseService.getGroupInfo(groupId).getGroupHostId()) {
                    DatabaseService.newMemberRefuse(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
            }
        } catch (GroupNotExistsException e) {
            logger.error("Group Add Failed - Unknown Error!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, new Object());
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, new Object());
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, new Object());
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_WEBRTC_CALL)
    public void webrtcCall(SimpMessageHeaderAccessor sha, WebRTCCallMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int sender = message.getSender();
        int receiver = message.getReceiver();
        String command = message.getWebRTCCommand();
        String sessionDescription = message.getWebRTCSessionDescription();

        try {
            if (DatabaseService.checkSession(sender, session)) {
                String messageSerialized = new ObjectMapper().writeValueAsString(message);
                switch (command) {
                    case VideoCommands.WEBRTC_COMMAND_REQUEST:
                        if (DatabaseService.isOnline(receiver)) {
                            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(receiver),
                                    ServerConstant.DIR_WEBRTC_CALL, messageSerialized);
                        } else {
                            HashMap<String, JsonObject> refuseResponseMap = new HashMap<>();
                            refuseResponseMap.put(ServerConstant.CHAT_WEBRTC_CALL__SENDER, new JsonObject(receiver));
                            refuseResponseMap.put(ServerConstant.CHAT_WEBRTC_CALL__RECEIVER, new JsonObject(sender));
                            refuseResponseMap.put(ServerConstant.CHAT_WEBRTC_CALL__WEBRTC_COMMAND,
                                    new JsonObject(VideoCommands.WEBRTC_COMMAND_REFUSE));
                            messagingTemplate.convertAndSendToUser(session,
                                    ServerConstant.DIR_WEBRTC_CALL, new JsonObject(refuseResponseMap).toString());
                        }
                        break;
                    case VideoCommands.WEBRTC_COMMAND_ACCEPT:
                    case VideoCommands.WEBRTC_COMMAND_REFUSE:
                    case VideoCommands.WEBRTC_COMMAND_CALL:
                    case VideoCommands.WEBRTC_COMMAND_ANSWER:
                    case VideoCommands.WEBRTC_COMMAND_CANDIDATE:
                    default:
                        if (DatabaseService.isOnline(receiver)) {
                            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(receiver),
                                    ServerConstant.DIR_WEBRTC_CALL, messageSerialized);
                        }
                        break;
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SESSION_EXPIRED, new Object());
            }
        } catch (IOException e) {
            logger.error("WebRTC Call Failed - IOException");
            e.printStackTrace();
        }
    }

    private String buildUserMessage(int userId)
            throws UserNotExistsException, GroupNotExistsException, RequestNotExistsException {
        UserInfo userInfo = DatabaseService.getUserInfo(userId);
        HashMap<String, JsonObject> userInfoMap = new HashMap<>();
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_ID, new JsonObject(userInfo.getUserId()));
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_SESSION, new JsonObject(userInfo.get))
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_EMAIL, new JsonObject(userInfo.getUserEmail()));
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_NAME, new JsonObject(userInfo.getUserName()));
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_PASSWORD, new JsonObject(userInfo.getUserPassword()));
        ArrayList<JsonObject> contactList = new ArrayList<>();
        for (int contactUserId : userInfo.getContactList()) {
            UserInfo contactInfo = DatabaseService.getUserInfo(contactUserId);
            HashMap<String, JsonObject> contactInfoMap = new HashMap<>();
            contactInfoMap.put(ServerConstant.ACC_CONTACT_SYNC__CONTACT_ID,
                    new JsonObject(contactInfo.getUserId()));
            contactInfoMap.put(ServerConstant.ACC_CONTACT_SYNC__CONTACT_NAME,
                    new JsonObject(contactInfo.getUserName()));
            contactInfoMap.put(ServerConstant.ACC_CONTACT_SYNC__CONTACT_EMAIL,
                    new JsonObject(contactInfo.getUserEmail()));
            contactList.add(new JsonObject(contactInfoMap));
        }
        ArrayList<JsonObject> groupList = new ArrayList<>();
        for (int groupId : userInfo.getGroupList()) {
            GroupInfo groupInfo = DatabaseService.getGroupInfo(groupId);
            HashMap<String, JsonObject> groupInfoMap = new HashMap<>();
            groupInfoMap.put(ServerConstant.ACC_GROUP_SYNC__GROUP_ID,
                    new JsonObject(groupInfo.getGroupId()));
            groupInfoMap.put(ServerConstant.ACC_GROUP_SYNC__GROUP_NAME,
                    new JsonObject(groupInfo.getGroupName()));
            UserInfo hostInfo = DatabaseService.getUserInfo(groupInfo.getGroupHostId());
            groupInfoMap.put(ServerConstant.ACC_GROUP_SYNC__GROUP_HOST,
                    new JsonObject(hostInfo.getUserId()));
            groupInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_GROUP_LIST_G_HOST_NAME,
                    new JsonObject(hostInfo.getUserName()));
            groupInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_GROUP_LIST_G_HOST_EMAIL,
                    new JsonObject(hostInfo.getUserEmail()));
            ArrayList<JsonObject> memberList = new ArrayList<>();
            for (int memberId : groupInfo.getMemberList()) {
                UserInfo memberInfo = DatabaseService.getUserInfo(memberId);
                HashMap<String, JsonObject> memberInfoMap = new HashMap<>();
                memberInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_GROUP_LIST_G_MEMBER_LIST_G_MEMBER_USER_ID,
                        new JsonObject(memberInfo.getUserId()));
                memberInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_GROUP_LIST_G_MEMBER_LIST_G_MEMBER_USER_NAME,
                        new JsonObject(memberInfo.getUserName()));
                memberInfoMap.put(ServerConstant.ACC_USER_SYNC__USER_GROUP_LIST_G_MEMBER_LIST_G_MEMBER_USER_EMAIL,
                        new JsonObject(memberInfo.getUserEmail()));
                memberList.add(new JsonObject(memberInfoMap));
            }
            groupInfoMap.put(ServerConstant.ACC_GROUP_SYNC__GROUP_MEMBER_LIST,
                    new JsonObject(memberList));
            contactList.add(new JsonObject(groupInfoMap));
        }
        ArrayList<JsonObject> requestList = new ArrayList<>();
        for (int requestId : userInfo.getRequestList()) {
            RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId);
            HashMap<String, JsonObject> requestInfoMap = new HashMap<>();
            requestInfoMap.put(ServerConstant.ACC_REQUEST_SYNC__REQUEST_ID,
                    new JsonObject(requestInfo.getRequestType()));
            requestInfoMap.put(ServerConstant.ACC_REQUEST_SYNC__REQUEST_STATUS,
                    new JsonObject(requestInfo.getRequestStatus()));
            requestInfoMap.put(ServerConstant.ACC_REQUEST_SYNC__REQUEST_TYPE,
                    new JsonObject(requestInfo.getRequestType()));
            requestInfoMap.put(ServerConstant.ACC_REQUEST_SYNC__REQUEST_METADATA,
                    new JsonObject(requestInfo.getRequestMetadata()));
            requestList.add(new JsonObject(requestInfoMap));
        }
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__CONTACT_LIST, new JsonObject(contactList));
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__GROUP_LIST, new JsonObject(groupList));
        userInfoMap.put(ServerConstant.ACC_USER_SYNC__REQUEST_LIST, new JsonObject(requestList));
        return new JsonObject(userInfoMap).toString();
    }
}
