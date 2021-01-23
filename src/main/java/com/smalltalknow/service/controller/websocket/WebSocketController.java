package com.smalltalknow.service.controller.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.smalltalknow.service.controller.enums.EnumMessageType;
import com.smalltalknow.service.controller.enums.EnumRequestStatus;
import com.smalltalknow.service.controller.patterns.PatternChecker;
import com.smalltalknow.service.controller.patterns.exceptions.*;
import com.smalltalknow.service.database.DatabaseService;
import com.smalltalknow.service.database.exception.*;
import com.smalltalknow.service.database.model.*;
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
import java.util.*;

@Controller
@SuppressWarnings("unused")
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void proceedToSignIn(int userId, String session) {
        messagingTemplate.convertAndSendToUser(session,
                ServerConstant.DIR_USER_SIGN_IN_SUCCESS, "Success");
        refreshSession(userId, session);
        DatabaseService.popOfflineMessageAsList(userId).forEach(m -> {
            String messageType = m.get("message_type").getString();
            if (messageType.equals(EnumMessageType.MESSAGE_TYPE_PRIVATE.toString())) {
                forwardMessage(session, m.get("content").toString());
            } else if (messageType.equals(EnumMessageType.MESSAGE_TYPE_GROUP.toString())) {
                forwardGroupMessage(session, m.get("content").toString());
            } else {
                logger.error("Unsupported Message");
            }
        });
    }

    private void forwardMessage(String session, String content) {
        messagingTemplate.convertAndSendToUser(session, ServerConstant.DIR_NEW_MESSAGE, content);
    }

    private void forwardGroupMessage(String session, String content) {
        messagingTemplate.convertAndSendToUser(session, ServerConstant.DIR_NEW_GROUP_MESSAGE, content);
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
                    ServerConstant.DIR_USER_SIGN_UP_SUCCESS, "Success");
            EmailHelper.sendNewUserNotification(userEmail);
            proceedToSignIn(userId, session);
        } catch (PasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_FAILED_PASSCODE_INCORRECT, "Success");
        } catch (UserEmailExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_FAILED_EMAIL_EXISTS, "Success");
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, "Success");
        } catch (InvalidPasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_PASSCODE, "Success");
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    // Support HTTP Version Soon
    @MessageMapping(ClientConstant.API_USER_SIGN_UP_PASSCODE_REQUEST)
    public void userSignUpPasscodeRequest(SimpMessageHeaderAccessor sha, UserSignUpPasscodeRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String userEmail = message.getUserEmail();

        try {
            PatternChecker.checkUserEmail(userEmail);

            if (DatabaseService.hasUserWithEmail(userEmail)) {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_EMAIL_EXISTS, "Success");
            } else {
                String passcode = DatabaseService.newPasscode(new HashMap<String, String>() {{
                    put(ServerConstant.PSC_TYPE, ServerConstant.PSC_TYPE_USER_SIGN_UP);
                    put(ServerConstant.PSC_USER_EMAIL, userEmail);
                }});

                EmailHelper.sendPasscode(userEmail, "Creating New Account", passcode);

                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_SUCCESS, "Success");
            }
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        } catch (UnirestException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_SERVER_ERROR, "Success");
        }
    }

    // Support HTTP Version Soon
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
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_SUCCESS, "Success");
            proceedToSignIn(userId, session);
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_FAILED_USER_NOT_FOUND, "Success");
        } catch (PasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_FAILED_PASSCODE_INCORRECT, "Success");
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, "Success");
        } catch (InvalidPasscodeException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_PASSCODE, "Success");
        }
    }

    // Support HTTP Version Soon
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
                        ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_SUCCESS, "Success");
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_USER_NOT_FOUND, "Success");
            }
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        } catch (UnirestException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_SERVER_ERROR, "Success");
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
            UserInfo userInfo = DatabaseService.getUser(userId);
            if (userInfo.getUserPassword().equals(userPassword)) {
                proceedToSignIn(userId, session);
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SIGN_IN_FAILED_PASSWORD_INCORRECT, "Success");
            }
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Sign In - User not found!");
            e.printStackTrace();
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_IN_FAILED_USER_NOT_FOUND, "Success");
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, "Success");
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
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        } catch (InvalidSessionException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_SESSION, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_USER_SESSION_SIGN_OUT)
    public void userSessionSignOut(SimpMessageHeaderAccessor sha, UserSessionSignOutMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();

        try {
            int userId = DatabaseService.queryUserIdBySession(session);
            DatabaseService.updateLogoutRecord(userId);
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SIGN_OUT_SUCCESS, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    // Support HTTP Version Soon
    // Todo: Add more checks
    @MessageMapping(ClientConstant.API_USER_MODIFY_INFO)
    public void userModifyInfo(SimpMessageHeaderAccessor sha, UserModifyInfoMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int userId = message.getUserId();
        String userName = message.getUserName();
        String userPassword = message.getUserPassword();
        Integer userGender = message.getUserGender();
        String userAvatarLink = message.getUserAvatarLink();
        String userInfo = message.getUserInfo();
        String userLocation = message.getUserLocation();

        try {
            if (userId != DatabaseService.queryUserIdBySession(session)) return;
            if (userName != null) {
                PatternChecker.checkUserName(userName);
                DatabaseService.modifyUserName(userId, userName);
            }
            if (userPassword != null) {
                PatternChecker.checkUserPassword(userPassword);
                DatabaseService.modifyUserPassword(userId, userPassword);
            }
            if (userGender != null) {
                DatabaseService.modifyUserGender(userId, userGender);
            }
            if (userAvatarLink != null) {
                DatabaseService.modifyUserAvatarLink(userId, userAvatarLink);
            }
            if (userInfo != null) {
                DatabaseService.modifyUserInfo(userId, userInfo);
            }
            if (userLocation != null) {
                DatabaseService.modifyUserLocation(userId, userLocation);
            }
            sendUserInfo(userId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        } catch (InvalidUserNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_NAME, "Success");
        } catch (InvalidUserPasswordException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_PASSWORD, "Success");
        }
    }

    // Support HTTP Version Soon
    // Todo: Add more checks
    @MessageMapping(ClientConstant.API_GROUP_MODIFY_INFO)
    public void groupModifyInfo(SimpMessageHeaderAccessor sha, GroupModifyInfoMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();
        String groupName = message.getGroupName();
        String groupInfo = message.getGroupInfo();
        String groupAvatarLink = message.getGroupAvatarLink();

        try {
            int userId = DatabaseService.queryUserIdBySession(session);
            if (!isGroupHost(userId, groupId)) return;
            if (groupName != null) {
                PatternChecker.checkGroupName(groupName);
                DatabaseService.modifyGroupName(groupId, groupName);
            }
            if (groupInfo != null) {
                DatabaseService.modifyGroupInfo(groupId, groupInfo);
            }
            if (groupAvatarLink != null) {
                DatabaseService.modifyGroupAvatarLink(groupId, groupAvatarLink);
            }
            sendGroupInfo(userId, groupId);
        } catch (GroupNotExistsException e) {
            e.printStackTrace();
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        } catch (InvalidGroupNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_GROUP_NAME, "Success");
        }
    }

    private void sendUserInfo(int userId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_USER_SYNC, buildUserMessage(userId));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UserNotExistsException e) {
            logger.error("Unknown Error When Synchronizing - User not found!");
            e.printStackTrace();
        }
    }

    private void sendContactInfo(int userId, int contactId) {
        try {
            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                    ServerConstant.DIR_CONTACT_SYNC, buildContactMessage(contactId));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UserNotExistsException e) {
            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                    ServerConstant.DIR_CONTACT_SYNC_FAILED_USER_NOT_FOUND, "Success");
        }
    }

    private void sendGroupInfo(int userId, int groupId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_GROUP_SYNC, buildGroupMessage(groupId));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                    ServerConstant.DIR_GROUP_SYNC_FAILED_GROUP_NOT_FOUND, "Success");
        }
    }

    private void sendRequestInfo(int userId, int requestId) {
        try {
            if (DatabaseService.isOnline(userId)) {
                messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                        ServerConstant.DIR_REQUEST_SYNC, buildRequestMessage(requestId));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(DatabaseService.queryLastSessionById(userId),
                    ServerConstant.DIR_REQUEST_SYNC_FAILED_REQUEST_NOT_FOUND, "Success");
        }
    }

    // Support HTTP Version Soon
    @MessageMapping(ClientConstant.API_LOAD_USER)
    public void loadUser(SimpMessageHeaderAccessor sha, LoadUserMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int userId = message.getUserId();

        try {
            if (userId == DatabaseService.queryUserIdBySession(session)) {
                sendUserInfo(DatabaseService.queryUserIdBySession(session));
            }
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    // Support HTTP Version Soon
    @MessageMapping(ClientConstant.API_LOAD_CONTACT)
    public void loadContact(SimpMessageHeaderAccessor sha, LoadContactMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int contactId = message.getContactId();

        try {
            sendContactInfo(DatabaseService.queryUserIdBySession(session), contactId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    // Support HTTP Version Soon
    @MessageMapping(ClientConstant.API_LOAD_CONTACT_BY_EMAIL)
    public void loadContactByEmail(SimpMessageHeaderAccessor sha, LoadContactByEmailMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String contactEmail = message.getContactEmail();

        try {
            int contactId = DatabaseService.queryUserIdByEmail(contactEmail);
            sendContactInfo(DatabaseService.queryUserIdBySession(session), contactId);
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_CONTACT_SYNC_FAILED_USER_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    // Support HTTP Version Soon
    @MessageMapping({ClientConstant.API_LOAD_GROUP})
    public void loadGroup(SimpMessageHeaderAccessor sha, LoadGroupMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();

        try {
            sendGroupInfo(DatabaseService.queryUserIdBySession(session), groupId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    // Support HTTP Version Soon
    @MessageMapping(ClientConstant.API_LOAD_REQUEST)
    public void loadRequest(SimpMessageHeaderAccessor sha, LoadRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            sendRequestInfo(DatabaseService.queryUserIdBySession(session), requestId);
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_LOAD_FILE_LIST)
    public void loadFileList(SimpMessageHeaderAccessor sha, LoadFileListMessage message) throws Exception {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int firstSelector = message.getFirstSelector();
        int secondSelector = message.getSecondSelector();
        List<FileInfo> fileList = DatabaseService.getFileList(firstSelector, secondSelector);
        messagingTemplate.convertAndSendToUser(
                session, ServerConstant.DIR_FILE_LIST_SYNC, new ObjectMapper().writeValueAsString(fileList));
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
                    DatabaseService.pushOfflineMessage(sender, messageSerialized, EnumMessageType.MESSAGE_TYPE_PRIVATE);
                }
                if (DatabaseService.isOnline(receiver)) {
                    forwardMessage(DatabaseService.queryLastSessionById(receiver), messageSerialized);
                } else {
                    DatabaseService.pushOfflineMessage(receiver, messageSerialized, EnumMessageType.MESSAGE_TYPE_PRIVATE);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
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
                GroupInfo groupInfo = DatabaseService.getGroup(receiver);
                String messageSerialized = new ObjectMapper().writeValueAsString(message);
                for (int memberId : groupInfo.getMemberList()) {
                    if (DatabaseService.isOnline(memberId)) {
                        forwardGroupMessage(DatabaseService.queryLastSessionById(memberId), messageSerialized);
                    } else {
                        DatabaseService.pushOfflineMessage(memberId, messageSerialized, EnumMessageType.MESSAGE_TYPE_GROUP);
                    }
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
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
                        ServerConstant.DIR_CONTACT_ADD_REQUEST_FAILED_ALREADY_CONTACT, "Success");
            } else {
                int requestId = DatabaseService.newContactRequest(userId, contactId);
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_CONTACT_ADD_REQUEST_SUCCESS, "Success");
                sendUserInfo(userId);
                sendUserInfo(contactId);
            }
        } catch (UserEmailNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_CONTACT_ADD_REQUEST_FAILED_USER_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        } catch (InvalidUserEmailException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_USER_EMAIL, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_REVOKE)
    public void contactAddRevoke(SimpMessageHeaderAccessor sha, ContactAddRevokeMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_CONTACT_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (sender == userId) {
                    DatabaseService.newContactRevoke(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_CONFIRM)
    public void contactAddConfirm(SimpMessageHeaderAccessor sha, ContactAddConfirmMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_CONTACT_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isFriend(sender, receiver)
                        && receiver != sender) {
                    DatabaseService.newContactConfirm(requestId, sender, receiver);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_CONTACT_ADD_REFUSE)
    public void contactAddRefuse(SimpMessageHeaderAccessor sha, ContactAddRefuseMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_CONTACT_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isFriend(sender, receiver)
                        && receiver != sender) {
                    DatabaseService.newContactRefuse(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_CREATE_REQUEST)
    public void groupCreateRequest(SimpMessageHeaderAccessor sha, GroupCreateRequestMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String groupName = message.getGroupName();

        try {
            PatternChecker.checkGroupName(groupName);
            int[] memberList = new ObjectMapper().readValue(message.getMemberList(), int[].class);
            int userId = DatabaseService.queryUserIdBySession(session);
            int groupId = DatabaseService.newGroup(userId);
            DatabaseService.modifyGroupName(groupId, groupName);
            for (int i : memberList) { DatabaseService.newMember(groupId, i); }
            sendUserInfo(userId);
            for (int i : memberList) { sendUserInfo(i); }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        } catch (InvalidGroupNameException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_INVALID_GROUP_NAME, "Success");
        }
    }

    private boolean isGroupHost(int userId, int groupId) throws GroupNotExistsException {
        return DatabaseService.getGroup(groupId).getGroupHostId() == userId;
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_INVITE_MEMBER)
    public void groupInviteMember(SimpMessageHeaderAccessor sha, GroupInviteMemberMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int groupId = message.getGroupId();
        int memberId = message.getMemberId();

        try {
            UserInfo memberInfo = DatabaseService.getUser(memberId);
            int userId = DatabaseService.queryUserIdBySession(session);
            if (!memberInfo.getGroupList().contains(groupId)
                    && isGroupHost(userId, groupId)) {
                DatabaseService.newMember(groupId, memberId);
                sendUserInfo(userId);
                sendUserInfo(memberId);
            }
        } catch (UserNotExistsException e) {
            logger.error("Group Invite Failed - Member ID not exists!");
            e.printStackTrace();
        } catch (GroupNotExistsException e) {
            logger.error("Group Invite Failed - Unknown Error!");
            e.printStackTrace();
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
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
                        ServerConstant.DIR_GROUP_ADD_REQUEST_FAILED_ALREADY_MEMBER, "Success");
            } else {
                GroupInfo groupInfo = DatabaseService.getGroup(groupId);
                int hostId = groupInfo.getGroupHostId();
                int requestId = DatabaseService.newMemberRequest(userId, groupId, hostId);
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_GROUP_ADD_REQUEST_SUCCESS, "Success");
                sendUserInfo(userId);
                sendUserInfo(hostId);
            }
        } catch (GroupNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_GROUP_ADD_REQUEST_FAILED_GROUP_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_REVOKE)
    public void groupAddRevoke(SimpMessageHeaderAccessor sha, GroupAddRevokeMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_GROUP_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt();
                int groupId = metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (sender == userId) {
                    DatabaseService.newMemberRevoke(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_CONFIRM)
    public void groupAddConfirm(SimpMessageHeaderAccessor sha, GroupAddConfirmMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_GROUP_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt();
                int groupId = metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isMember(groupId, sender)
                        && isGroupHost(receiver, groupId)) {
                    DatabaseService.newMemberConfirm(requestId, sender, groupId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (GroupNotExistsException e) {
            logger.error("Group Add Failed - Unknown Error!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_CHAT_GROUP_ADD_REFUSE)
    public void groupAddRefuse(SimpMessageHeaderAccessor sha, GroupAddRefuseMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int requestId = message.getRequestId();

        try {
            RequestInfo requestInfo = DatabaseService.getRequest(requestId);
            if (requestInfo.getRequestType().equals(RequestConstant.REQUEST_GROUP_ADD)
                    || requestInfo.getRequestStatus().equals(EnumRequestStatus.REQUEST_STATUS_PENDING.toString())) {
                JsonObject metadata = JsonObject.create(requestInfo.getRequestMetadata());
                int sender = metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
                int receiver = metadata.get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt();
                int groupId = metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
                int userId = DatabaseService.queryUserIdBySession(session);
                if (receiver == userId
                        && !DatabaseService.isMember(groupId, sender)
                        && isGroupHost(receiver, groupId)) {
                    DatabaseService.newMemberRefuse(requestId);
                    sendUserInfo(sender);
                    sendUserInfo(receiver);
                    sendRequestInfo(sender, requestId);
                    sendRequestInfo(receiver, requestId);
                }
            } else {
                messagingTemplate.convertAndSendToUser(session,
                        ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
            }
        } catch (GroupNotExistsException e) {
            logger.error("Group Add Failed - Unknown Error!");
            e.printStackTrace();
        } catch (RequestNotExistsException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_REQUEST_NOT_FOUND, "Success");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    @MessageMapping(ClientConstant.API_USER_REPLACE_SESSION)
    public void userSessionReplace(SimpMessageHeaderAccessor sha, UserSessionReplaceMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        String oldSession = message.getOldSession();

        try {
            PatternChecker.checkSessionToken(oldSession);
            int userId = DatabaseService.queryUserIdBySession(oldSession);
            refreshSession(userId, session);
        } catch (InvalidSessionException e) {
            logger.info("Replace Session Failed");
        } catch (SessionInvalidException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_INVALID, "Success");
        } catch (SessionExpiredException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_EXPIRED, "Success");
        } catch (SessionRevokedException e) {
            messagingTemplate.convertAndSendToUser(session,
                    ServerConstant.DIR_USER_SESSION_REVOKED, "Success");
        }
    }

    public void refreshSession(int userId, String session) {
        DatabaseService.updateSession(userId, session);
        DatabaseService.updateLoginRecord(userId);
        sendUserInfo(userId);
    }

    public void cleanUser(int userId) {
        exitVideoChatRoom(userId);
    }

    private void enterVideoChatRoom(int userId, String channel) {
        logger.info("User " + userId + " enters the chat room.");
        if (userToChannel.containsKey(userId)) {
            String lastChannel = userToChannel.get(userId);
            Set<Integer> lastChannelMembers = channelMap.get(lastChannel);
            lastChannelMembers.remove(userId);
            if (lastChannelMembers.isEmpty()) {
                channelMap.remove(lastChannel);
            }
            userToChannel.remove(userId);
        }
        if (!channelMap.containsKey(channel)) {
            channelMap.put(channel, new HashSet<>());
        }
        channelMap.get(channel).add(userId);
        userToChannel.put(userId, channel);
    }

    private void exitVideoChatRoom(int userId) {
        logger.info("User " + userId + " exits the chat room.");
        if (userToChannel.containsKey(userId)) {
            String lastChannel = userToChannel.get(userId);
            Set<Integer> lastChannelMembers = channelMap.get(lastChannel);
            lastChannelMembers.remove(userId);
            if (lastChannelMembers.isEmpty()) {
                channelMap.remove(lastChannel);
            }
            userToChannel.remove(userId);
        }
    }

    private final Map<Integer, String> userToChannel = new Hashtable<>();
    private final Map<String, Set<Integer>> channelMap = new Hashtable<>();
    @MessageMapping(ClientConstant.API_CHAT_WEBRTC_CALL)
    public void webRTCCall(SimpMessageHeaderAccessor sha, WebRTCCallMessage message) throws Exception {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int userId = DatabaseService.queryUserIdBySession(session);
        String channel = message.getChannel();
        String command = message.getCommand();
        String payload = message.getPayload();

        switch (command) {
            case "connect": {
                enterVideoChatRoom(userId, channel);
                JsonObject response = new JsonObject(new LinkedHashMap<>());
                JsonObject responsePayload = new JsonObject(new LinkedHashMap<>());
                JsonObject potentialCandidates = new JsonObject(new ArrayList<>());
                response.put(ClientConstant.CHAT_WEBRTC_CALL_CHANNEL, new JsonObject(channel));
                response.put(ClientConstant.CHAT_WEBRTC_CALL_COMMAND, new JsonObject("init"));
                for (Integer memberId : channelMap.get(channel)) {
                    if (memberId != userId) {
                        potentialCandidates.add(new JsonObject(memberId));
                    }
                }
                responsePayload.put("candidates", potentialCandidates);
                response.put(ClientConstant.CHAT_WEBRTC_CALL_PAYLOAD, new JsonObject(responsePayload.toString()));
                messagingTemplate.convertAndSendToUser(session, ServerConstant.DIR_WEBRTC_CALL, response.toString());
            }
            break;
            case "disconnect": {
                exitVideoChatRoom(userId);
            }
            break;
            case "transfer": {
                JsonObject payloadAsJson = JsonObject.create(payload);
                int from = payloadAsJson.get("from").getInt();
                int to = payloadAsJson.get("to").getInt();
                String type = payloadAsJson.get("type").getString();
                assert userId == from;
                assert userId != to;
                logger.info("Transferring = " + message.getPayload() + " from " + from + ", to " + to + ", type " + type);
                if (DatabaseService.isOnline(to)) {
                    messagingTemplate.convertAndSendToUser(
                            DatabaseService.queryLastSessionById(to),
                            ServerConstant.DIR_WEBRTC_CALL, new ObjectMapper().writeValueAsString(message));
                }
            }
            break;
        }
    }

    @MessageMapping(ClientConstant.API_FILE_ARCHIVE)
    public void fileArchive(SimpMessageHeaderAccessor sha, FileArchiveMessage message) {
        String session = Objects.requireNonNull(sha.getUser()).getName();
        int firstSelector = message.getFirstSelector();
        int secondSelector = message.getSecondSelector();
        String fileName = message.getFileName();
        String fileLink = message.getFileLink();
        int fileUploader = message.getFileUploader();
        int fileSize = message.getFileSize();
        DatabaseService.newFileDescriptor(firstSelector, secondSelector, fileName, fileLink, fileUploader, fileSize);
    }

    private String buildUserMessage(int userId)
            throws UserNotExistsException, JsonProcessingException {
        UserInfo userInfo = DatabaseService.getUser(userId);
        return new ObjectMapper().writeValueAsString(userInfo);
    }

    private String buildContactMessage(int contactId)
            throws UserNotExistsException, JsonProcessingException {
        ContactInfo contactInfo = DatabaseService.getContact(contactId);
        return new ObjectMapper().writeValueAsString(contactInfo);
    }

    private String buildGroupMessage(int groupId)
            throws GroupNotExistsException, JsonProcessingException {
        GroupInfo groupInfo = DatabaseService.getGroup(groupId);
        return new ObjectMapper().writeValueAsString(groupInfo);
    }

    private String buildRequestMessage(int requestId)
            throws RequestNotExistsException, JsonProcessingException {
        RequestInfo requestInfo = DatabaseService.getRequest(requestId);
        return new ObjectMapper().writeValueAsString(requestInfo);
    }
}
