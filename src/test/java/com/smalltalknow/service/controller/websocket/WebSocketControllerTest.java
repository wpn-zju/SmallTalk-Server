package com.smalltalknow.service.controller.websocket;

import com.smalltalknow.service.database.DatabaseService;
import com.smalltalknow.service.message.*;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

// Todo: Write Unit Test for Class WebSocketController
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class WebSocketControllerTest {

    private final String userEmailFake = "TestUserFake@peinanweng.com";
    private final String userEmail1 = "TestUser1@peinanweng.com";
    private final String userEmail2 = "TestUser2@peinanweng.com";
    private final String userEmail3 = "TestUser3@peinanweng.com";
    private final String userEmail4 = "TestUser4@peinanweng.com";
    private final String userEmail5 = "TestUser5@peinanweng.com";
    private final String userEmail6 = "TestUser6@peinanweng.com";
    private final String userEmail7 = "TestUser7@peinanweng.com";
    private final String userEmail8 = "TestUser8@peinanweng.com";
    private final String userEmail9 = "TestUser9@peinanweng.com"; // late create
    private final String userEmail10 = "TestUser10@peinanweng.com";
    private final String userEmail11 = "TestUser11@peinanweng.com";
    private int userId1 = 0;
    private int userId2 = 0;
    private int userId3 = 0;
    private int userId4 = 0;
    private int userId5 = 0;
    private int userId6 = 0;
    private int userId7 = 0;
    private int userId8 = 0;
    private int userId9 = 0; // late create
    private int userId10 = 0; // for session testing
    private int userId11 = 0; // for login / logout record and online checking
    private final String userPassword1 = "000001";
    private final String userPassword2 = "000002";
    private final String userPassword3 = "000003";
    private final String userPassword4 = "000004";
    private final String userPassword5 = "000005";
    private final String userPassword6 = "000006";
    private final String userPassword7 = "000007";
    private final String userPassword8 = "000008";
    private final String userPassword9 = "000009";
    private final String userPassword10 = "000010";
    private final String userPassword11 = "000011";
    private final String groupName1 = "Group 1";
    private final String groupNameNew1 = "Group 1 New";
    private final String groupName2 = "Group 2";
    private final String groupName3 = "Group 3";
    private int groupId1 = 0;
    private int groupId2 = 0;
    private int groupId3 = 0; // late create
    private final String user1NewName = "I AM USER 1";
    private final String user1NewPassword = "EasyPassword";

    private int requestId1 = 0;
    private int requestId2 = 0;
    private int requestId3 = 0;
    private int requestId4 = 0;
    private int requestId5 = 0;
    private int requestId6 = 0;
    private int requestId7 = 0; // for get request info

    private final String sampleMessageContent = "Hello World!";

    private final String sessionId1 = "17fc8d6e-238a-11eb-adc1-0242ac120002";
    private final String sessionId2 = "17fc8f94-238a-11eb-adc1-0242ac120002";
    private final String sessionId3 = "17fc908e-238a-11eb-adc1-0242ac120002";
    private final String sessionId4 = "17fc9160-238a-11eb-adc1-0242ac120002";
    private final String sessionId5 = "17fc9228-238a-11eb-adc1-0242ac120002";
    private final String sessionId6 = "17fc955c-238a-11eb-adc1-0242ac120002";
    private final String sessionId7 = "17fc9638-238a-11eb-adc1-0242ac120002";
    private final String sessionId8 = "17fc96f6-238a-11eb-adc1-0242ac120002";
    private final String sessionId9 = "17fc98c2-238a-11eb-adc1-0242ac120002";
    private final String sessionId10 = "17fc99a8-238a-11eb-adc1-0242ac120002";

    private final String passcode1 = "";
    private final String passcode2 = "";
    private final String passcode3 = "";
    private final String passcode4 = "";
    private final String passcode5 = "";
    private final String passcode6 = "";
    private final String passcode7 = "";
    private final String passcode8 = "";
    private final String passcode9 = "";
    private final String passcode10 = "";

    private WebSocketController webSocketController;

    @BeforeSuite
    public void beforeSuite() throws Exception {
        DatabaseService.reset();
        userId1 = DatabaseService.newAccount(userEmail1);
        userId2 = DatabaseService.newAccount(userEmail2);
        userId3 = DatabaseService.newAccount(userEmail3);
        userId4 = DatabaseService.newAccount(userEmail4);
        userId5 = DatabaseService.newAccount(userEmail5);
        userId6 = DatabaseService.newAccount(userEmail6);
        userId7 = DatabaseService.newAccount(userEmail7);
        userId8 = DatabaseService.newAccount(userEmail8);
        userId10 = DatabaseService.newAccount(userEmail10);
        userId11 = DatabaseService.newAccount(userEmail11);
        groupId1 = DatabaseService.newGroup(userId3);
        groupId2 = DatabaseService.newGroup(userId8);
        DatabaseService.newContact(userId1, userId2);
        DatabaseService.newContact(userId7, userId8);
        DatabaseService.modifyGroupName(groupId1, groupName1);
        DatabaseService.modifyGroupName(groupId2, groupName2);
        assertNotEquals(userId1, 0);
        assertNotEquals(userId2, 0);
        assertNotEquals(userId3, 0);
        assertNotEquals(userId4, 0);
        assertNotEquals(userId5, 0);
        assertNotEquals(userId6, 0);
        assertNotEquals(userId7, 0);
        assertNotEquals(userId8, 0);
        assertNotEquals(userId10, 0);
        assertNotEquals(userId11, 0);
        assertNotEquals(groupId1, 0);
        assertNotEquals(groupId2, 0);
        requestId2 = DatabaseService.newContactRequest(userId3, userId1);
        requestId3 = DatabaseService.newContactRequest(userId4, userId1);
        requestId5 = DatabaseService.newMemberRequest(userId1, groupId1, userId3);
        requestId6 = DatabaseService.newMemberRequest(userId2, groupId1, userId3);
        requestId7 = DatabaseService.newContactRequest(userId3, userId4);

        webSocketController = new WebSocketController();
    }

    @AfterSuite
    public void afterSuite() {
        DatabaseService.reset();
    }

    @Test
    public void testUserSignUp() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        UserSignUpMessage message = Mockito.mock(UserSignUpMessage.class);
        Mockito.when(message.getUserEmail()).thenReturn(userEmail1);
        Mockito.when(message.getUserPassword()).thenReturn(userPassword1);
        Mockito.when(message.getPasscode()).thenReturn(passcode1);
        webSocketController.userSignUp(sha, message);

        // Assert
        assertTrue(DatabaseService.hasUserWithEmail(userEmail1));

    }

    @Test
    public void testUserSignUpPasscodeRequest() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId2);
        UserSignUpPasscodeRequestMessage message = Mockito.mock(UserSignUpPasscodeRequestMessage.class);
        Mockito.when(message.getUserEmail()).thenReturn(userEmail2);
        webSocketController.userSignUpPasscodeRequest(sha, message);

        // Assert
        Mockito.verify(webSocketController).messagingTemplate.convertAndSendToUser(sessionId1,
                ServerStrings.DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_EMAIL_EXISTS, new Object());
    }

    @Test
    public void testUserRecoverPassword() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId3);
        UserRecoverPasswordMessage message = Mockito.mock(UserRecoverPasswordMessage.class);
        Mockito.when(message.getUserEmail()).thenReturn(userEmail3);
        Mockito.when(message.getUserPassword()).thenReturn(userPassword3);
        Mockito.when(message.getPasscode()).thenReturn(passcode3);
        webSocketController.userRecoverPassword(sha, message);

        // Assert

    }

    @Test
    public void testUserRecoverPasswordPasscodeRequest() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId4);
        UserRecoverPasswordPasscodeRequestMessage message = Mockito.mock(UserRecoverPasswordPasscodeRequestMessage.class);
        Mockito.when(message.getUserEmail()).thenReturn(userEmail4);
        webSocketController.userRecoverPasswordPasscodeRequest(sha, message);

        // Assert

    }

    @Test
    public void testUserSignIn() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId5);
        UserSignInMessage message = Mockito.mock(UserSignInMessage.class);
        Mockito.when(message.getUserEmail()).thenReturn(userEmail5);
        Mockito.when(message.getUserPassword()).thenReturn(userPassword5);
        webSocketController.userSignIn(sha, message);

        // Assert

    }

    private final String lastSessionId = "17fc955c-238a-11eb-adc1-0242ac120002";
    @Test
    public void testUserSessionSignIn() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId6);
        UserSessionSignInMessage message = Mockito.mock(UserSessionSignInMessage.class);
        Mockito.when(message.getSessionToken()).thenReturn(lastSessionId);
        webSocketController.userSessionSignIn(sha, message);

        // Assert

    }

    @Test
    public void testUserSessionSignOut() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId7);
        UserSessionSignOutMessage message = Mockito.mock(UserSessionSignOutMessage.class);
        webSocketController.userSessionSignOut(sha, message);

        // Assert

    }

    @Test
    public void testUserModifyName() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId8);
        UserModifyNameMessage message = Mockito.mock(UserModifyNameMessage.class);
        Mockito.when(message.getNewUserName()).thenReturn(user1NewName);
        webSocketController.userModifyName(sha, message);

        // Assert

    }

    @Test
    public void testUserModifyPassword() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId9);
        UserModifyPasswordMessage message = Mockito.mock(UserModifyPasswordMessage.class);
        Mockito.when(message.getNewUserPassword()).thenReturn(user1NewPassword);
        webSocketController.userModifyPassword(sha, message);

        // Assert

    }

    @Test
    public void testUserSync() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId10);
        UserSyncMessage message = Mockito.mock(UserSyncMessage.class);
        webSocketController.userSync(sha, message);

        // Assert

    }

    @Test
    public void testMessageForward() {
        String timestamp = Timestamp.from(Instant.now()).toString();

        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        MessageForwardMessage message = Mockito.mock(MessageForwardMessage.class);
        Mockito.when(message.getSender()).thenReturn(userId1);
        Mockito.when(message.getReceiver()).thenReturn(userId2);
        Mockito.when(message.getContent()).thenReturn(sampleMessageContent);
        Mockito.when(message.getContentType()).thenReturn(ClientStrings.CHAT_CONTENT_TYPE_TEXT);
        Mockito.when(message.getTimestamp()).thenReturn(timestamp);
        webSocketController.messageForward(sha, message);

        // Assert

    }

    @Test
    public void testMessageForwardGroup() {
        String timestamp = Timestamp.from(Instant.now()).toString();

        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        MessageForwardGroupMessage message = Mockito.mock(MessageForwardGroupMessage.class);
        Mockito.when(message.getSender()).thenReturn(userId1);
        Mockito.when(message.getReceiver()).thenReturn(userId2);
        Mockito.when(message.getContent()).thenReturn(sampleMessageContent);
        Mockito.when(message.getContentType()).thenReturn(ClientStrings.CHAT_CONTENT_TYPE_TEXT);
        Mockito.when(message.getTimestamp()).thenReturn(timestamp);
        webSocketController.messageForwardGroup(sha, message);

        // Assert

    }

    @Test
    public void testContactAddRequest() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        ContactAddRequestMessage message = Mockito.mock(ContactAddRequestMessage.class);
        Mockito.when(message.getContactEmail()).thenReturn(userEmail3);
        webSocketController.contactAddRequest(sha, message);

        // Assert

    }

    @Test
    public void testContactAddConfirm() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        ContactAddConfirmMessage message = Mockito.mock(ContactAddConfirmMessage.class);
        Mockito.when(message.getRequestId()).thenReturn(requestId2);
        webSocketController.contactAddConfirm(sha, message);

        // Assert

    }

    @Test
    public void testContactAddRefuse() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        ContactAddRefuseMessage message = Mockito.mock(ContactAddRefuseMessage.class);
        Mockito.when(message.getRequestId()).thenReturn(requestId3);
        webSocketController.contactAddRefuse(sha, message);

        // Assert

    }

    @Test
    public void testGroupCreateRequest() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        GroupCreateRequestMessage message = Mockito.mock(GroupCreateRequestMessage.class);
        Mockito.when(message.getGroupName()).thenReturn(groupName3);
        webSocketController.groupCreateRequest(sha, message);

        // Assert

    }

    @Test
    public void testGroupModifyName() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        GroupModifyNameMessage message = Mockito.mock(GroupModifyNameMessage.class);
        Mockito.when(message.getGroupId()).thenReturn(groupId1);
        Mockito.when(message.getNewGroupName()).thenReturn(groupNameNew1);
        webSocketController.groupModifyName(sha, message);

        // Assert

    }

    @Test
    public void testGroupAddRequest() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        GroupAddRequestMessage message = Mockito.mock(GroupAddRequestMessage.class);
        Mockito.when(message.getGroupId()).thenReturn(groupId1);
        webSocketController.groupAddRequest(sha, message);

        // Assert

    }

    @Test
    public void testGroupAddConfirm() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        GroupAddConfirmMessage message = Mockito.mock(GroupAddConfirmMessage.class);
        Mockito.when(message.getRequestId()).thenReturn(requestId5);
        webSocketController.groupAddConfirm(sha, message);

        // Assert

    }

    @Test
    public void testGroupAddRefuse() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        GroupAddRefuseMessage message = Mockito.mock(GroupAddRefuseMessage.class);
        Mockito.when(message.getRequestId()).thenReturn(requestId6);
        webSocketController.groupAddRefuse(sha, message);

        // Assert

    }

    @Test
    public void testWebrtcCall() {
        // Mock
        SimpMessageHeaderAccessor sha = Mockito.mock(SimpMessageHeaderAccessor.class);
        Mockito.when(Objects.requireNonNull(sha.getUser()).getName()).thenReturn(sessionId1);
        WebRTCCallMessage message = Mockito.mock(WebRTCCallMessage.class);
        Mockito.when(message.getSender()).thenReturn(userId1);
        Mockito.when(message.getReceiver()).thenReturn(userId2);
        Mockito.when(message.getWebRTCCommand()).thenReturn(VideoCommands.WEBRTC_COMMAND_REQUEST);
        Mockito.when(message.getWebRTCSessionDescription()).thenReturn(null);
        webSocketController.webrtcCall(sha, message);

        // Assert

    }
}
