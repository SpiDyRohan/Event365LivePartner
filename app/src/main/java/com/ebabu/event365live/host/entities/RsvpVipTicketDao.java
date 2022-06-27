package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class RsvpVipTicketDao implements Parcelable {

    private String ticketType, ticketName, pricePerTicket, ticketQuantity, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime;
    private double discountPercentage;
    private int cancellationCharge = 0;
    private long id;

    public RsvpVipTicketDao(){

    }

    protected RsvpVipTicketDao(Parcel in) {
        ticketType = in.readString();
        ticketName = in.readString();
        pricePerTicket = in.readString();
        ticketQuantity = in.readString();
        description = in.readString();
        sellingStartDate = in.readString();
        sellingEndDate = in.readString();
        sellingStartTime = in.readString();
        sellingEndTime = in.readString();
        cancellationCharge = in.readInt();
        id = in.readInt();
        discountPercentage = in.readDouble();
    }

    public static final Creator<RsvpVipTicketDao> CREATOR = new Creator<RsvpVipTicketDao>() {
        @Override
        public RsvpVipTicketDao createFromParcel(Parcel in) {
            return new RsvpVipTicketDao(in);
        }

        @Override
        public RsvpVipTicketDao[] newArray(int size) {
            return new RsvpVipTicketDao[size];
        }
    };

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(String pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public String getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(String ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSellingStartDate() {
        return sellingStartDate;
    }

    public void setSellingStartDate(String sellingStartDate) {
        this.sellingStartDate = sellingStartDate;
    }

    public String getSellingEndDate() {
        return sellingEndDate;
    }

    public void setSellingEndDate(String sellingEndDate) {
        this.sellingEndDate = sellingEndDate;
    }

    public String getSellingStartTime() {
        return sellingStartTime;
    }

    public void setSellingStartTime(String sellingStartTime) {
        this.sellingStartTime = sellingStartTime;
    }

    public String getSellingEndTime() {
        return sellingEndTime;
    }

    public void setSellingEndTime(String sellingEndTime) {
        this.sellingEndTime = sellingEndTime;
    }

    public int getCancellationCharge() {
        return cancellationCharge;
    }

    public void setCancellationCharge(int cancellationCharge) {
        this.cancellationCharge = cancellationCharge;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int in) {

        parcel.writeString(ticketType);
        parcel.writeString(ticketName);
        parcel.writeString(pricePerTicket);
        parcel.writeString(ticketQuantity);
        parcel.writeString(description);
        parcel.writeString(sellingStartDate);
        parcel.writeString(sellingEndDate);
        parcel.writeString(sellingStartTime);
        parcel.writeString(sellingEndTime);
        parcel.writeInt(cancellationCharge);
        parcel.writeLong(id);
        parcel.writeDouble(discountPercentage);
    }
}
