package com.ebabu.event365live.host.entities;

public class MyResponse {
    private String message;
    private int code;
    private boolean success;

    public MyResponse() {
    }

    public MyResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "MyResponse{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", success=" + success +
                '}';
    }
}
