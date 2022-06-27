package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class MDaysAvailable implements Parcelable {

    int weekDayName;
    String weekDay;
    String fromTime;
    String toTime;

    public MDaysAvailable(String weekDay, String fromTime, String toTime) {
        this.weekDay = weekDay;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    protected MDaysAvailable(Parcel in) {
        weekDayName = in.readInt();
        weekDay = in.readString();
        fromTime = in.readString();
        toTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(weekDayName);
        dest.writeString(weekDay);
        dest.writeString(fromTime);
        dest.writeString(toTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MDaysAvailable> CREATOR = new Creator<MDaysAvailable>() {
        @Override
        public MDaysAvailable createFromParcel(Parcel in) {
            return new MDaysAvailable(in);
        }

        @Override
        public MDaysAvailable[] newArray(int size) {
            return new MDaysAvailable[size];
        }
    };

    public int getWeekDayName() {
        return weekDayName;
    }

    public void setWeekDayName(int weekDayName) {
        this.weekDayName = weekDayName;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }
}