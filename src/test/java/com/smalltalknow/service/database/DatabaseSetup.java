package com.smalltalknow.service.database;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DatabaseSetup {

    private final String userEmail1 = "TU1@st.com";
    private final String userEmail2 = "TU2@st.com";
    private final String userEmail3 = "TU3@st.com";
    private final String userEmail4 = "TU4@st.com";
    private final String userEmail5 = "TU5@st.com";
    private final String userEmail6 = "TU6@st.com";
    private final String userEmail7 = "TU7@st.com";
    private final String userEmail8 = "TU8@st.com";
    private final String userEmail9 = "TU9@st.com";
    private final String userEmail10 = "TU10@st.com";
    private final String userName1 = "Test User 1";
    private final String userName2 = "Test User 2";
    private final String userName3 = "Test User 3";
    private final String userName4 = "Test User 4";
    private final String userName5 = "Test User 5";
    private final String userName6 = "Test User 6";
    private final String userName7 = "Test User 7";
    private final String userName8 = "Test User 8";
    private final String userName9 = "Test User 9";
    private final String userName10 = "Test User 10";
    private final String userPassword1 = "test01";
    private final String userPassword2 = "test02";
    private final String userPassword3 = "test03";
    private final String userPassword4 = "test04";
    private final String userPassword5 = "test05";
    private final String userPassword6 = "test06";
    private final String userPassword7 = "test07";
    private final String userPassword8 = "test08";
    private final String userPassword9 = "test09";
    private final String userPassword10 = "test10";
    private int userId1 = 0;
    private int userId2 = 0;
    private int userId3 = 0;
    private int userId4 = 0;
    private int userId5 = 0;
    private int userId6 = 0;
    private int userId7 = 0;
    private int userId8 = 0;
    private int userId9 = 0;
    private int userId10 = 0;
    private final String groupName1 = "Test Group 1";
    private final String groupName2 = "Test Group 2";
    private final String groupName3 = "Test Group 3";
    private int groupId1 = 0;
    private int groupId2 = 0;
    private int groupId3 = 0;
    
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
        userId9 = DatabaseService.newAccount(userEmail9);
        userId10 = DatabaseService.newAccount(userEmail10);
        groupId1 = DatabaseService.newGroup(userId1);
        groupId2 = DatabaseService.newGroup(userId2);
        groupId3 = DatabaseService.newGroup(userId3);
        assertNotEquals(userId1, 0);
        assertNotEquals(userId2, 0);
        assertNotEquals(userId3, 0);
        assertNotEquals(userId4, 0);
        assertNotEquals(userId5, 0);
        assertNotEquals(userId6, 0);
        assertNotEquals(userId7, 0);
        assertNotEquals(userId8, 0);
        assertNotEquals(userId9, 0);
        assertNotEquals(userId10, 0);
        assertNotEquals(groupId1, 0);
        assertNotEquals(groupId2, 0);
        assertNotEquals(groupId3, 0);
        DatabaseService.modifyGroupName(groupId1, groupName1);
        DatabaseService.modifyGroupName(groupId2, groupName2);
        DatabaseService.modifyGroupName(groupId3, groupName3);
        DatabaseService.modifyUserName(userId1, userName1);
        DatabaseService.modifyUserName(userId2, userName2);
        DatabaseService.modifyUserName(userId3, userName3);
        DatabaseService.modifyUserName(userId4, userName4);
        DatabaseService.modifyUserName(userId5, userName5);
        DatabaseService.modifyUserName(userId6, userName6);
        DatabaseService.modifyUserName(userId7, userName7);
        DatabaseService.modifyUserName(userId8, userName8);
        DatabaseService.modifyUserName(userId9, userName9);
        DatabaseService.modifyUserName(userId10, userName10);
        DatabaseService.modifyUserPassword(userId1, userPassword1);
        DatabaseService.modifyUserPassword(userId2, userPassword2);
        DatabaseService.modifyUserPassword(userId3, userPassword3);
        DatabaseService.modifyUserPassword(userId4, userPassword4);
        DatabaseService.modifyUserPassword(userId5, userPassword5);
        DatabaseService.modifyUserPassword(userId6, userPassword6);
        DatabaseService.modifyUserPassword(userId7, userPassword7);
        DatabaseService.modifyUserPassword(userId8, userPassword8);
        DatabaseService.modifyUserPassword(userId9, userPassword9);
        DatabaseService.modifyUserPassword(userId10, userPassword10);
    }

    @AfterSuite
    public void afterSuite() {

    }

    @Test
    public void DummyTest() {

    }
}
