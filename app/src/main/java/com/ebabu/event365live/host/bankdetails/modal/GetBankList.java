package com.ebabu.event365live.host.bankdetails.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetBankList {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Data data;
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


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {
        private List<BankList> bank_details;
        public List<BankList> getBank_details() {
            return bank_details;
        }
        private boolean stripeAccountStatus;
        private boolean accountLinkStatus;

        public void setBank_details(List<BankList> bank_details) {
            this.bank_details = bank_details;
        }

        public boolean isStripeAccountStatus() {
            return stripeAccountStatus;
        }

        public void setStripeAccountStatus(boolean stripeAccountStatus) {
            this.stripeAccountStatus = stripeAccountStatus;
        }

        public boolean isAccountLinkStatus() {
            return accountLinkStatus;
        }

        public void setAccountLinkStatus(boolean accountLinkStatus) {
            this.accountLinkStatus = accountLinkStatus;
        }
    }

    public class BankList {
        private Integer bankIdKey;
        private String AccountNo;
        private String routingNo;
        private String bankName;

        public Integer getId() {
            return bankIdKey;
        }

        public Integer getBankIdKey() {
            return bankIdKey;
        }

        public void setBankIdKey(Integer bankIdKey) {
            this.bankIdKey = bankIdKey;
        }

        public String getAccountNo() {
            return AccountNo;
        }

        public void setAccountNo(String accountNo) {
            AccountNo = accountNo;
        }

        public String getRoutingNo() {
            return routingNo;
        }

        public void setRoutingNo(String routingNo) {
            this.routingNo = routingNo;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }
    }

}
