package com.smalltalknow.service.controller.patterns;

import com.smalltalknow.service.controller.patterns.exceptions.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("SpellCheckingInspection")
public class PatternCheckerTest {

    @DataProvider(name = "data_test_check_user_email")
    public Object[][] testCheckUserEmailDataProvider() {
        return new Object[][] {
                { "a@a.a" },
                { "a.a@a.a" },
                { "a.a.a@a.a" },
                { "a@a.a.a" },
                { "a.a@a.a.a" },
                { "a.a.a@a.a.a" },
        };
    }

    @Test(dataProvider = "data_test_check_user_email")
    public void testCheckUserEmail(String input) throws InvalidUserEmailException {
        PatternChecker.checkUserEmail(input);
    }

    @DataProvider(name = "data_test_check_user_email_invalid")
    public Object[][] testCheckUserEmailInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "a" },
                { "a.a" },
                { "@a" },
                { "a@a" },
                { "@.a" },
                { "a@.a" },
                { "a.a@a" },
                { "a.a@.a" },
        };
    }

    @Test(dataProvider = "data_test_check_user_email_invalid", expectedExceptions = InvalidUserEmailException.class)
    public void testCheckUserEmailInvalid(String input) throws InvalidUserEmailException {
        PatternChecker.checkUserEmail(input);
    }

    @DataProvider(name = "data_test_check_user_name")
    public Object[][] testCheckUserNameDataProvider() {
        return new Object[][] {
                { "11" },
                { "1111111111111111" },
                { "1111aaaa1111AAAA" },
                { "测试" },
                { "测试1111aaAA" },
                { "测试测试测试测试测试测试测试测试" },
        };
    }

    @Test(dataProvider = "data_test_check_user_name")
    public void testCheckUserName(String input) throws InvalidUserNameException {
        PatternChecker.checkUserName(input);
    }

    @DataProvider(name = "data_test_check_user_name_invalid")
    public Object[][] testCheckUserNameInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "1" },
                { "测" },
                { "11111111111111111" },
                { "测试测试测试测试测试测试测试测试测" },
        };
    }

    @Test(dataProvider = "data_test_check_user_name_invalid", expectedExceptions = InvalidUserNameException.class)
    public void testCheckUserNameInvalid(String input) throws InvalidUserNameException {
        PatternChecker.checkUserName(input);
    }

    @DataProvider(name = "data_test_check_group_name")
    public Object[][] testCheckGroupNameDataProvider() {
        return new Object[][] {
                { "11" },
                { "1111111111111111" },
                { "1111aaaa1111AAAA" },
                { "测试" },
                { "测试1111aaAA" },
                { "测试测试测试测试测试测试测试测试" },
        };
    }

    @Test(dataProvider = "data_test_check_group_name")
    public void testCheckGroupName(String input) throws InvalidGroupNameException {
        PatternChecker.checkGroupName(input);
    }

    @DataProvider(name = "data_test_check_group_name_invalid")
    public Object[][] testCheckGroupNameInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "1" },
                { "测" },
                { "11111111111111111" },
                { "测试测试测试测试测试测试测试测试测" },
        };
    }

    @Test(dataProvider = "data_test_check_group_name_invalid", expectedExceptions = InvalidGroupNameException.class)
    public void testCheckGroupNameInvalid(String input) throws InvalidGroupNameException {
        PatternChecker.checkGroupName(input);
    }

    @DataProvider(name = "data_test_check_user_password")
    public Object[][] testCheckUserPasswordDataProvider() {
        return new Object[][] {
                { "aaa111" },
                { "AaA111" },
                { "AaAaA1" },
                { "AaAaAaAaAaAa1111" },
                { "111111AAAA" },
                { "111111111111111a" },
                { "111111111111111A" },
        };
    }

    @Test(dataProvider = "data_test_check_user_password")
    public void testCheckUserPassword(String input) throws InvalidUserPasswordException {
        PatternChecker.checkUserPassword(input);
    }

    @DataProvider(name = "data_test_check_user_password_invalid")
    public Object[][] testCheckUserPasswordInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "111aa" },
                { "111111" },
                { "AAAaaa" },
                { "111111111111111Aa" },
                { "测试AaAa" },
        };
    }

    @Test(dataProvider = "data_test_check_user_password_invalid", expectedExceptions = InvalidUserPasswordException.class)
    public void testCheckUserPasswordInvalid(String input) throws InvalidUserPasswordException {
        PatternChecker.checkUserPassword(input);
    }

    @DataProvider(name = "data_test_check_session_token")
    public Object[][] testCheckSessionTokenDataProvider() {
        return new Object[][] {
                { "123e4567-e89b-12d3-a456-556642440000" },
                { "00000000-0000-0000-0000-000000000000" },
                { "ABCDEFEF-ABCD-abcd-ABCD-ABcdEF121212" },
        };
    }

    @Test(dataProvider = "data_test_check_session_token")
    public void testCheckSessionToken(String input) throws InvalidSessionException {
        PatternChecker.checkSessionToken(input);
    }

    @DataProvider(name = "data_test_check_session_token_invalid")
    public Object[][] testCheckSessionTokenInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "1234567-e89b-12d3-a456-556642440000" },
                { "ABCDEFEF-ABD-abcd-ABCD-ABcdEF121212" },
                { "ABCDEFEF-ABCD-abd-ABCD-ABcdEF121212" },
                { "ABCDEFEF-ABCD-abcd-ABD-ABcdEF121212" },
                { "ABCDEFEF-ABCD-abcd-ABCD-ABdEF121212" },
                { "1234567-e89b-12d3-a456-556642440000A" },
                { "ABCDEFEF-ABD-abcd-ABCD-ABcdEF121212A" },
                { "ABCDEFEF-ABCD-abd-ABCD-ABcdEF121212A" },
                { "ABCDEFEF-ABCD-abcd-ABD-ABcdEF121212A" },
        };
    }

    @Test(dataProvider = "data_test_check_session_token_invalid", expectedExceptions = InvalidSessionException.class)
    public void testCheckSessionTokenInvalid(String input) throws InvalidSessionException {
        PatternChecker.checkSessionToken(input);
    }

    @DataProvider(name = "data_test_check_passcode")
    public Object[][] testCheckPasscodeDataProvider() {
        return new Object[][] {
                { "111111" },
                { "11AAaa" },
                { "AbCDEf" },
                { "A1b2C3" },
        };
    }

    @Test(dataProvider = "data_test_check_passcode")
    public void testCheckPasscode(String input) throws InvalidPasscodeException {
        PatternChecker.checkPasscode(input);
    }

    @DataProvider(name = "data_test_check_passcode_invalid")
    public Object[][] testCheckPasscodeInvalidDataProvider() {
        return new Object[][] {
                { "" },
                { "1" },
                { "11111" },
                { "1111111" },
                { "测试测试测试" },
        };
    }

    @Test(dataProvider = "data_test_check_passcode_invalid", expectedExceptions = InvalidPasscodeException.class)
    public void testCheckPasscodeInvalid(String input) throws InvalidPasscodeException {
        PatternChecker.checkPasscode(input);
    }
}
