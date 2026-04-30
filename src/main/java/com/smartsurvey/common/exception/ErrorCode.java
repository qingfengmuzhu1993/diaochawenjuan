package com.smartsurvey.common.exception;

public enum ErrorCode {
    // 400 Bad Request
    BAD_REQUEST(40000, "请求参数错误"),
    VALIDATION_ERROR(40001, "参数校验失败"),

    // 401 Unauthorized
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    TOKEN_INVALID(40101, "Token无效"),
    LOGIN_FAILED(40102, "用户名或密码错误"),

    // 403 Forbidden
    FORBIDDEN(40300, "无权限访问"),
    ACCOUNT_FROZEN(40301, "账号已被冻结"),

    // 404 Not Found
    NOT_FOUND(40400, "资源不存在"),
    SURVEY_NOT_FOUND(40401, "问卷不存在"),
    USER_NOT_FOUND(40402, "用户不存在"),

    // 409 Conflict
    DUPLICATE_SUBMIT(40900, "请勿重复提交"),
    QUOTA_FULL(40901, "问卷配额已满"),
    SURVEY_CLAIMED(40902, "您已抢过此问卷"),

    // 422 Unprocessable Entity
    INSUFFICIENT_BALANCE(42200, "余额不足"),
    WITHDRAW_MINIMUM(42201, "提现金额不能低于10元"),
    ANSWER_EXPIRED(42203, "答题时间已过期"),

    // 429 Too Many Requests
    RATE_LIMIT(42900, "操作过于频繁，请稍后再试"),
    SMS_LIMIT(42901, "验证码发送过于频繁"),

    // 500 Internal Server Error
    INTERNAL_ERROR(50000, "服务器内部错误"),
    AI_GENERATION_FAILED(50001, "AI生成失败，请重试"),
    PAYMENT_FAILED(50002, "支付处理失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
