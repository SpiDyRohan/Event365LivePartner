package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class VenueDAO implements Parcelable {

    private int id, venueCapacity,userId;
    private String venueAddress, websiteURL, latitude, longitude, availableDays, venueType, shortDescription,distance,isBooked;
    private List<String> imageList = new ArrayList<>();

    private boolean isVenueAvailableToOtherHosts;
    private List<ImageDAO> venueImages;
    private List<SubVenueDao> subVenues;
    private List<SubVenueDao> selectedSubVenues;
    private List<MDaysAvailable> daysAvailable;

    protected VenueDAO(Parcel in) {
        id = in.readInt();
        venueCapacity = in.readInt();
        userId = in.readInt();
        venueAddress = in.readString();
        websiteURL = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        availableDays = in.readString();
        venueType = in.readString();
        shortDescription = in.readString();
        imageList = in.createStringArrayList();
        isVenueAvailableToOtherHosts = in.readByte() != 0;
        venueImages = in.createTypedArrayList(ImageDAO.CREATOR);
        subVenues = in.createTypedArrayList(SubVenueDao.CREATOR);
        selectedSubVenues = in.createTypedArrayList(SubVenueDao.CREATOR);
        daysAvailable = in.createTypedArrayList(MDaysAvailable.CREATOR);
        name = in.readString();
        imgPath = in.readString();
        isVenueOwner = in.readString();
        distance = in.readString();
        isBooked = in.readString();
    }

    public static final Creator<VenueDAO> CREATOR = new Creator<VenueDAO>() {
        @Override
        public VenueDAO createFromParcel(Parcel in) {
            return new VenueDAO(in);
        }

        @Override
        public VenueDAO[] newArray(int size) {
            return new VenueDAO[size];
        }
    };

    public List<ImageDAO> getVenueImages() {
        return venueImages;
    }

    public void setVenueImages(List<ImageDAO> venueImages) {
        this.venueImages = venueImages;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }

    public void setVenueCapacity(int venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<SubVenueDao> getSubVenues() {
        return subVenues;
    }

    public void setSubVenues(List<SubVenueDao> subVenues) {
        this.subVenues = subVenues;
    }

    public boolean isVenueAvailableToOtherHosts() {
        return isVenueAvailableToOtherHosts;
    }

    public void setVenueAvailableToOtherHosts(boolean venueAvailableToOtherHosts) {
        isVenueAvailableToOtherHosts = venueAvailableToOtherHosts;
    }

    public List<MDaysAvailable> getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(List<MDaysAvailable> daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SerializedName("venueName")
    private String name;

    @SerializedName("venueImage")
    private String imgPath;

    private String isVenueOwner;

    public VenueDAO() {
    }

    public String getIsVenueOwner() {
        return isVenueOwner;
    }

    public void setIsVenueOwner(String isVenueOwner) {
        this.isVenueOwner = isVenueOwner;
    }

    public VenueDAO(String name, String imgPath) {
        this.name = name;
        this.imgPath = imgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getIsBooked() {
        return isBooked;
    }

    public void setIsBooked(String isBooked) {
        this.isBooked = isBooked;
    }

    public List<SubVenueDao> getSelectedSubVenues() {
        return selectedSubVenues;
    }

    public void setSelectedSubVenues(List<SubVenueDao> selectedSubVenues) {
        this.selectedSubVenues = selectedSubVenues;
    }

    @Override
    public String toString() {
        return "VenueDAO{" +
                "id=" + id +
                ", venueAddress='" + venueAddress + '\'' +
                ", websiteURL='" + websiteURL + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", availableDays='" + availableDays + '\'' +
                ", imageList=" + imageList +
                ", venueImages=" + venueImages +
                ", name='" + name + '\'' +
                ", imgPath='" + imgPath + '\'' +
                ", isVenueOwner='" + isVenueOwner + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(venueCapacity);
        parcel.writeInt(userId);
        parcel.writeString(venueAddress);
        parcel.writeString(websiteURL);
        parcel.writeString(latitude);
        parcel.writeString(longitude);
        parcel.writeString(availableDays);
        parcel.writeString(venueType);
        parcel.writeString(shortDescription);
        parcel.writeStringList(imageList);
        parcel.writeByte((byte) (isVenueAvailableToOtherHosts ? 1 : 0));
        parcel.writeTypedList(venueImages);
        parcel.writeTypedList(subVenues);
        parcel.writeTypedList(selectedSubVenues);
        parcel.writeTypedList(daysAvailable);
        parcel.writeString(name);
        parcel.writeString(imgPath);
        parcel.writeString(isVenueOwner);
        parcel.writeString(distance);
        parcel.writeString(isBooked);
    }
}
