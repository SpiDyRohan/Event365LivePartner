package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class RegularTicketDao implements Parcelable
{
    private String ticketType, ticketName, pricePerTicket, ticketQuantity, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime, cancellationCharge,eventDay, ticketsellingdays;
    private long id;
    private double discountPercentage;

    public RegularTicketDao(){

    }

    protected RegularTicketDao(Parcel in) {
        ticketType = in.readString();
        ticketName = in.readString();
        pricePerTicket = in.readString();
        ticketQuantity = in.readString();
        description = in.readString();
        sellingStartDate = in.readString();
        sellingEndDate = in.readString();
        sellingStartTime = in.readString();
        sellingEndTime = in.readString();
        cancellationCharge = in.readString();
        eventDay=in.readString();
        ticketsellingdays=in.readString();
        id = in.readInt();
        discountPercentage = in.readDouble();
    }

    public static final Creator<RegularTicketDao> CREATOR = new Creator<RegularTicketDao>() {
        @Override
        public RegularTicketDao createFromParcel(Parcel in) {
            return new RegularTicketDao(in);
        }

        @Override
        public RegularTicketDao[] newArray(int size) {
            return new RegularTicketDao[size];
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
    public String getEventDay() {   return eventDay;  }

    public void setEventDay(String s) { this.eventDay=s; }

    public String getTicketsellingdays() { return ticketsellingdays; }

    public void setTicketsellingdays(String s) { this.ticketsellingdays=s; }

    public String getSellingEndDate() {
        return sellingEndDate;
    }

    public void setSellingEndDate(String sellingEndDate) {
        this.sellingEndDate = sellingEndDate;
    }

    public String getSellingStartTime() {
        return sellingStartTime;
    }

    public void setSellingStartTime(String sellingStartTime) {this.sellingStartTime = sellingStartTime;}

    public String getSellingEndTime() {
        return sellingEndTime;
    }

    public void setSellingEndTime(String sellingEndTime) {
        this.sellingEndTime = sellingEndTime;
    }

    public String getCancellationCharge() {
        return cancellationCharge;
    }

    public void setCancellationCharge(String cancellationCharge) {this.cancellationCharge = cancellationCharge;}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage;}

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
        parcel.writeString(ticketsellingdays);
        parcel.writeString(eventDay);
        parcel.writeString(cancellationCharge);
        parcel.writeLong(id);
        parcel.writeDouble(discountPercentage);
    }
}
