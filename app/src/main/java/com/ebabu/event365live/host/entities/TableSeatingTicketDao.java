package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class TableSeatingTicketDao implements Parcelable {

    private String ticketName, pricePerTicket, description, personPerTable, noOfTables,
            sellingStartDate, sellingStartTime, sellingEndDate, sellingEndTime, cancellationCharge;
    private long id;
    private double discountPercentage = 0.0;


    public TableSeatingTicketDao(){

    }

    protected TableSeatingTicketDao(Parcel in) {
        ticketName = in.readString();
        pricePerTicket = in.readString();
        description = in.readString();
        personPerTable = in.readString();
        noOfTables = in.readString();
        sellingStartDate = in.readString();
        sellingEndDate = in.readString();
        sellingStartTime = in.readString();
        sellingEndTime = in.readString();
        cancellationCharge = in.readString();
        id = in.readInt();
        discountPercentage = in.readDouble();
    }

    public static final Creator<TableSeatingTicketDao> CREATOR = new Creator<TableSeatingTicketDao>() {
        @Override
        public TableSeatingTicketDao createFromParcel(Parcel in) {
            return new TableSeatingTicketDao(in);
        }

        @Override
        public TableSeatingTicketDao[] newArray(int size) {
            return new TableSeatingTicketDao[size];
        }
    };


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonPerTable() {
        return personPerTable;
    }

    public void setPersonPerTable(String personPerTable) {
        this.personPerTable = personPerTable;
    }

    public String getNoOfTables() {
        return noOfTables;
    }

    public void setNoOfTables(String noOfTables) {
        this.noOfTables = noOfTables;
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

    public String getCancellationCharge() {
        return cancellationCharge;
    }

    public void setCancellationCharge(String cancellationCharge) {
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

        parcel.writeString(ticketName);
        parcel.writeString(pricePerTicket);
        parcel.writeString(description);
        parcel.writeString(personPerTable);
        parcel.writeString(noOfTables);
        parcel.writeString(sellingStartDate);
        parcel.writeString(sellingEndDate);
        parcel.writeString(sellingStartTime);
        parcel.writeString(sellingEndTime);
        parcel.writeString(cancellationCharge);
        parcel.writeLong(id);
        parcel.writeDouble(discountPercentage);

    }
}
