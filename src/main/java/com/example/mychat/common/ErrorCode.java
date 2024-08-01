package com.example.mychat.common;

public enum ErrorCode {

    PARAM_ERROR(401, "参数错误"),

    LOGIN_ERROR(402, "登录信息错误"),

    SYSTEM_ERROR(500, "系统错误");

    int code;

    String message;

    ErrorCode(int code, String message) {
        this.code = code; // 使用 this 关键字初始化字段
        this.message = message; // 使用 this 关键字初始化字段
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
