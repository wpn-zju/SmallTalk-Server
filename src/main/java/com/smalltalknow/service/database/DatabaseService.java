package com.smalltalknow.service.database;

import com.smalltalknow.service.controller.enums.EnumMessageStatus;
import com.smalltalknow.service.controller.enums.EnumPasscodeStatus;
import com.smalltalknow.service.controller.enums.EnumRequestStatus;
import com.smalltalknow.service.controller.enums.EnumSessionStatus;
import com.smalltalknow.service.controller.websocket.RequestConstant;
import com.smalltalknow.service.database.exception.*;
import com.smalltalknow.service.database.model.GroupInfo;
import com.smalltalknow.service.database.model.RequestInfo;
import com.smalltalknow.service.database.model.UserInfo;
import com.smalltalknow.service.tool.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static final String url = "jdbc:MySQL://localhost:3306/small_talk_prod?" +
            "useUnicode=true&" +
            "characterEncoding=utf-8&" +
            "serverTimezone=UTC&" +
            "useSSL=false&" +
            "allowPublicKeyRetrieval=true";
    private static final String user = "zjuwpn";
    private static final String password = "peinan";

    private static final String searchUserIdByEmail = "select user_id from email_to_user_id where user_email = ?";
    public static boolean hasUserWithEmail(String userEmail) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchUserIdByEmailSt = con.prepareStatement(searchUserIdByEmail)) {
            searchUserIdByEmailSt.setString(1, userEmail);
            try (ResultSet rs = searchUserIdByEmailSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed checkUserByEmail(String userEmail)");
        }
    }

    public static int queryUserIdByEmail(String userEmail) throws UserEmailNotExistsException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchUserIdByEmailSt = con.prepareStatement(searchUserIdByEmail)) {
            searchUserIdByEmailSt.setString(1, userEmail);
            try (ResultSet rs = searchUserIdByEmailSt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new UserEmailNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed queryUserIdByEmail(String userEmail)");
        }
    }

    private static final String setSessionStatus = "update session_storage set session_status = ? where session_id = ?";
    private static void setSessionStatus(String session, String newStatus) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setSessionStatusSt = con.prepareStatement(setSessionStatus)) {
            setSessionStatusSt.setString(1, newStatus);
            setSessionStatusSt.setString(2, session);
            setSessionStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed setSessionStatus(String session, String newStatus)");
        }
    }

    private static final String searchSession = "select " +
            "session_id, user_id, session_create_datetime, session_expire_datetime, session_status " +
            "from session_storage where session_id = ?";
    @SuppressWarnings("unused")
    public static int queryUserIdBySession(String sessionToken)
            throws SessionInvalidException, SessionExpiredException, SessionRevokedException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchUserIdBySessionSt = con.prepareStatement(searchSession)) {
            searchUserIdBySessionSt.setString(1, sessionToken);
            try (ResultSet rs = searchUserIdBySessionSt.executeQuery()) {
                if (rs.next()) {
                    String sessionId = rs.getString(1);
                    int userId = rs.getInt(2);
                    Instant sessionCreateTime = rs.getTimestamp(3).toInstant();
                    Instant sessionExpireTime = rs.getTimestamp(4).toInstant();
                    String sessionStatus = rs.getString(5);
                    if (sessionStatus.equals(EnumSessionStatus.SESSION_STATUS_VALID.toString())) {
                        if (sessionExpireTime.isBefore(Instant.now())) {
                            setSessionStatus(sessionId, EnumSessionStatus.SESSION_STATUS_EXPIRED.toString());
                            throw new SessionExpiredException();
                        } else {
                            return userId;
                        }
                    } else if (sessionStatus.equals(EnumSessionStatus.SESSION_STATUS_EXPIRED.toString())) {
                        throw new SessionExpiredException();
                    } else if (sessionStatus.equals(EnumSessionStatus.SESSION_STATUS_REVOKED.toString())) {
                        throw new SessionRevokedException();
                    } else {
                        throw new SessionInvalidException();
                    }
                } else {
                    throw new SessionInvalidException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed queryUserIdBySession(String sessionToken)");
        }
    }

    /* Create an new account, you have to manually set user_name and user_password after the account has created. */
    private static final String createAccount = "insert into account " +
            "(user_email, user_name, user_password, contact_list, group_list, request_list, offline_message_list, last_session) " +
            "values (?, 'New User', 'Password Placeholder', '[]', '[]', '[]', '[]', '00000000-0000-0000-0000-000000000000')";
    private static final String createAccountDetail = "insert into account_detail (user_id) values (?)";
    private static final String getLastInserted = "select last_insert_id()";
    private static final String storeReverseSearchRecord = "insert into email_to_user_id " +
            "(user_email, user_id) values (?, ?)";
    public static int newAccount(String userEmail) throws DataAccessException, UserEmailExistsException {
        if (hasUserWithEmail(userEmail)) { throw new UserEmailExistsException(); }

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement createAccSt = con.prepareStatement(createAccount);
             PreparedStatement createAccDetailSt = con.prepareStatement(createAccountDetail);
             PreparedStatement getLastInsertedSt = con.prepareStatement(getLastInserted);
             PreparedStatement storeReverseSearchRecordSt = con.prepareStatement(storeReverseSearchRecord)) {
            createAccSt.setString(1, userEmail);
            createAccSt.executeUpdate();
            try (ResultSet rs = getLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    createAccDetailSt.setInt(1, userId);
                    createAccDetailSt.executeUpdate();
                    storeReverseSearchRecordSt.setString(1, userEmail);
                    storeReverseSearchRecordSt.setInt(2, userId);
                    storeReverseSearchRecordSt.executeUpdate();
                    return userId;
                } else {
                    throw new DataAccessException("Unexpected Error - Create New Account!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newAccount(String userEmail)");
        }
    }

    /* Create a new group, you have to manually add the creator user to this group after the group has created. */
    private static final String createGroup = "insert into group_info " +
            "(group_name, group_host_id, member_list) " +
            "values ('New Group', ?, '[]')";
    public static int newGroup(int hostId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement createGroupSt = con.prepareStatement(createGroup);
             PreparedStatement getLastInsertedSt = con.prepareStatement(getLastInserted)) {
            createGroupSt.setInt(1, hostId);
            createGroupSt.executeUpdate();
            try (ResultSet rs = getLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int newGroupId = rs.getInt(1);
                    newMember(newGroupId, hostId);
                    return newGroupId;
                } else {
                    throw new DataAccessException("Unexpected Error - Create New Group!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newGroup(int hostId)");
        }
    }

    private static final String editUserName = "update account set user_name = ? where user_id = ?";
    public static void modifyUserName(int userId, String newName) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement editNameSt = con.prepareStatement(editUserName)) {
            editNameSt.setString(1, newName);
            editNameSt.setInt(2, userId);
            editNameSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserName(int userId, String newName)");
        }
    }

    private static final String updatePassword = "update account set user_password = ? where user_id = ?";
    public static void modifyUserPassword(int userId, String newPassword) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement editPwdSt = con.prepareStatement(updatePassword)) {
            editPwdSt.setString(1, newPassword);
            editPwdSt.setInt(2, userId);
            editPwdSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyPassword(int userId, String newPassword)");
        }
    }

    private static final String editGroupName = "update group_info set group_name = ? where group_id = ?";
    public static void modifyGroupName(int groupId, String newGroupName) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement editNameSt = con.prepareStatement(editGroupName)) {
            editNameSt.setString(1, newGroupName);
            editNameSt.setInt(2, groupId);
            editNameSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyGroupName(int groupId, String newGroupName)");
        }
    }

    private static final String updateLoginTime = "update account set last_login = ? where user_id = ?";
    public static void updateLoginRecord(int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement loginTimeUpdSt = con.prepareStatement(updateLoginTime)) {
            loginTimeUpdSt.setTimestamp(1, Timestamp.from(Instant.now()));
            loginTimeUpdSt.setInt(2, userId);
            loginTimeUpdSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed updateUserLoginRecord(int userId)");
        }
    }

    private static final String updateLogoutTime = "update account set last_logout = ? where user_id = ?";
    public static void updateLogoutRecord(int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement logoutTimeUpdSt = con.prepareStatement(updateLogoutTime)) {
            logoutTimeUpdSt.setTimestamp(1, Timestamp.from(Instant.now()));
            logoutTimeUpdSt.setInt(2, userId);
            logoutTimeUpdSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed updateUserLogoutRecord(int userId)");
        }
    }

    private static final String searchUser = "select " +
            "user_id, user_email, user_name, user_password, contact_list, group_list, request_list, offline_message_list, last_session " +
            "from account where user_id = ?";
    public static UserInfo getUserInfo(int userId) throws UserNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchAccSt = con.prepareStatement(searchUser)) {
            searchAccSt.setInt(1, userId);
            try (ResultSet rs = searchAccSt.executeQuery()) {
                if (rs.next()) {
                    return UserInfo.builder()
                            .userId(rs.getInt(1))
                            .userEmail(rs.getString(2))
                            .userName(rs.getString(3))
                            .userPassword(rs.getString(4))
                            .contactList(JsonObject.create(rs.getString(5)).getList()
                                    .stream().map(JsonObject::getInt).collect(Collectors.toList()))
                            .groupList(JsonObject.create(rs.getString(6)).getList()
                                    .stream().map(JsonObject::getInt).collect(Collectors.toList()))
                            .requestList(JsonObject.create(rs.getString(7)).getList()
                                    .stream().map(JsonObject::getInt).collect(Collectors.toList()))
                            .session(rs.getString(9)).build();
                } else {
                    throw new UserNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getUserInfo(int userId)");
        }
    }

    private static final String searchGroup = "select " +
            "group_id, group_name, group_host_id, member_list from group_info where group_id = ?";
    public static GroupInfo getGroupInfo(int groupId) throws GroupNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchGroupSt = con.prepareStatement(searchGroup)) {
            searchGroupSt.setInt(1, groupId);
            try (ResultSet rs = searchGroupSt.executeQuery()) {
                if (rs.next()) {
                    return GroupInfo.builder()
                            .groupId(rs.getInt(1))
                            .groupName(rs.getString(2))
                            .groupHostId(rs.getInt(3))
                            .memberList(JsonObject.create(rs.getString(4)).getList()
                                    .stream().map(JsonObject::getInt).collect(Collectors.toList())).build();
                } else {
                    throw new GroupNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getGroupInfo(int groupId)");
        }
    }

    private static final String searchRequest = "select " +
            "request_id, request_status, request_type, request_metadata, visible_user_list from request where request_id = ?";
    public static RequestInfo getRequestInfo(int requestId) throws RequestNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchRequestSt = con.prepareStatement(searchRequest)) {
            searchRequestSt.setInt(1, requestId);
            try (ResultSet rs = searchRequestSt.executeQuery()) {
                if (rs.next()) {
                    return RequestInfo.builder()
                            .requestId(rs.getInt(1))
                            .requestStatus(rs.getString(2))
                            .requestType(rs.getString(3))
                            .requestMetadata(JsonObject.create(rs.getString(4)))
                            .visibleUserList(JsonObject.create(rs.getString(5))
                                    .getList().stream().map(JsonObject::getInt).collect(Collectors.toList())).build();
                } else {
                    throw new RequestNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getGroupInfo(int groupId)");
        }
    }

    private static final String checkIsFriend = "select user_id from account " +
            "where user_id = ? and json_contains(contact_list->'$', ?, '$')";
    public static boolean isFriend(int userId1, int userId2) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement checkIsFriendSt = con.prepareStatement(checkIsFriend)) {
            checkIsFriendSt.setInt(1, userId1);
            checkIsFriendSt.setString(2, String.valueOf(userId2));
            try (ResultSet rs = checkIsFriendSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed isFriend(int userId1, int userId2)");
        }
    }

    private static final String checkIsMember = "select user_id from account " +
            "where user_id = ? and json_contains(group_list->'$', ?, '$')";
    public static boolean isMember(int groupId, int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement checkIsMemberSt = con.prepareStatement(checkIsMember)) {
            checkIsMemberSt.setInt(1, userId);
            checkIsMemberSt.setString(2, String.valueOf(groupId));
            try (ResultSet rs = checkIsMemberSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed isMember(int groupId, int userId)");
        }
    }

    private static final String createNewRequest = "insert into request " +
            "(request_status, request_type, request_metadata, visible_user_list)" +
            " values (?, ?, ?, ?)";
    private static final String addNewRequestRecordToAccountTable = "update account " +
            "set request_list = json_array_append(request_list, '$', ?) where user_id = ?";
    public static int newContactRequest(int userId, int newContactId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement createNewRequestSt = con.prepareStatement(createNewRequest);
             PreparedStatement getLastInsertedSt = con.prepareStatement(getLastInserted);
             PreparedStatement storeRequestToAccountTableSt = con.prepareStatement(addNewRequestRecordToAccountTable)) {
            JsonObject metadata = new JsonObject(new HashMap<>());
            metadata.put(RequestConstant.REQUEST_CONTACT_ADD_SENDER, new JsonObject(userId));
            metadata.put(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER, new JsonObject(newContactId));
            JsonObject visibleList = new JsonObject(new ArrayList<>());
            visibleList.add(new JsonObject(userId));
            visibleList.add(new JsonObject(newContactId));
            createNewRequestSt.setString(1, EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
            createNewRequestSt.setString(2, RequestConstant.REQUEST_CONTACT_ADD);
            createNewRequestSt.setString(3, metadata.toString());
            createNewRequestSt.setString(4, visibleList.toString());
            createNewRequestSt.executeUpdate();
            try (ResultSet rs = getLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    storeRequestToAccountTableSt.setInt(1, requestId);
                    storeRequestToAccountTableSt.setInt(2, userId);
                    storeRequestToAccountTableSt.executeUpdate();
                    storeRequestToAccountTableSt.setInt(1, requestId);
                    storeRequestToAccountTableSt.setInt(2, newContactId);
                    storeRequestToAccountTableSt.executeUpdate();
                    return requestId;
                } else {
                    throw new DataAccessException("Unexpected Error - New Contact Request");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContactRequest(int userId, int newContactId)");
        }
    }

    private static final String setRequestStatus = "update request set request_status = ? where request_id = ?";
    public static void newContactConfirm(int requestId, int requesterId, int newContactId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            newContact(requesterId, newContactId);
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_ACCEPTED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContactConfirm(int requestId, int requesterId, int newContactId)");
        }
    }

    public static void newContactRefuse(int requestId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_REFUSED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContactRefuse(int requestId)");
        }
    }

    public static int newMemberRequest(int requester, int groupId, int groupHostId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement createNewRequestSt = con.prepareStatement(createNewRequest);
             PreparedStatement getLastInsertedSt = con.prepareStatement(getLastInserted);
             PreparedStatement storeRequestToAccountTableSt = con.prepareStatement(addNewRequestRecordToAccountTable)) {
            JsonObject metadata = new JsonObject(new HashMap<>());
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_SENDER, new JsonObject(requester));
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_RECEIVER, new JsonObject(groupHostId));
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID, new JsonObject(groupId));
            JsonObject visibleList = new JsonObject(new ArrayList<>());
            visibleList.add(new JsonObject(requester));
            visibleList.add(new JsonObject(groupHostId));
            createNewRequestSt.setString(1, EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
            createNewRequestSt.setString(2, RequestConstant.REQUEST_GROUP_ADD);
            createNewRequestSt.setString(3, metadata.toString());
            createNewRequestSt.setString(4, visibleList.toString());
            createNewRequestSt.executeUpdate();
            try (ResultSet rs = getLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    storeRequestToAccountTableSt.setInt(1, requestId);
                    storeRequestToAccountTableSt.setInt(2, requester);
                    storeRequestToAccountTableSt.executeUpdate();
                    storeRequestToAccountTableSt.setInt(1, requestId);
                    storeRequestToAccountTableSt.setInt(2, groupHostId);
                    storeRequestToAccountTableSt.executeUpdate();
                    return requestId;
                } else {
                    throw new DataAccessException("Unexpected Error - New Member Request");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMemberRequest(int requester, int groupId, int groupHostId)");
        }
    }

    public static void newMemberConfirm(int requestId, int requesterId, int groupId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            newMember(groupId, requesterId);
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_ACCEPTED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMemberConfirm(int requestId, int requesterId, int groupId)");
        }
    }

    public static void newMemberRefuse(int requestId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_REFUSED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMemberRefuse(int requestId)");
        }
    }

    private static final String newContact = "update account " +
            "set contact_list = json_array_append(contact_list, '$', ?) where user_id = ?";
    public static void newContact(int requesterId, int newContactId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement newContactSt = con.prepareStatement(newContact)) {
            newContactSt.setInt(1, requesterId);
            newContactSt.setInt(2, newContactId);
            newContactSt.executeUpdate();
            newContactSt.setInt(1, newContactId);
            newContactSt.setInt(2, requesterId);
            newContactSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContact(int requesterId, int newContactId)");
        }
    }

    private static final String addGroup = "update account " +
            "set group_list = json_array_append(group_list, '$', ?) where user_id = ?";
    private static final String addMember = "update group_info " +
            "set member_list = json_array_append(member_list, '$', ?) where group_id = ?";
    public static void newMember(int groupId, int requesterId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement addGroupSt = con.prepareStatement(addGroup);
             PreparedStatement addMemberSt = con.prepareStatement(addMember)) {
            addGroupSt.setInt(1, groupId);
            addGroupSt.setInt(2, requesterId);
            addGroupSt.executeUpdate();
            addMemberSt.setInt(1, requesterId);
            addMemberSt.setInt(2, groupId);
            addMemberSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMember(int groupId, int requesterId)");
        }
    }

    private static final String storeNewMessage = "insert into message (receiver_id, message_status, message_content) " +
            "values (?, ?, ?)";
    private static final String updateAccountNewMessage = "update account " +
            "set offline_message_list = json_array_append(offline_message_list, '$', ?) where user_id = ?";
    public static void pushOfflineMessage(int userId, String content) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement storeNewMessageSt = con.prepareStatement(storeNewMessage);
             PreparedStatement getLastInsertedSt = con.prepareStatement(getLastInserted);
             PreparedStatement updateAccNewMessageSt = con.prepareStatement(updateAccountNewMessage)) {
            storeNewMessageSt.setInt(1, userId);
            storeNewMessageSt.setString(2, EnumMessageStatus.MESSAGE_STATUS_PENDING.toString());
            storeNewMessageSt.setString(3, content);
            storeNewMessageSt.executeUpdate();
            try (ResultSet rs = getLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    updateAccNewMessageSt.setInt(1, messageId);
                    updateAccNewMessageSt.setInt(2, userId);
                    updateAccNewMessageSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Push New Offline Messages!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed pushOfflineMessage(int userId, String content)");
        }
    }

    private static final String searchMessage = "select " +
            "message_id, receiver_id, message_status, message_content from message where message_id = ?";
    private static final String setMessageStatus = "update message set message_status = ? where message_id = ?";
    private static final String clearAccMessageList = "update account set offline_message_list = '[]' where user_id = ?";
    public static List<JsonObject> popOfflineMessageAsList(int userId) throws DataAccessException {
        List<JsonObject> result = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchAccSt = con.prepareStatement(searchUser);
             PreparedStatement searchMessageSt = con.prepareStatement(searchMessage);
             PreparedStatement updateMessageStateSt = con.prepareStatement(setMessageStatus);
             PreparedStatement updateAccMessageListSt = con.prepareStatement(clearAccMessageList)) {
            searchAccSt.setInt(1, userId);
            try (ResultSet rs = searchAccSt.executeQuery()) {
                if (rs.next()) {
                    List<Integer> messages = JsonObject.create(rs.getString(8)).getList()
                            .stream().map(JsonObject::getInt).collect(Collectors.toList());

                    for (Integer i : messages) {
                        int textId = i;
                        searchMessageSt.setInt(1, i);
                        try (ResultSet mrs = searchMessageSt.executeQuery()) {
                            if (mrs.next()) {
                                result.add(JsonObject.create(mrs.getString(4)));
                                updateMessageStateSt.setString(1, EnumMessageStatus.MESSAGE_STATUS_POPPED.toString());
                                updateMessageStateSt.setInt(2, textId);
                                updateMessageStateSt.executeUpdate();
                            } else {
                                throw new DataAccessException("Unexpected Error - Pop Offline Messages!");
                            }
                        }
                    }

                    updateAccMessageListSt.setInt(1, userId);
                    updateAccMessageListSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Pop Offline Messages!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed popOfflineMessage(int userId)");
        }

        return result;
    }

    private static final String passcodeCharSet = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static String generateNewPasscode() {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 6; ++i) {
            int random = r.nextInt();
            int index = random % passcodeCharSet.length();
            if (index < 0) index = index + passcodeCharSet.length();
            sb.append(passcodeCharSet.charAt(index));
        }
        logger.info(String.format("New Passcode -> %s", sb.toString()));
        return sb.toString();
    }

    private static final Duration PASSCODE_EXPIRE_DURATION = Duration.ofMinutes(10);
    private static final String createPasscode = "insert into passcode " +
            "(passcode, passcode_metadata, passcode_create_datetime, passcode_expire_datetime, passcode_status) " +
            "values (?, ?, ?, ?, ?)";
    public static String newPasscode(Map<String, String> kvMap) {
        try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement createPasscodeSt = con.prepareStatement(createPasscode)) {
            String passcode = generateNewPasscode();
            JsonObject metadata = new JsonObject(new HashMap<>());
            kvMap.forEach((key, value) -> metadata.put(key, new JsonObject(value)));
            createPasscodeSt.setString(1, passcode);
            createPasscodeSt.setString(2, metadata.toString());
            createPasscodeSt.setTimestamp(3, Timestamp.from(Instant.now()));
            createPasscodeSt.setTimestamp(4, Timestamp.from(Instant.now().plus(PASSCODE_EXPIRE_DURATION)));
            createPasscodeSt.setString(5, EnumPasscodeStatus.PASSCODE_STATUS_PENDING.toString());
            createPasscodeSt.executeUpdate();
            return passcode;
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed createPasscode(Map<String, String> kvMap)");
        }
    }


    private static final String setPasscodeStatus = "update passcode set passcode_status = ? where passcode = ?";
    private static void setPasscodeStatus(String passcode, String newStatus) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setPasscodeStatusSt = con.prepareStatement(setPasscodeStatus)) {
            setPasscodeStatusSt.setString(1, newStatus);
            setPasscodeStatusSt.setString(2, passcode);
            setPasscodeStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed setPasscodeStatus(String passcode, String newStatus)");
        }
    }

    private static final String searchPasscode = "select " +
            "passcode_metadata, passcode_create_datetime, passcode_expire_datetime, passcode_status " +
            "from passcode where passcode = ?";
    @SuppressWarnings("unused")
    public static void checkPasscode(String passcode, Map<String, String> kvMap) throws PasscodeException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchPasscodeSt = con.prepareStatement(searchPasscode)) {
            searchPasscodeSt.setString(1, passcode);
            try (ResultSet rs = searchPasscodeSt.executeQuery()) {
                if (rs.next()) {
                    String metadataRaw = rs.getString(1);
                    Instant passcodeCreateTime = rs.getTimestamp(2).toInstant();
                    Instant passcodeExpireTime = rs.getTimestamp(3).toInstant();
                    String passcodeStatus = rs.getString(4);
                    if (passcodeStatus.equals(EnumPasscodeStatus.PASSCODE_STATUS_PENDING.toString())) {
                        if (passcodeExpireTime.isBefore(Instant.now())) {
                            setPasscodeStatus(passcode, EnumPasscodeStatus.PASSCODE_STATUS_EXPIRED.toString());
                            throw new PasscodeException();
                        } else {
                            JsonObject metadata = JsonObject.create(metadataRaw);
                            for (Map.Entry<String, String> entry : kvMap.entrySet()) {
                                if (!metadata.containsKey(entry.getKey())
                                        || !metadata.get(entry.getKey()).getString().equals(entry.getValue())) {
                                    throw new PasscodeException();
                                }
                            }
                        }
                    } else {
                        throw new PasscodeException();
                    }
                } else {
                    throw new PasscodeException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed checkPasscode(String passcode, Map<String, String> kvMap)");
        }
    }

    private static final Duration SESSION_EXPIRE_DURATION = Duration.ofDays(30);
    private static final String createSession = "insert into session_storage " +
            "(session_id, user_id, session_create_datetime, session_expire_datetime, session_status) " +
            "values (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
            "session_id = ?, user_id = ?, session_create_datetime =?, session_expire_datetime = ?, session_status = ?";
    private static final String updateLastSession = "update account set last_session = ? where user_id = ?";
    public static void updateSession(int userId, String session) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchLastSessionSt = con.prepareStatement(findLastSession);
             PreparedStatement revokePreviousSt = con.prepareStatement(setSessionStatus);
             PreparedStatement createSessionSt = con.prepareStatement(createSession);
             PreparedStatement updatePreviousSessionSt = con.prepareStatement(updateLastSession)) {
            searchLastSessionSt.setInt(1, userId);
            try (ResultSet rs = searchLastSessionSt.executeQuery()) {
                if (rs.next()) {
                    String lastSession = rs.getString(1);
                    if (lastSession != null) {
                        revokePreviousSt.setString(1, EnumSessionStatus.SESSION_STATUS_REVOKED.toString());
                        revokePreviousSt.setString(2, lastSession);
                        revokePreviousSt.executeUpdate();
                    }
                    createSessionSt.setString(1, session);
                    createSessionSt.setString(6, session);
                    createSessionSt.setInt(2, userId);
                    createSessionSt.setInt(7, userId);
                    createSessionSt.setTimestamp(3, Timestamp.from(Instant.now()));
                    createSessionSt.setTimestamp(8, Timestamp.from(Instant.now()));
                    createSessionSt.setTimestamp(4, Timestamp.from(Instant.now().plus(SESSION_EXPIRE_DURATION)));
                    createSessionSt.setTimestamp(9, Timestamp.from(Instant.now().plus(SESSION_EXPIRE_DURATION)));
                    createSessionSt.setString(5, EnumSessionStatus.SESSION_STATUS_VALID.toString());
                    createSessionSt.setString(10, EnumSessionStatus.SESSION_STATUS_VALID.toString());
                    createSessionSt.executeUpdate();
                    updatePreviousSessionSt.setString(1, session);
                    updatePreviousSessionSt.setInt(2, userId);
                    updatePreviousSessionSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Revoke Last Session!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed createSession(int userId, String session)");
        }
    }

    // Currently do not check expiration.
    private static final String searchUserLastSession = "select last_session from account where user_id = ?";
    public static boolean checkSession(int userId, String sessionToken) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchUserLastSessionSt = con.prepareStatement(searchUserLastSession)) {
            searchUserLastSessionSt.setInt(1, userId);
            try (ResultSet rs = searchUserLastSessionSt.executeQuery()) {
                if (rs.next()) {
                    String lastSession = rs.getString(1);
                    return lastSession.equals(sessionToken);
                } else {
                    throw new DataAccessException("Unexpected Error - Check Session!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed checkSession(int userId, String sessionToken)");
        }
    }

    private static final String searchUserLogRecord = "select last_login, last_logout from account where user_id = ?";
    public static boolean isOnline(int userId) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement searchUserLogRecordSt = con.prepareStatement(searchUserLogRecord)) {
            searchUserLogRecordSt.setInt(1, userId);
            try (ResultSet rs = searchUserLogRecordSt.executeQuery()) {
                if (rs.next()) {
                    Instant lastLogin = rs.getTimestamp(1).toInstant();
                    Instant lastLogout = rs.getTimestamp(2).toInstant();
                    return lastLogout.plus(Duration.ofMillis(1)).isBefore(lastLogin);
                } else {
                    throw new DataAccessException("Unexpected Error - Check If Online!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed isOnline(int userId)");
        }
    }

    private static final String findLastSession = "select last_session from account where user_id = ?";
    public static String queryLastSessionById(int userId) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement findLastSessionSt = con.prepareStatement(findLastSession)) {
            findLastSessionSt.setInt(1, userId);
            try (ResultSet rs = findLastSessionSt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new DataAccessException("Unexpected Error - Find Last Session By User ID!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed findLastSessionByUserId(int userId)");
        }
    }

    // WARNING - FOR UNIT TEST ONLY
    private static final String deleteUser = "delete from account";
    private static final String deleteUserDetail = "delete from account_detail";
    private static final String deleteReverseMap = "delete from email_to_user_id";
    private static final String deleteGroup = "delete from group_info";
    private static final String deleteMessage = "delete from message";
    private static final String deletePasscode = "delete from passcode";
    private static final String deleteRequest = "delete from request";
    private static final String deleteSession = "delete from session_storage";
    public static void reset() {
        try (Connection con = DriverManager.getConnection(url, user, password);
            Statement deleteSt = con.createStatement()) {
            deleteSt.executeUpdate(deleteUser);
            deleteSt.executeUpdate(deleteUserDetail);
            deleteSt.executeUpdate(deleteReverseMap);
            deleteSt.executeUpdate(deleteGroup);
            deleteSt.executeUpdate(deleteMessage);
            deleteSt.executeUpdate(deletePasscode);
            deleteSt.executeUpdate(deleteRequest);
            deleteSt.executeUpdate(deleteSession);
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed resetDatabase()");
        }
    }
}
