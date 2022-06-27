package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class SubVenueDao implements Parcelable {

    int id,subVenueCapacity,subVenueEventId;
    String subVenueName,isBooked;
    boolean isSelected;


    int venueId;
    int subVenueId;
    String status;

    public SubVenueDao(String subVenueName) {
        this.subVenueName = subVenueName;
    }

    public SubVenueDao(int venueId,int subVenueEventId,int subVenueCapacity, int subVenueId, String status,String subVenueName) {
        this.venueId = venueId;
        this.subVenueEventId = subVenueEventId;
        this.subVenueCapacity = subVenueCapacity;
        this.subVenueId = subVenueId;
        this.status = status;
        this.subVenueName =  subVenueName;
    }

    protected SubVenueDao(Parcel in) {
        id = in.readInt();
        venueId = in.readInt();
        subVenueId = in.readInt();
        subVenueEventId = in.readInt();
        subVenueCapacity = in.readInt();
        subVenueName = in.readString();
        status = in.readString();
        isBooked = in.readString();
        isSelected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(venueId);
        dest.writeInt(subVenueId);
        dest.writeInt(subVenueEventId);
        dest.writeInt(subVenueCapacity);
        dest.writeString(subVenueName);
        dest.writeString(status);
        dest.writeString(isBooked);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubVenueDao> CREATOR = new Creator<SubVenueDao>() {
        @Override
        public SubVenueDao createFromParcel(Parcel in) {
            return new SubVenueDao(in);
        }

        @Override
        public SubVenueDao[] newArray(int size) {
            return new SubVenueDao[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public int getSubVenueCapacity() {
        return subVenueCapacity;
    }

    public void setSubVenueCapacity(int subVenueCapacity) {
        this.subVenueCapacity = subVenueCapacity;
    }

    public String getSubVenueName() {
        return subVenueName;
    }

    public void setSubVenueName(String subVenueName) {
        this.subVenueName = subVenueName;
    }

    public int getSubVenueId() {
        return subVenueId;
    }

    public void setSubVenueId(int subVenueId) {
        this.subVenueId = subVenueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(String isBooked) {
        this.isBooked = isBooked;
    }

    public int getSubVenueEventId() {
        return subVenueEventId;
    }

    public void setSubVenueEventId(int subVenueEventId) {
        this.subVenueEventId = subVenueEventId;
    }
}