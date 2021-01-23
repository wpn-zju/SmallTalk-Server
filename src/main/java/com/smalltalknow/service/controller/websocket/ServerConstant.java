package com.smalltalknow.service.controller.websocket;

public final class ServerConstant {
    public static final String PSC_TYPE = "passcode_type";
    public static final String PSC_TYPE_USER_SIGN_UP = "sign_up";
    public static final String PSC_TYPE_USER_RECOVER_PASSWORD = "recover_password";
    public static final String PSC_USER_EMAIL = "user_email";

    public static final String DIR_INVALID_PASSCODE = "/queue/port/invalid_passcode";
    public static final String DIR_INVALID_SESSION = "/queue/port/invalid_session";
    public static final String DIR_INVALID_USER_NAME = "/queue/port/invalid_user_name";
    public static final String DIR_INVALID_USER_EMAIL = "/queue/port/invalid_user_email";
    public static final String DIR_INVALID_USER_PASSWORD = "/queue/port/invalid_user_password";
    public static final String DIR_INVALID_GROUP_NAME = "/queue/port/invalid_group_name";

    public static final String DIR_USER_SIGN_UP_SUCCESS = "/queue/port/user_sign_up_success";
    public static final String DIR_USER_SIGN_UP_FAILED_EMAIL_EXISTS = "/queue/port/user_sign_up_failed_email_exists";
    public static final String DIR_USER_SIGN_UP_FAILED_PASSCODE_INCORRECT = "/queue/port/user_sign_up_failed_passcode_incorrect";
    public static final String DIR_USER_SIGN_UP_PASSCODE_REQUEST_SUCCESS = "/queue/port/user_sign_up_passcode_request_success";
    public static final String DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_SERVER_ERROR = "/queue/port/user_sign_up_passcode_request_failed_server_error";
    public static final String DIR_USER_SIGN_UP_PASSCODE_REQUEST_FAILED_EMAIL_EXISTS = "/queue/port/user_sign_up_passcode_request_failed_email_exists";
    public static final String DIR_USER_RECOVER_PASSWORD_SUCCESS = "/queue/port/user_recover_password_success";
    public static final String DIR_USER_RECOVER_PASSWORD_FAILED_USER_NOT_FOUND = "/queue/port/user_recover_password_failed_user_not_found";
    public static final String DIR_USER_RECOVER_PASSWORD_FAILED_PASSCODE_INCORRECT ="/queue/port/user_recover_password_failed_passcode_incorrect";
    public static final String DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_SUCCESS = "/queue/port/user_recover_passcode_passcode_request_success";
    public static final String DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_SERVER_ERROR = "/queue/port/user_recover_password_passcode_request_failed_server_error";
    public static final String DIR_USER_RECOVER_PASSWORD_PASSCODE_REQUEST_FAILED_USER_NOT_FOUND = "/queue/port/user_recover_password_passcode_request_failed_user_not_found";
    public static final String DIR_USER_SIGN_IN_SUCCESS = "/queue/port/user_sign_in_success";
    public static final String DIR_USER_SIGN_IN_FAILED_USER_NOT_FOUND = "/queue/port/user_sign_in_failed_user_not_found";
    public static final String DIR_USER_SIGN_IN_FAILED_PASSWORD_INCORRECT = "/queue/port/user_sign_in_failed_password_incorrect";
    public static final String DIR_USER_SIGN_OUT_SUCCESS = "/queue/port/user_sign_out_success";
    public static final String DIR_USER_SESSION_INVALID = "/queue/port/user_session_invalid";
    public static final String DIR_USER_SESSION_EXPIRED = "/queue/port/user_session_expired";
    public static final String DIR_USER_SESSION_REVOKED = "/queue/port/user_session_revoked";
    public static final String DIR_USER_SYNC = "/queue/port/user_sync";
    public static final String DIR_CONTACT_SYNC = "/queue/port/contact_sync";
    public static final String DIR_CONTACT_SYNC_FAILED_USER_NOT_FOUND = "/queue/port/contact_sync_failed_user_not_found";
    public static final String DIR_GROUP_SYNC = "/queue/port/group_sync";
    public static final String DIR_GROUP_SYNC_FAILED_GROUP_NOT_FOUND = "/queue/port/group_sync_failed_group_not_found";
    public static final String DIR_REQUEST_SYNC = "/queue/port/request_sync";
    public static final String DIR_REQUEST_SYNC_FAILED_REQUEST_NOT_FOUND = "/queue/port/request_sync_failed_request_not_found";
    public static final String DIR_FILE_LIST_SYNC = "/queue/port/file_list_sync";
    public static final String DIR_NEW_MESSAGE = "/queue/port/new_message";
    public static final String DIR_NEW_GROUP_MESSAGE = "/queue/port/new_group_message";
    public static final String DIR_CONTACT_ADD_REQUEST_SUCCESS = "/queue/port/contact_add_request_success";
    public static final String DIR_CONTACT_ADD_REQUEST_FAILED_ALREADY_CONTACT = "/queue/port/contact_add_request_failed_already_contact";
    public static final String DIR_CONTACT_ADD_REQUEST_FAILED_USER_NOT_FOUND = "/queue/port/contact_add_failed_user_not_found";
    public static final String DIR_GROUP_ADD_REQUEST_SUCCESS = "/queue/port/group_add_request_success";
    public static final String DIR_GROUP_ADD_REQUEST_FAILED_ALREADY_MEMBER = "/queue/port/group_add_request_failed_already_member";
    public static final String DIR_GROUP_ADD_REQUEST_FAILED_GROUP_NOT_FOUND = "/queue/port/group_add_request_failed_group_not_found";
    public static final String DIR_WEBRTC_CALL = "/queue/port/webrtc_call";
    public static final String DIR_REQUEST_NOT_FOUND = "/queue/port/request_not_found";
}
