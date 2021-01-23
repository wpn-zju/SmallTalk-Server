package com.smalltalknow.service.database;

import com.smalltalknow.service.controller.enums.*;
import com.smalltalknow.service.controller.websocket.RequestConstant;
import com.smalltalknow.service.database.exception.*;
import com.smalltalknow.service.database.model.*;
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
    private static final String defaultAvatarLink = "https://peinanweng.com/download_index/base/avatar.png";

    private static final String queryUserIdByEmail = "select user_id from email_to_user_id where user_email = ?";
    public static boolean hasUserWithEmail(String userEmail) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserIdByEmailSt = con.prepareStatement(queryUserIdByEmail)) {
            queryUserIdByEmailSt.setString(1, userEmail);
            try (ResultSet rs = queryUserIdByEmailSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed hasUserWithEmail(String userEmail)");
        }
    }

    public static int queryUserIdByEmail(String userEmail) throws UserEmailNotExistsException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserIdByEmailSt = con.prepareStatement(queryUserIdByEmail)) {
            queryUserIdByEmailSt.setString(1, userEmail);
            try (ResultSet rs = queryUserIdByEmailSt.executeQuery()) {
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

    private static final String querySession = "select " +
            "session_id, user_id, session_create_datetime, session_expire_datetime, session_status " +
            "from session_storage where session_id = ?";
    @SuppressWarnings("unused")
    public static int queryUserIdBySession(String sessionToken)
            throws SessionInvalidException, SessionExpiredException, SessionRevokedException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserIdBySessionSt = con.prepareStatement(querySession)) {
            queryUserIdBySessionSt.setString(1, sessionToken);
            try (ResultSet rs = queryUserIdBySessionSt.executeQuery()) {
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

    /* Create an new account, have to manually set user_name and user_password after the account has created. */
    private static final String insertAccount = "insert into account " +
            "(user_email, user_name, user_password, contact_list, group_list, request_list, offline_message_list, last_session) " +
            "values (?, 'New User', 'Password Placeholder', '[]', '[]', '[]', '[]', '00000000-0000-0000-0000-000000000000')";
    private static final String insertAccountDetail = "insert into account_detail " +
            "(user_id, user_avatar_link) " +
            "values (?, ?)";
    private static final String queryLastInserted = "select last_insert_id()";
    private static final String insertReverseSearchRecord = "insert into email_to_user_id " +
            "(user_email, user_id) values (?, ?)";
    public static int newAccount(String userEmail) throws DataAccessException, UserEmailExistsException {
        if (hasUserWithEmail(userEmail)) { throw new UserEmailExistsException(); }

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement insertAccountSt = con.prepareStatement(insertAccount);
             PreparedStatement insertAccountDetailSt = con.prepareStatement(insertAccountDetail);
             PreparedStatement queryLastInsertedSt = con.prepareStatement(queryLastInserted);
             PreparedStatement insertReverseSearchRecordSt = con.prepareStatement(insertReverseSearchRecord)) {
            insertAccountSt.setString(1, userEmail);
            insertAccountSt.executeUpdate();
            try (ResultSet rs = queryLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    insertAccountDetailSt.setInt(1, userId);
                    insertAccountDetailSt.setString(2, defaultAvatarLink);
                    insertAccountDetailSt.executeUpdate();
                    insertReverseSearchRecordSt.setString(1, userEmail);
                    insertReverseSearchRecordSt.setInt(2, userId);
                    insertReverseSearchRecordSt.executeUpdate();
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

    /* Create a new group, and automatically add the creator to this group as administrator after the group has created. */
    private static final String insertGroup = "insert into group_info " +
            "(group_name, group_host_id, member_list, group_avatar_link) " +
            "values ('New Group', ?, '[]', ?)";
    public static int newGroup(int hostId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement insertGroupSt = con.prepareStatement(insertGroup);
             PreparedStatement queryLastInsertedSt = con.prepareStatement(queryLastInserted)) {
            insertGroupSt.setInt(1, hostId);
            insertGroupSt.setString(2, defaultAvatarLink);
            insertGroupSt.executeUpdate();
            try (ResultSet rs = queryLastInsertedSt.executeQuery()) {
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

    private static final String updateUserName = "update account set user_name = ? where user_id = ?";
    public static void modifyUserName(int userId, String newName) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserNameSt = con.prepareStatement(updateUserName)) {
            updateUserNameSt.setString(1, newName);
            updateUserNameSt.setInt(2, userId);
            updateUserNameSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserName(int userId, String newName)");
        }
    }

    private static final String updateUserPassword = "update account set user_password = ? where user_id = ?";
    public static void modifyUserPassword(int userId, String newPassword) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserPasswordSt = con.prepareStatement(updateUserPassword)) {
            updateUserPasswordSt.setString(1, newPassword);
            updateUserPasswordSt.setInt(2, userId);
            updateUserPasswordSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserPassword(int userId, String newPassword)");
        }
    }

    private static final String updateUserGender = "update account_detail set user_gender = ? where user_id = ?";
    public static void modifyUserGender(int userId, int newGender) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserGenderSt = con.prepareStatement(updateUserGender)) {
            updateUserGenderSt.setInt(1, newGender);
            updateUserGenderSt.setInt(2, userId);
            updateUserGenderSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserGender(int userId, int newGender)");
        }
    }

    private static final String updateUserAvatarLink = "update account_detail set user_avatar_link = ? where user_id = ?";
    public static void modifyUserAvatarLink(int userId, String newAvatarLink) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserAvatarLinkSt = con.prepareStatement(updateUserAvatarLink)) {
            updateUserAvatarLinkSt.setString(1, newAvatarLink);
            updateUserAvatarLinkSt.setInt(2, userId);
            updateUserAvatarLinkSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserAvatarLink(int userId, String newAvatarLink)");
        }
    }

    private static final String updateUserInfo = "update account_detail set user_info = ? where user_id = ?";
    public static void modifyUserInfo(int userId, String newUserInfo) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserInfoSt = con.prepareStatement(updateUserInfo)) {
            updateUserInfoSt.setString(1, newUserInfo);
            updateUserInfoSt.setInt(2, userId);
            updateUserInfoSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserInfo(int userId, String newUserInfo)");
        }
    }

    private static final String updateUserLocation = "update account_detail set user_location = ? where user_id = ?";
    public static void modifyUserLocation(int userId, String newUserLocation) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateUserLocationSt = con.prepareStatement(updateUserLocation)) {
            updateUserLocationSt.setString(1, newUserLocation);
            updateUserLocationSt.setInt(2, userId);
            updateUserLocationSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyUserLocation(int userId, String newUserLocation)");
        }
    }

    private static final String updateGroupName = "update group_info set group_name = ? where group_id = ?";
    public static void modifyGroupName(int groupId, String newGroupName) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateGroupNameSt = con.prepareStatement(updateGroupName)) {
            updateGroupNameSt.setString(1, newGroupName);
            updateGroupNameSt.setInt(2, groupId);
            updateGroupNameSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyGroupName(int groupId, String newGroupName)");
        }
    }

    private static final String updateGroupInfo = "update group_info set group_info = ? where group_id = ?";
    public static void modifyGroupInfo(int groupId, String newGroupInfo) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateGroupInfoSt = con.prepareStatement(updateGroupInfo)) {
            updateGroupInfoSt.setString(1, newGroupInfo);
            updateGroupInfoSt.setInt(2, groupId);
            updateGroupInfoSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyGroupInfo(int groupId, String newGroupInfo)");
        }
    }

    private static final String updateGroupAvatarLink = "update group_info set group_avatar_link = ? where group_id = ?";
    public static void modifyGroupAvatarLink(int groupId, String newAvatarLink) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateGroupAvatarLinkSt = con.prepareStatement(updateGroupAvatarLink)) {
            updateGroupAvatarLinkSt.setString(1, newAvatarLink);
            updateGroupAvatarLinkSt.setInt(2, groupId);
            updateGroupAvatarLinkSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed modifyGroupAvatar(int groupId, String newAvatarLink)");
        }
    }

    private static final String updateLoginTime = "update account set last_login = ? where user_id = ?";
    public static void updateLoginRecord(int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateLoginTimeSt = con.prepareStatement(updateLoginTime)) {
            updateLoginTimeSt.setTimestamp(1, Timestamp.from(Instant.now()));
            updateLoginTimeSt.setInt(2, userId);
            updateLoginTimeSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed updateLoginRecord(int userId)");
        }
    }

    private static final String updateLogoutTime = "update account set last_logout = ? where user_id = ?";
    public static void updateLogoutRecord(int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement updateLogoutTimeSt = con.prepareStatement(updateLogoutTime)) {
            updateLogoutTimeSt.setTimestamp(1, Timestamp.from(Instant.now()));
            updateLogoutTimeSt.setInt(2, userId);
            updateLogoutTimeSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed updateLogoutRecord(int userId)");
        }
    }

    private static final String queryUserLoginRecords = "select last_login, last_logout from account where user_id = ?";
    public static boolean isOnline(int userId) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserLoginRecordsSt = con.prepareStatement(queryUserLoginRecords)) {
            queryUserLoginRecordsSt.setInt(1, userId);
            try (ResultSet rs = queryUserLoginRecordsSt.executeQuery()) {
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

    private static final String queryUser = "select " +
            "p1.user_id, p1.user_email, p1.user_name, p1.user_password, p1.contact_list, p1.group_list, p1.request_list, p1.last_session, " +
            "p2.user_gender, p2.user_avatar_link, p2.user_info, p2.user_location " +
            "from account p1 inner join " +
            "(select user_id, user_gender, user_avatar_link, user_info, user_location from account_detail) p2 " +
            "on p1.user_id = p2.user_id where p1.user_id = ?";
    public static UserInfo getUser(int userId) throws UserNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserSt = con.prepareStatement(queryUser)) {
            queryUserSt.setInt(1, userId);
            try (ResultSet rs = queryUserSt.executeQuery()) {
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
                            .userSession(rs.getString(8))
                            .userGender(rs.getInt(9))
                            .userAvatarLink(rs.getString(10))
                            .userInfo(rs.getString(11))
                            .userLocation(rs.getString(12)).build();
                } else {
                    throw new UserNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getUser(int userId)");
        }
    }

    public static ContactInfo getContact(int contactId) throws UserNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserSt = con.prepareStatement(queryUser)) {
            queryUserSt.setInt(1, contactId);
            try (ResultSet rs = queryUserSt.executeQuery()) {
                if (rs.next()) {
                    return ContactInfo.builder()
                            .contactId(rs.getInt(1))
                            .contactEmail(rs.getString(2))
                            .contactName(rs.getString(3))
                            .contactGender(rs.getInt(9))
                            .contactAvatarLink(rs.getString(10))
                            .contactInfo(rs.getString(11))
                            .contactLocation(rs.getString(12)).build();
                } else {
                    throw new UserNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getContact(int contactId)");
        }
    }

    private static final String queryGroup = "select " +
            "group_id, group_name, group_host_id, member_list, group_info, group_avatar_link from group_info where group_id = ?";
    public static GroupInfo getGroup(int groupId) throws GroupNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryGroupSt = con.prepareStatement(queryGroup)) {
            queryGroupSt.setInt(1, groupId);
            try (ResultSet rs = queryGroupSt.executeQuery()) {
                if (rs.next()) {
                    return GroupInfo.builder()
                            .groupId(rs.getInt(1))
                            .groupName(rs.getString(2))
                            .groupHostId(rs.getInt(3))
                            .memberList(JsonObject.create(rs.getString(4)).getList()
                                    .stream().map(JsonObject::getInt).collect(Collectors.toList()))
                            .groupInfo(rs.getString(5))
                            .groupAvatarLink(rs.getString(6)).build();
                } else {
                    throw new GroupNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getGroup(int groupId)");
        }
    }

    private static final String queryRequest = "select " +
            "request_id, request_status, request_type, request_metadata from request where request_id = ?";
    public static RequestInfo getRequest(int requestId) throws RequestNotExistsException, DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryRequestSt = con.prepareStatement(queryRequest)) {
            queryRequestSt.setInt(1, requestId);
            try (ResultSet rs = queryRequestSt.executeQuery()) {
                if (rs.next()) {
                    return RequestInfo.builder()
                            .requestId(rs.getInt(1))
                            .requestStatus(rs.getString(2))
                            .requestType(rs.getString(3))
                            .requestMetadata(rs.getString(4)).build();
                } else {
                    throw new RequestNotExistsException();
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getRequest(int requestId)");
        }
    }

    private static final String checkFriendship = "select user_id from account " +
            "where user_id = ? and json_contains(contact_list->'$', ?, '$')";
    public static boolean isFriend(int userId1, int userId2) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement checkFriendshipSt = con.prepareStatement(checkFriendship)) {
            checkFriendshipSt.setInt(1, userId1);
            checkFriendshipSt.setString(2, String.valueOf(userId2));
            try (ResultSet rs = checkFriendshipSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed isFriend(int userId1, int userId2)");
        }
    }

    private static final String checkMembership = "select user_id from account " +
            "where user_id = ? and json_contains(group_list->'$', ?, '$')";
    public static boolean isMember(int groupId, int userId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement checkMembershipSt = con.prepareStatement(checkMembership)) {
            checkMembershipSt.setInt(1, userId);
            checkMembershipSt.setString(2, String.valueOf(groupId));
            try (ResultSet rs = checkMembershipSt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed isMember(int groupId, int userId)");
        }
    }

    private static final String insertRequest = "insert into request " +
            "(request_status, request_type, request_metadata) " +
            "values (?, ?, ?)";
    private static final String appendUserRequestList = "update account " +
            "set request_list = json_array_append(request_list, '$', ?) where user_id = ?";
    public static int newContactRequest(int userId, int newContactId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement insertRequestSt = con.prepareStatement(insertRequest);
             PreparedStatement queryLastInsertedSt = con.prepareStatement(queryLastInserted);
             PreparedStatement appendUserRequestListSt = con.prepareStatement(appendUserRequestList)) {
            JsonObject metadata = new JsonObject(new LinkedHashMap<>());
            metadata.put(RequestConstant.REQUEST_CONTACT_ADD_SENDER, new JsonObject(userId));
            metadata.put(RequestConstant.REQUEST_CONTACT_ADD_RECEIVER, new JsonObject(newContactId));
            insertRequestSt.setString(1, EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
            insertRequestSt.setString(2, RequestConstant.REQUEST_CONTACT_ADD);
            insertRequestSt.setString(3, metadata.toString());
            insertRequestSt.executeUpdate();
            try (ResultSet rs = queryLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    appendUserRequestListSt.setInt(1, requestId);
                    appendUserRequestListSt.setInt(2, userId);
                    appendUserRequestListSt.executeUpdate();
                    appendUserRequestListSt.setInt(1, requestId);
                    appendUserRequestListSt.setInt(2, newContactId);
                    appendUserRequestListSt.executeUpdate();
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
    public static void newContactRevoke(int requestId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_REVOKED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContactRevoke(int requestId)");
        }
    }

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
             PreparedStatement insertRequestSt = con.prepareStatement(insertRequest);
             PreparedStatement queryLastInsertedSt = con.prepareStatement(queryLastInserted);
             PreparedStatement appendUserRequestListSt = con.prepareStatement(appendUserRequestList)) {
            JsonObject metadata = new JsonObject(new LinkedHashMap<>());
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_SENDER, new JsonObject(requester));
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_RECEIVER, new JsonObject(groupHostId));
            metadata.put(RequestConstant.REQUEST_GROUP_ADD_GROUP_ID, new JsonObject(groupId));
            insertRequestSt.setString(1, EnumRequestStatus.REQUEST_STATUS_PENDING.toString());
            insertRequestSt.setString(2, RequestConstant.REQUEST_GROUP_ADD);
            insertRequestSt.setString(3, metadata.toString());
            insertRequestSt.executeUpdate();
            try (ResultSet rs = queryLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    appendUserRequestListSt.setInt(1, requestId);
                    appendUserRequestListSt.setInt(2, requester);
                    appendUserRequestListSt.executeUpdate();
                    appendUserRequestListSt.setInt(1, requestId);
                    appendUserRequestListSt.setInt(2, groupHostId);
                    appendUserRequestListSt.executeUpdate();
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

    public static void newMemberRevoke(int requestId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement setRequestStatusSt = con.prepareStatement(setRequestStatus)) {
            setRequestStatusSt.setString(1, EnumRequestStatus.REQUEST_STATUS_REVOKED.toString());
            setRequestStatusSt.setInt(2, requestId);
            setRequestStatusSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMemberRevoke(int requestId)");
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

    private static final String appendUserContactList = "update account " +
            "set contact_list = json_array_append(contact_list, '$', ?) where user_id = ?";
    public static void newContact(int requesterId, int newContactId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement appendUserContactListSt = con.prepareStatement(appendUserContactList)) {
            appendUserContactListSt.setInt(1, requesterId);
            appendUserContactListSt.setInt(2, newContactId);
            appendUserContactListSt.executeUpdate();
            appendUserContactListSt.setInt(1, newContactId);
            appendUserContactListSt.setInt(2, requesterId);
            appendUserContactListSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newContact(int requesterId, int newContactId)");
        }
    }

    private static final String appendUserGroupList = "update account " +
            "set group_list = json_array_append(group_list, '$', ?) where user_id = ?";
    private static final String appendGroupMemberList = "update group_info " +
            "set member_list = json_array_append(member_list, '$', ?) where group_id = ?";
    public static void newMember(int groupId, int requesterId) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement appendUserGroupListSt = con.prepareStatement(appendUserGroupList);
             PreparedStatement appendGroupMemberListSt = con.prepareStatement(appendGroupMemberList)) {
            appendUserGroupListSt.setInt(1, groupId);
            appendUserGroupListSt.setInt(2, requesterId);
            appendUserGroupListSt.executeUpdate();
            appendGroupMemberListSt.setInt(1, requesterId);
            appendGroupMemberListSt.setInt(2, groupId);
            appendGroupMemberListSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newMember(int groupId, int requesterId)");
        }
    }

    private static final String insertMessage = "insert into message (receiver_id, message_type, message_status, message_content) " +
            "values (?, ?, ?, ?)";
    private static final String appendUserMessageList = "update account " +
            "set offline_message_list = json_array_append(offline_message_list, '$', ?) where user_id = ?";
    public static void pushOfflineMessage(int userId, String content, EnumMessageType messageType) throws DataAccessException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement insertMessageSt = con.prepareStatement(insertMessage);
             PreparedStatement queryLastInsertedSt = con.prepareStatement(queryLastInserted);
             PreparedStatement appendUserMessageListSt = con.prepareStatement(appendUserMessageList)) {
            insertMessageSt.setInt(1, userId);
            insertMessageSt.setString(2, messageType.toString());
            insertMessageSt.setString(3, EnumMessageStatus.MESSAGE_STATUS_PENDING.toString());
            insertMessageSt.setString(4, content);
            insertMessageSt.executeUpdate();
            try (ResultSet rs = queryLastInsertedSt.executeQuery()) {
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    appendUserMessageListSt.setInt(1, messageId);
                    appendUserMessageListSt.setInt(2, userId);
                    appendUserMessageListSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Push New Offline Messages!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed pushOfflineMessage(int userId, String content, EnumMessageType messageType)");
        }
    }

    private static final String queryUserMessageList = "select offline_message_list from account where user_id = ?";
    private static final String queryMessage = "select " +
            "message_id, receiver_id, message_type, message_status, message_content from message where message_id = ?";
    private static final String setMessageStatus = "update message set message_status = ? where message_id = ?";
    private static final String clearUserMessageList = "update account set offline_message_list = '[]' where user_id = ?";
    public static List<JsonObject> popOfflineMessageAsList(int userId) throws DataAccessException {
        List<JsonObject> result = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserMessageListSt = con.prepareStatement(queryUserMessageList);
             PreparedStatement queryMessageSt = con.prepareStatement(queryMessage);
             PreparedStatement setMessageStatusSt = con.prepareStatement(setMessageStatus);
             PreparedStatement clearUserMessageListSt = con.prepareStatement(clearUserMessageList)) {
            queryUserMessageListSt.setInt(1, userId);
            try (ResultSet rs = queryUserMessageListSt.executeQuery()) {
                if (rs.next()) {
                    List<Integer> messages = JsonObject.create(rs.getString(1)).getList()
                            .stream().map(JsonObject::getInt).collect(Collectors.toList());

                    for (Integer i : messages) {
                        int textId = i;
                        queryMessageSt.setInt(1, i);
                        try (ResultSet mrs = queryMessageSt.executeQuery()) {
                            if (mrs.next()) {
                                JsonObject messageWithType = new JsonObject(new LinkedHashMap<>());
                                messageWithType.put("message_type", new JsonObject(mrs.getString(3)));
                                messageWithType.put("content", JsonObject.create(mrs.getString(5)));
                                result.add(messageWithType);
                                setMessageStatusSt.setString(1, EnumMessageStatus.MESSAGE_STATUS_POPPED.toString());
                                setMessageStatusSt.setInt(2, textId);
                                setMessageStatusSt.executeUpdate();
                            } else {
                                throw new DataAccessException("Unexpected Error - Pop Offline Messages!");
                            }
                        }
                    }

                    clearUserMessageListSt.setInt(1, userId);
                    clearUserMessageListSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Pop Offline Messages!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed popOfflineMessageAsList(int userId)");
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
    private static final String insertPasscode = "insert into passcode " +
            "(passcode, passcode_metadata, passcode_create_datetime, passcode_expire_datetime, passcode_status) " +
            "values (?, ?, ?, ?, ?)";
    public static String newPasscode(Map<String, String> kvMap) {
        try (Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement insertPasscodeSt = con.prepareStatement(insertPasscode)) {
            String passcode = generateNewPasscode();
            JsonObject metadata = new JsonObject(new LinkedHashMap<>());
            kvMap.forEach((key, value) -> metadata.put(key, new JsonObject(value)));
            insertPasscodeSt.setString(1, passcode);
            insertPasscodeSt.setString(2, metadata.toString());
            insertPasscodeSt.setTimestamp(3, Timestamp.from(Instant.now()));
            insertPasscodeSt.setTimestamp(4, Timestamp.from(Instant.now().plus(PASSCODE_EXPIRE_DURATION)));
            insertPasscodeSt.setString(5, EnumPasscodeStatus.PASSCODE_STATUS_PENDING.toString());
            insertPasscodeSt.executeUpdate();
            return passcode;
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newPasscode(Map<String, String> kvMap)");
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

    private static final String queryPasscode = "select " +
            "passcode_metadata, passcode_create_datetime, passcode_expire_datetime, passcode_status " +
            "from passcode where passcode = ?";
    @SuppressWarnings("unused")
    public static void checkPasscode(String passcode, Map<String, String> kvMap) throws PasscodeException {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryPasscodeSt = con.prepareStatement(queryPasscode)) {
            queryPasscodeSt.setString(1, passcode);
            try (ResultSet rs = queryPasscodeSt.executeQuery()) {
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
    private static final String queryUserLastSession = "select last_session from account where user_id = ?";
    private static final String insertSession = "insert into session_storage " +
            "(session_id, user_id, session_create_datetime, session_expire_datetime, session_status) " +
            "values (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
            "session_id = ?, user_id = ?, session_create_datetime =?, session_expire_datetime = ?, session_status = ?";
    private static final String updateUserLastSession = "update account set last_session = ? where user_id = ?";
    public static void updateSession(int userId, String session) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserLastSessionSt = con.prepareStatement(queryUserLastSession);
             PreparedStatement revokePreviousSessionSt = con.prepareStatement(setSessionStatus);
             PreparedStatement insertSessionSt = con.prepareStatement(insertSession);
             PreparedStatement updateUserLastSessionSt = con.prepareStatement(updateUserLastSession)) {
            queryUserLastSessionSt.setInt(1, userId);
            try (ResultSet rs = queryUserLastSessionSt.executeQuery()) {
                if (rs.next()) {
                    String lastSession = rs.getString(1);
                    if (lastSession != null) {
                        revokePreviousSessionSt.setString(1, EnumSessionStatus.SESSION_STATUS_REVOKED.toString());
                        revokePreviousSessionSt.setString(2, lastSession);
                        revokePreviousSessionSt.executeUpdate();
                    }
                    insertSessionSt.setString(1, session);
                    insertSessionSt.setString(6, session);
                    insertSessionSt.setInt(2, userId);
                    insertSessionSt.setInt(7, userId);
                    insertSessionSt.setTimestamp(3, Timestamp.from(Instant.now()));
                    insertSessionSt.setTimestamp(8, Timestamp.from(Instant.now()));
                    insertSessionSt.setTimestamp(4, Timestamp.from(Instant.now().plus(SESSION_EXPIRE_DURATION)));
                    insertSessionSt.setTimestamp(9, Timestamp.from(Instant.now().plus(SESSION_EXPIRE_DURATION)));
                    insertSessionSt.setString(5, EnumSessionStatus.SESSION_STATUS_VALID.toString());
                    insertSessionSt.setString(10, EnumSessionStatus.SESSION_STATUS_VALID.toString());
                    insertSessionSt.executeUpdate();
                    updateUserLastSessionSt.setString(1, session);
                    updateUserLastSessionSt.setInt(2, userId);
                    updateUserLastSessionSt.executeUpdate();
                } else {
                    throw new DataAccessException("Unexpected Error - Revoke Last Session!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed updateSession(int userId, String session)");
        }
    }

    // Currently do not check expiration.
    public static boolean checkSession(int userId, String sessionToken) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserLastSessionSt = con.prepareStatement(queryUserLastSession)) {
            queryUserLastSessionSt.setInt(1, userId);
            try (ResultSet rs = queryUserLastSessionSt.executeQuery()) {
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

    public static String queryLastSessionById(int userId) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryUserLastSessionSt = con.prepareStatement(queryUserLastSession)) {
            queryUserLastSessionSt.setInt(1, userId);
            try (ResultSet rs = queryUserLastSessionSt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new DataAccessException("Unexpected Error - Find Last Session By User ID!");
                }
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed queryLastSessionById(int userId)");
        }
    }

    private static final Duration FILE_EXPIRE_DURATION = Duration.ofDays(365);
    private static final String insertFileArchive = "insert into file_archive " +
            "(first_selector, second_selector, file_name, file_link, file_uploader, file_upload_time, file_expire_time, file_size) " +
            "values (?, ?, ?, ?, ?, ?, ?, ?)";
    public static void newFileDescriptor(int firstSelector, int secondSelector, String fileName, String fileLink, int fileUploader, int fileSize) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement insertFileArchiveSt = con.prepareStatement(insertFileArchive)) {
            insertFileArchiveSt.setInt(1, firstSelector);
            insertFileArchiveSt.setInt(2, secondSelector);
            insertFileArchiveSt.setString(3, fileName);
            insertFileArchiveSt.setString(4, fileLink);
            insertFileArchiveSt.setInt(5, fileUploader);
            insertFileArchiveSt.setTimestamp(6, Timestamp.from(Instant.now()));
            insertFileArchiveSt.setTimestamp(7, Timestamp.from(Instant.now().plus(FILE_EXPIRE_DURATION)));
            insertFileArchiveSt.setInt(8, fileSize);
            insertFileArchiveSt.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed newFileDescriptor(int firstSelector, int secondSelector, String fileName, String fileLink, int fileUploader, int fileSize)");
        }
    }

    private static final String queryFileList = "select " +
            "file_id, first_selector, second_selector, file_name, file_link, file_uploader, file_upload_time, file_expire_time, file_size, file_downloads " +
            "from file_archive where first_selector = ? and second_selector = ?";
    public static List<FileInfo> getFileList(int firstSelector, int secondSelector) {
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement queryFileListSt = con.prepareStatement(queryFileList)) {
            queryFileListSt.setInt(1, firstSelector);
            queryFileListSt.setInt(2, secondSelector);
            try (ResultSet rs = queryFileListSt.executeQuery()) {
                List<FileInfo> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(FileInfo.builder()
                            .fileId(rs.getInt(1))
                            .firstSelector(rs.getInt(2))
                            .secondSelector(rs.getInt(3))
                            .fileName(rs.getString(4))
                            .fileLink(rs.getString(5))
                            .fileUploader(rs.getInt(6))
                            .fileUploadTime(rs.getTimestamp(7).toInstant())
                            .fileExpireTime(rs.getTimestamp(8).toInstant())
                            .fileSize(rs.getInt(9))
                            .fileDownloads(rs.getInt(10)).build());
                }
                return result;
            }
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed getFileList(int firstSelector, int secondSelector)");
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
    private static final String deleteFileArchive = "delete from file_archive";
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
            deleteSt.executeUpdate(deleteFileArchive);
        } catch (SQLException e) {
            logger.info(e.getMessage());
            throw new DataAccessException("MySQL Execution Failed resetDatabase()");
        }
    }
}
