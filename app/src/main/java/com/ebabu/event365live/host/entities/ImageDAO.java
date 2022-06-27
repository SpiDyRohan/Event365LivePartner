package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageDAO implements Parcelable {

    private int id;
    private String venueImages;
    private boolean isPrimary;

    public ImageDAO() {
    }

    public ImageDAO(String venueImages) {
        this.venueImages = venueImages;
    }

    public ImageDAO(int id, String venueImages, boolean isPrimary) {
        this.id = id;
        this.venueImages = venueImages;
        this.isPrimary = isPrimary;
    }


    protected ImageDAO(Parcel in) {
        id = in.readInt();
        venueImages = in.readString();
        isPrimary = in.readByte() != 0;
    }

    public static final Creator<ImageDAO> CREATOR = new Creator<ImageDAO>() {
        @Override
        public ImageDAO createFromParcel(Parcel in) {
            return new ImageDAO(in);
        }

        @Override
        public ImageDAO[] newArray(int size) {
            return new ImageDAO[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVenueImages() {
        return venueImages;
    }

    public void setVenueImages(String venueImages) {
        this.venueImages = venueImages;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Override
    public String toString() {
        return "ImageDAO{" +
                "id=" + id +
                ", venueImages='" + venueImages + '\'' +
                " , isPrimary='" + isPrimary + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageDAO) {
            ImageDAO dao = (ImageDAO) o;
            return dao.getVenueImages().equalsIgnoreCase(venueImages);
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(venueImages);
        parcel.writeByte((byte) (isPrimary ? 1 : 0));
    }
}
