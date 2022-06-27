package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HostDetailResponse {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private HostDetailData data;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("message")
    @Expose
    private String message;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public HostDetailData getData() {
        return data;
    }

    public void setData(HostDetailData data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public class HostDetailData {

        @SerializedName("countryCode")
        @Expose
        private String countryCode;
        @SerializedName("hostMobile")
        @Expose
        private String hostMobile;
        @SerializedName("hostAddress")
        @Expose
        private String hostAddress;
        @SerializedName("websiteUrl")
        @Expose
        private String websiteUrl;
        @SerializedName("otherWebsiteUrl")
        @Expose
        private String otherWebsiteUrl;

        public String getHostMobile() {
            return hostMobile;
        }

        public void setHostMobile(String hostMobile) {
            this.hostMobile = hostMobile;
        }

        public String getHostAddress() {
            return hostAddress;
        }

        public void setHostAddress(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        public String getWebsiteUrl() {
            return websiteUrl;
        }

        public void setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getOtherWebsiteUrl() {
            return otherWebsiteUrl;
        }

        public void setOtherWebsiteUrl(String otherWebsiteUrl) {
            this.otherWebsiteUrl = otherWebsiteUrl;
        }
    }

    @Override
    public String toString() {
        return "HostDetailResponse{" +
                "message='" + message + '\'' +
                ", code=" + code +
                ", success=" + success +
                '}';
    }
}
