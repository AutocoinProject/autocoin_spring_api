package com.autocoin.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Method Not Allowed"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "Invalid Type Value"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "Access is Denied"),
    
    // User
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "U001", "Email is Duplicated"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "User Not Found"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "U003", "Login Failed: Invalid Credentials"),
    
    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Post Not Found"),
    NOT_POST_OWNER(HttpStatus.FORBIDDEN, "P002", "Not the Post Owner"),
    
    // File
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "File Upload Failed"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "File Not Found"),
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "File Download Failed");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
