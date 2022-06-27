package com.ebabu.event365live.host.entities;

import java.util.List;

public class SubCategoryResponse {

    private boolean success;
    private String message;
    private int code;
    private List<SubCategory> categories;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public List<SubCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<SubCategory> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "SubCategoryResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", code=" + code +
                ", categories=" + categories +
                '}';
    }
}
