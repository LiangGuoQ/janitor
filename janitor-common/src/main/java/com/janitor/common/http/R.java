package com.janitor.common.http;

/**
 * ClassName R
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:22
 */
public class R {
    private int statusCode;
    private String statusMsg;
    private String responseText;

    public R() {
    }

    public static R success(int statusCode, String statusMsg) {
        R r = new R();
        r.setStatusCode(statusCode);
        r.setStatusMsg(statusMsg);
        return r;
    }

    public static R failure(int statusCode, String statusMsg) {
        R r = new R();
        r.setStatusCode(statusCode);
        r.setStatusMsg(statusMsg);
        return r;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMsg() {
        return this.statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getResponseText() {
        return this.responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}
