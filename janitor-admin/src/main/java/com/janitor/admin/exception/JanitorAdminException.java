package com.janitor.admin.exception;

/**
 * ClassName JanitorAdminException
 * Description admin业务异常类
 *
 * @author 曦逆
 * Date 2022/5/31 8:56
 */
public class JanitorAdminException extends RuntimeException {
    private static final long serialVersionUID = -1;
    /**
     * 统一异常码
     */
    private static final Integer UNIFIED_ERROR_CODE = 9999;
    /**
     * 错误码
     */
    private Integer errorCode;

    public JanitorAdminException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public JanitorAdminException(Throwable t, Integer errorCode, String message) {
        super(message, t);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public static JanitorAdminException of(Throwable t) {
        return new JanitorAdminException(t, UNIFIED_ERROR_CODE, null);
    }

    public static JanitorAdminException of(Throwable t, String message) {
        return new JanitorAdminException(t, UNIFIED_ERROR_CODE, message);
    }

    public static JanitorAdminException of(String message) {
        return new JanitorAdminException(null, UNIFIED_ERROR_CODE, message);
    }

    public static JanitorAdminException of(Integer errorCode, String message) {
        return new JanitorAdminException(null, errorCode, message);
    }

    public static JanitorAdminException of(Throwable t, Integer errorCode, String message) {
        return new JanitorAdminException(t, errorCode, message);
    }
}
