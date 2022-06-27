package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class RSVPTicketDao extends FreeTicketDao implements Parcelable {

    private String uid;
    private double priceTicket;
    private int cancellationChargesTicket;

    public RSVPTicketDao(String uid) {
        this.uid = uid;
    }

    public RSVPTicketDao() {
    }

    protected RSVPTicketDao(Parcel in) {
        super(in);
        priceTicket = in.readDouble();
        cancellationChargesTicket = in.readInt();
        uid = in.readString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static final Creator<RSVPTicketDao> CREATOR = new Creator<RSVPTicketDao>() {
        @Override
        public RSVPTicketDao createFromParcel(Parcel in) {
            return new RSVPTicketDao(in);
        }

        @Override
        public RSVPTicketDao[] newArray(int size) {
            return new RSVPTicketDao[size];
        }
    };

    public double getPriceTicket() {
        return priceTicket;
    }

    public void setPriceTicket(double priceTicket) {
        this.priceTicket = priceTicket;
    }


    public int getCancellationChargesTicket() {
        return cancellationChargesTicket;
    }

    public void setCancellationChargesTicket(int cancellationChargesTicket) {
        this.cancellationChargesTicket = cancellationChargesTicket;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(priceTicket);
        parcel.writeString(uid);

        parcel.writeInt(cancellationChargesTicket);
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj instanceof RSVPTicketDao) {
            RSVPTicketDao dao = (RSVPTicketDao) obj;
            return dao.uid.equalsIgnoreCase(uid);
        }
        return false;
    }
}
