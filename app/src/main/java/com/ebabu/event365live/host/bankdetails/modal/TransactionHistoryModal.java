package com.ebabu.event365live.host.bankdetails.modal;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionHistoryModal implements Parcelable{
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

    protected TransactionHistoryModal(Parcel in) {
        byte tmpSuccess = in.readByte();
        success = tmpSuccess == 0 ? null : tmpSuccess == 1;
        data = in.readParcelable(Data.class.getClassLoader());
        if (in.readByte() == 0) {
            code = null;
        } else {
            code = in.readInt();
        }
        message = in.readString();
    }

    public static final Creator<TransactionHistoryModal> CREATOR = new Creator<TransactionHistoryModal>() {
        @Override
        public TransactionHistoryModal createFromParcel(Parcel in) {
            return new TransactionHistoryModal(in);
        }

        @Override
        public TransactionHistoryModal[] newArray(int size) {
            return new TransactionHistoryModal[size];
        }
    };

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (success == null ? 0 : success ? 1 : 2));
        dest.writeParcelable(data, flags);
        if (code == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(code);
        }
        dest.writeString(message);
    }

    public class TransactionHistory implements Parcelable{

        @SerializedName("withdrawnAmount")
        @Expose
        private Double withdrawnAmount;
        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("updated_at")
        @Expose
        private String updatedAt;
        @SerializedName("transStatus")
        @Expose
        private String transStatus;
        @SerializedName("bank_details")
        @Expose
        private BankDetails bankDetails;

        protected TransactionHistory(Parcel in) {
            if (in.readByte() == 0) {
                withdrawnAmount = null;
            } else {
                withdrawnAmount = in.readDouble();
            }
            createdAt = in.readString();
            updatedAt = in.readString();
            transStatus = in.readString();
            bankDetails = in.readParcelable(BankDetails.class.getClassLoader());
        }

        public final Creator<TransactionHistory> CREATOR = new Creator<TransactionHistory>() {
            @Override
            public TransactionHistory createFromParcel(Parcel in) {
                return new TransactionHistory(in);
            }

            @Override
            public TransactionHistory[] newArray(int size) {
                return new TransactionHistory[size];
            }
        };

        public Double getWithdrawnAmount() {
            return withdrawnAmount;
        }

        public void setWithdrawnAmount(Double withdrawnAmount) {
            this.withdrawnAmount = withdrawnAmount;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getTransStatus() {
            return transStatus;
        }

        public void setTransStatus(String transStatus) {
            this.transStatus = transStatus;
        }

        public BankDetails getBankDetails() {
            return bankDetails;
        }

        public void setBankDetails(BankDetails bankDetails) {
            this.bankDetails = bankDetails;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (withdrawnAmount == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeDouble(withdrawnAmount);
            }
            dest.writeString(createdAt);
            dest.writeString(updatedAt);
            dest.writeString(transStatus);
            dest.writeParcelable(bankDetails, flags);
        }
    }

    public class Data implements Parcelable{

        @SerializedName("transactionHistory")
        @Expose
        private List<TransactionHistory> transactionHistory = null;
        @SerializedName("page")
        @Expose
        private String page;

        protected Data(Parcel in) {
            page = in.readString();
        }

        public final Creator<Data> CREATOR = new Creator<Data>() {
            @Override
            public Data createFromParcel(Parcel in) {
                return new Data(in);
            }

            @Override
            public Data[] newArray(int size) {
                return new Data[size];
            }
        };

        public List<TransactionHistory> getTransactionHistory() {
            return transactionHistory;
        }

        public void setTransactionHistory(List<TransactionHistory> transactionHistory) {
            this.transactionHistory = transactionHistory;
        }

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(page);
        }
    }

    public static class BankDetails implements Parcelable {

        @SerializedName("AccountNo")
        @Expose
        private String accountNo;
        @SerializedName("bankName")
        @Expose
        private String bankName;

        protected BankDetails(Parcel in) {
            accountNo = in.readString();
            bankName = in.readString();
        }

        public static final Creator<BankDetails> CREATOR = new Creator<BankDetails>() {
            @Override
            public BankDetails createFromParcel(Parcel in) {
                return new BankDetails(in);
            }

            @Override
            public BankDetails[] newArray(int size) {
                return new BankDetails[size];
            }
        };

        public String getAccountNo() {
            return accountNo;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(accountNo);
            dest.writeString(bankName);
        }
    }

}
