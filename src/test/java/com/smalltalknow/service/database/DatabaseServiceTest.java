package com.smalltalknow.service.database;

import com.smalltalknow.service.controller.enums.EnumRequestStatus;
import com.smalltalknow.service.controller.websocket.ClientConstant;
import com.smalltalknow.service.controller.websocket.RequestConstant;
import com.smalltalknow.service.database.exception.*;
import com.smalltalknow.service.database.model.GroupInfo;
import com.smalltalknow.service.database.model.RequestInfo;
import com.smalltalknow.service.tool.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DatabaseServiceTest {

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
    }

    @AfterSuite
    public void afterSuite() {
        DatabaseService.reset();
    }

    @Test
    public void testCheckUserByEmail() {
        assertFalse(DatabaseService.hasUserWithEmail(userEmailFake));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail1));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail2));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail3));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail4));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail5));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail6));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail7));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail8));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail10));
        assertTrue(DatabaseService.hasUserWithEmail(userEmail11));
    }

    @Test
    public void testQueryUserIdByEmail() throws UserEmailNotExistsException {
        assertEquals(userId1, DatabaseService.queryUserIdByEmail(userEmail1));
        assertEquals(userId2, DatabaseService.queryUserIdByEmail(userEmail2));
        assertEquals(userId3, DatabaseService.queryUserIdByEmail(userEmail3));
        assertEquals(userId4, DatabaseService.queryUserIdByEmail(userEmail4));
        assertEquals(userId5, DatabaseService.queryUserIdByEmail(userEmail5));
        assertEquals(userId6, DatabaseService.queryUserIdByEmail(userEmail6));
        assertEquals(userId7, DatabaseService.queryUserIdByEmail(userEmail7));
        assertEquals(userId8, DatabaseService.queryUserIdByEmail(userEmail8));
    }

    @Test(expectedExceptions = UserEmailNotExistsException.class)
    public void testQueryUserIdByEmailUserNotExists() throws UserEmailNotExistsException {
        DatabaseService.queryUserIdByEmail(userEmailFake);
    }

    @Test
    public void testNewAccount() throws UserEmailExistsException {
        userId9 = DatabaseService.newAccount(userEmail9);
    }

    @Test(expectedExceptions = UserEmailExistsException.class)
    public void testNewAccountConflict() throws UserEmailExistsException {
        userId9 = DatabaseService.newAccount(userEmail1);
    }

    @Test
    public void testNewGroup() {
        groupId3 = DatabaseService.newGroup(userId7);
    }

    @Test
    public void testModifyUserName() throws UserNotExistsException {
        DatabaseService.modifyUserName(userId1, user1NewName);
        assertEquals(user1NewName, DatabaseService.getUserInfo(userId1).getUserName());
    }

    @Test
    public void testModifyPassword() throws UserNotExistsException {
        DatabaseService.modifyUserPassword(userId1, user1NewPassword);
        assertEquals(user1NewPassword, DatabaseService.getUserInfo(userId1).getUserPassword());
    }

    @Test
    public void testModifyGroupName() throws GroupNotExistsException {
        DatabaseService.modifyGroupName(groupId1, groupNameNew1);
        assertEquals(groupNameNew1, DatabaseService.getGroupInfo(groupId1).getGroupName());
    }

    @Test
    public void testGetUserInfo() {
    }

    @Test
    public void testGetGroupInfo() {
    }

    @Test
    public void testGetRequestInfo() {
    }

    @Test
    public void testIsFriend() {
        assertTrue(DatabaseService.isFriend(userId1, userId2));
        assertFalse(DatabaseService.isFriend(userId1, userId3));
    }

    @Test
    public void testIsMember() {
        assertTrue(DatabaseService.isMember(groupId1, userId3));
        assertFalse(DatabaseService.isMember(groupId1, userId1));
    }

    @Test
    public void testNewContactRequest() throws RequestNotExistsException {
        requestId1 = DatabaseService.newContactRequest(userId5, userId6);
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId1);
        assertEquals(requestInfo.getRequestId(), requestId1);
        Assert.assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
        Assert.assertEquals(requestInfo.getRequestType(), RequestConstant.REQUEST_CONTACT_ADD);
        JsonObject metadata = requestInfo.getRequestMetadata();
        assertEquals(metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt(), userId5);
        assertEquals(metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt(), userId6);
        List<Integer> visibleUserList = requestInfo.getVisibleUserList();
        assertEquals(visibleUserList.size(), 2);
        assertTrue(visibleUserList.contains(userId5));
        assertTrue(visibleUserList.contains(userId6));
    }

    @Test
    public void testNewContactConfirm() throws RequestNotExistsException {
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId2);
        JsonObject metadata = requestInfo.getRequestMetadata();
        int sender = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
        int receiver = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
        assertFalse(DatabaseService.isFriend(sender, receiver));
        DatabaseService.newContactConfirm(requestId2, sender, receiver);
        requestInfo = DatabaseService.getRequestInfo(requestId2);
        assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_ACCEPTED.toString());
        assertTrue(DatabaseService.isFriend(sender, receiver));
    }

    @Test
    public void testNewContactRefuse() throws RequestNotExistsException {
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId3);
        JsonObject metadata = requestInfo.getRequestMetadata();
        int sender = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_SENDER).getInt();
        int receiver = metadata.get(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER).getInt();
        assertFalse(DatabaseService.isFriend(sender, receiver));
        DatabaseService.newContactRefuse(requestId2);
        requestInfo = DatabaseService.getRequestInfo(requestId2);
        assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_REFUSED.toString());
        assertFalse(DatabaseService.isFriend(sender, receiver));
    }

    @Test
    public void testNewMemberRequest() throws GroupNotExistsException, RequestNotExistsException {
        GroupInfo groupInfo = DatabaseService.getGroupInfo(groupId1);
        requestId4 = DatabaseService.newMemberRequest(userId4, groupInfo.getGroupId(), groupInfo.getGroupHostId());
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId4);
        assertEquals(requestInfo.getRequestId(), requestId4);
        assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
        assertEquals(requestInfo.getRequestType(), RequestConstant.REQUEST_GROUP_ADD);
        JsonObject metadata = requestInfo.getRequestMetadata();
        assertEquals(metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt(), userId4);
        assertEquals(metadata.get(RequestConstant.REQUEST_GROUP_ADD_RECEIVER).getInt(), groupInfo.getGroupHostId());
        assertEquals(metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt(), groupInfo.getGroupId());
        List<Integer> visibleUserList = requestInfo.getVisibleUserList();
        assertEquals(visibleUserList.size(), 2);
        assertTrue(visibleUserList.contains(userId4));
        assertTrue(visibleUserList.contains(groupInfo.getGroupHostId()));
    }

    @Test
    public void testNewMemberConfirm() throws RequestNotExistsException {
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId5);
        JsonObject metadata = requestInfo.getRequestMetadata();
        int sender = metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
        int groupId = metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
        assertFalse(DatabaseService.isMember(groupId, sender));
        DatabaseService.newMemberConfirm(requestId5, sender, groupId);
        requestInfo = DatabaseService.getRequestInfo(requestId5);
        assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_ACCEPTED.toString());
        assertTrue(DatabaseService.isMember(groupId, sender));
    }

    @Test
    public void testNewMemberRefuse() throws RequestNotExistsException {
        RequestInfo requestInfo = DatabaseService.getRequestInfo(requestId6);
        JsonObject metadata = requestInfo.getRequestMetadata();
        int sender = metadata.get(RequestConstant.REQUEST_GROUP_ADD_SENDER).getInt();
        int groupId = metadata.get(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID).getInt();
        assertFalse(DatabaseService.isMember(groupId, sender));
        DatabaseService.newMemberRefuse(requestId5);
        requestInfo = DatabaseService.getRequestInfo(requestId5);
        assertEquals(requestInfo.getRequestStatus(), EnumRequestStatus.REQUEST_STATUS_REFUSED.toString());
        assertFalse(DatabaseService.isMember(groupId, sender));
    }

    @Test
    public void testPushAndPopOfflineMessage() {
        List<JsonObject> messageList = DatabaseService.popOfflineMessageAsList(userId1);
        assertEquals(messageList.size(), 0);
        String timestamp = Timestamp.from(Instant.now()).toString();
        String sampleMessage = String.format("{" +
                        "\"%s\":%d, " +
                        "\"%s\":%d, " +
                        "\"%s\":\"%s\", " +
                        "\"%s\":\"%s\", " +
                        "\"%s\":\"%s\"}",
                ClientConstant.CHAT_MESSAGE_FORWARD_SENDER, userId1,
                ClientConstant.CHAT_MESSAGE_FORWARD_RECEIVER, userId2,
                ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT, sampleMessageContent,
                ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT_TYPE, ClientConstant.CHAT_CONTENT_TYPE_TEXT,
                ClientConstant.TIMESTAMP, timestamp);
        DatabaseService.pushOfflineMessage(userId1, sampleMessage);
        messageList = DatabaseService.popOfflineMessageAsList(userId1);
        assertEquals(messageList.size(), 1);
        JsonObject message = messageList.get(0);
        assertEquals(message.get(ClientConstant.CHAT_MESSAGE_FORWARD_SENDER).getInt(), userId1);
        assertEquals(message.get(ClientConstant.CHAT_MESSAGE_FORWARD_RECEIVER).getInt(), userId2);
        assertEquals(message.get(ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT).getString(), sampleMessageContent);
        assertEquals(message.get(ClientConstant.CHAT_MESSAGE_FORWARD_CONTENT_TYPE).getString(), ClientConstant.CHAT_CONTENT_TYPE_TEXT);
        assertEquals(message.get(ClientConstant.TIMESTAMP).getString(), timestamp);
        messageList = DatabaseService.popOfflineMessageAsList(userId1);
        assertEquals(messageList.size(), 0);
    }

    @Test
    public void testCreateAndCheckPasscode() throws PasscodeException {
        Map<String, String> kvMap = new HashMap<>();
        kvMap.put("key1", "value1");
        kvMap.put("key2", "value2");
        kvMap.put("key3", "value3");
        String passcode = DatabaseService.newPasscode(kvMap);
        DatabaseService.checkPasscode(passcode, kvMap);
    }

    @Test(expectedExceptions = PasscodeException.class)
    public void testCreateAndCheckInvalidPasscode() throws PasscodeException {
        Map<String, String> kvMap = new HashMap<>();
        kvMap.put("key1", "value1");
        kvMap.put("key2", "value2");
        kvMap.put("key3", "value3");
        String passcode = DatabaseService.newPasscode(kvMap);
        kvMap.put("key1", "value2");
        DatabaseService.checkPasscode(passcode, kvMap);
    }

    @Test
    public void testStoreAndCheckAndFindSession() throws SessionInvalidException, SessionExpiredException, SessionRevokedException {
        String sessionToken1 = UUID.randomUUID().toString();
        DatabaseService.updateSession(userId10, sessionToken1);
        assertEquals(DatabaseService.queryUserIdBySession(sessionToken1), userId10);
        assertEquals(DatabaseService.queryLastSessionById(userId10), sessionToken1);
        assertTrue(DatabaseService.checkSession(userId10, sessionToken1));
        String sessionToken2 = UUID.randomUUID().toString();
        DatabaseService.updateSession(userId10, sessionToken2);
        assertEquals(DatabaseService.queryUserIdBySession(sessionToken2), userId10);
        assertEquals(DatabaseService.queryLastSessionById(userId10), sessionToken2);
        assertFalse(DatabaseService.checkSession(userId10, sessionToken1));
        assertTrue(DatabaseService.checkSession(userId10, sessionToken2));
    }

    @Test
    public void testIsOnline() {
        assertFalse(DatabaseService.isOnline(userId11));
        DatabaseService.updateLoginRecord(userId11);
        assertTrue(DatabaseService.isOnline(userId11));
        DatabaseService.updateLogoutRecord(userId11);
        assertFalse(DatabaseService.isOnline(userId11));
        DatabaseService.updateLoginRecord(userId11);
        assertTrue(DatabaseService.isOnline(userId11));
        DatabaseService.updateLogoutRecord(userId11);
        assertFalse(DatabaseService.isOnline(userId11));
        DatabaseService.updateLoginRecord(userId11);
        DatabaseService.updateLogoutRecord(userId11);
        assertFalse(DatabaseService.isOnline(userId11));
    }
}
