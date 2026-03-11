package com.daou.kwpsdk.common.exception;

/**
 * POS 라이브러리 공통 예외 클래스
 */
public class PosException extends Exception {

    private final int errorCode;

    /** 에러 코드 상수 */
    public static final int ERROR_READER_NOT_CONNECTED = 1001;
    public static final int ERROR_READER_TIMEOUT = 1002;
    public static final int ERROR_CARD_READ_FAILED = 1003;
    public static final int ERROR_VAN_CONNECTION_FAILED = 2001;
    public static final int ERROR_VAN_TIMEOUT = 2002;
    public static final int ERROR_VAN_RESPONSE_PARSE = 2003;
    public static final int ERROR_PAYMENT_CANCELLED = 3001;
    public static final int ERROR_INVALID_PARAMETER = 4001;
    public static final int ERROR_UNKNOWN = 9999;

    public PosException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PosException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "PosException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}

