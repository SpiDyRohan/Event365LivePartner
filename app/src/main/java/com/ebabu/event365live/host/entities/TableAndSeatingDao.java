package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class TableAndSeatingDao implements Parcelable {

    private String categoryName,desc,id;

    private int personPerTable,totalSeatings,noOfTables,cancellationChargesTable;

    private double pricePerTable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TableAndSeatingDao(String id) {
        this.id = id;
    }

    public TableAndSeatingDao(){}

    protected TableAndSeatingDao(Parcel in) {
        categoryName = in.readString();
        id = in.readString();
        desc = in.readString();
        personPerTable = in.readInt();
        totalSeatings = in.readInt();
        noOfTables = in.readInt();

        cancellationChargesTable = in.readInt();

        pricePerTable = in.readDouble();
    }

    public static final Creator<TableAndSeatingDao> CREATOR = new Creator<TableAndSeatingDao>() {
        @Override
        public TableAndSeatingDao createFromParcel(Parcel in) {
            return new TableAndSeatingDao(in);
        }

        @Override
        public TableAndSeatingDao[] newArray(int size) {
            return new TableAndSeatingDao[size];
        }
    };

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPersonPerTable() {
        return personPerTable;
    }

    public void setPersonPerTable(int personPerTable) {
        this.personPerTable = personPerTable;
    }

    public int getTotalSeatings() {
        return totalSeatings;
    }

    public void setTotalSeatings(int totalSeatings) {
        this.totalSeatings = totalSeatings;
    }

    public int getNoOfTables() {
        return noOfTables;
    }

    public void setNoOfTables(int noOfTables) {
        this.noOfTables = noOfTables;
    }

    public double getPricePerTable() {
        return pricePerTable;
    }

    public void setPricePerTable(double pricePerTable) {
        this.pricePerTable = pricePerTable;
    }


    public int getCancellationChargesTable() {
        return cancellationChargesTable;
    }

    public void setCancellationChargesTable(int cancellationChargesTable) {
        this.cancellationChargesTable = cancellationChargesTable;
    }

    @Override
    public String toString() {
        return "TableAndSeatingDao{" +
                "categoryName='" + categoryName + '\'' +
                ", desc='" + desc + '\'' +
                ", personPerTable=" + personPerTable +
                ", totalSeatings=" + totalSeatings +
                ", noOfTables=" + noOfTables +
                ", pricePerTable=" + pricePerTable +

                ", cancellationCharge=" + cancellationChargesTable +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(categoryName);
        parcel.writeString(desc);
        parcel.writeString(id);
        parcel.writeInt(personPerTable);
        parcel.writeInt(totalSeatings);
        parcel.writeInt(noOfTables);
        parcel.writeInt(cancellationChargesTable);
        parcel.writeDouble(pricePerTable);

    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof TableAndSeatingDao){
            TableAndSeatingDao dao=(TableAndSeatingDao)obj;
            return dao.id.equalsIgnoreCase(id);
        }
        return false;
    }
}
