package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class FreeTicketDao implements Parcelable {

    public FreeTicketDao(){
    }

    public FreeTicketDao(String id) {
        this.id = id;
    }

    protected String ticketName,totalTicketsQuantity,desc,id;

    protected FreeTicketDao(Parcel in) {
        ticketName = in.readString();
        totalTicketsQuantity = in.readString();
        desc = in.readString();
        id = in.readString();
    }

    public static final Creator<FreeTicketDao> CREATOR = new Creator<FreeTicketDao>() {
        @Override
        public FreeTicketDao createFromParcel(Parcel in) {
            return new FreeTicketDao(in);
        }

        @Override
        public FreeTicketDao[] newArray(int size) {
            return new FreeTicketDao[size];
        }
    };

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getTotalTicketsQuantity() {
        return totalTicketsQuantity;
    }

    public void setTotalTicketsQuantity(String totalTicketsQuantity) {
        this.totalTicketsQuantity = totalTicketsQuantity;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ticketName);
        parcel.writeString(totalTicketsQuantity);
        parcel.writeString(desc);
        parcel.writeString(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FreeTicketDao{" +
                "ticketName='" + ticketName + '\'' +
                ", totalTicketsQuantity='" + totalTicketsQuantity + '\'' +
                ", desc='" + desc + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof FreeTicketDao){
            FreeTicketDao dao=(FreeTicketDao)obj;
            return dao.id.equalsIgnoreCase(id);
        }
        return false;
    }
}
