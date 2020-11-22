package com.smalltalknow.service.controller.patterns;

import com.smalltalknow.service.controller.patterns.exceptions.*;

import java.util.regex.Pattern;

public final class PatternChecker {
    private static final Pattern PATTERN_USER_EMAIL = Pattern.compile(
            "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))$");

    /* Length: 2 - 16 */
    /* Contains: Unicode Characters */
    private static final Pattern PATTERN_USER_NAME = Pattern.compile(
            "[\\p{all}/]{2,16}$");

    /* Length: 2 - 16 */
    /* Contains: Unicode Characters */
    private static final Pattern PATTERN_GROUP_NAME = Pattern.compile(
            "[\\p{all}/]{2,16}$");

    /* Length: 6 - 16 */
    /* Contains: Uppercase Letters, Lowercase Letters and numbers */
    /* Must contain both letters and numbers */
    private static final Pattern PATTERN_USER_PASSWORD = Pattern.compile(
            "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");

    private static final Pattern PATTERN_SESSION_TOKEN = Pattern.compile(
            "\\b[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-\\b[0-9a-fA-F]{12}\\b$");

    /* Length: 6 */
    /* Contains: Uppercase Letters, Lowercase Letters and numbers */
    private static final Pattern PATTERN_PASSCODE = Pattern.compile(
            "[0-9A-Za-z]{6}$");

    public static void checkUserEmail(String input) throws InvalidUserEmailException {
        if(!PATTERN_USER_EMAIL.matcher(input).matches()) { throw new InvalidUserEmailException(); }
    }

    public static void checkUserName(String input) throws InvalidUserNameException {
        if(!PATTERN_USER_NAME.matcher(input).matches()) { throw new InvalidUserNameException(); }
    }

    public static void checkGroupName(String input) throws InvalidGroupNameException {
        if(!PATTERN_GROUP_NAME.matcher(input).matches()) { throw new InvalidGroupNameException(); }
    }

    public static void checkUserPassword(String input) throws InvalidUserPasswordException {
        if(!PATTERN_USER_PASSWORD.matcher(input).matches()) { throw new InvalidUserPasswordException(); }
    }

    public static void checkSessionToken(String input) throws InvalidSessionException {
        if(!PATTERN_SESSION_TOKEN.matcher(input).matches()) { throw new InvalidSessionException(); }
    }

    public static void checkPasscode(String input) throws InvalidPasscodeException {
        if(!PATTERN_PASSCODE.matcher(input).matches()) { throw new InvalidPasscodeException(); }
    }
}
