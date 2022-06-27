package com.ebabu.event365live.host.entities;

import android.os.Build;
import android.text.Html;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class EventDAO implements Serializable{
    private int id,userId,eventType,reviewCount,ratingCount,totalRSVP,checkedInRSVP;
    private double totalPayment;
    private boolean isEventAvailable;
    private boolean isExternalTicket;
    private VenueDAO venue;
    private float rating;
    private String imageShow;
    private Coupon coupan;

    public Coupon getCoupan() {
        return coupan;
    }

    public void setCoupan(Coupon coupan) {
        this.coupan = coupan;
    }

    public String getImageShow() {
        return imageShow;
    }

    public void setImageShow(String imageShow) {
        this.imageShow = imageShow;
    }

    public VenueDAO getVenue() {
        return venue;
    }

    public void setVenue(VenueDAO venue) {
        this.venue = venue;
    }

    public EventDAO(int id) {
        this.id = id;
    }

    public EventDAO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDAO dao = (EventDAO) o;
        return id == dao.id;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getTotalRSVP() {
        return totalRSVP;
    }

    public void setTotalRSVP(int totalRSVP) {
        this.totalRSVP = totalRSVP;
    }

    public int getCheckedInRSVP() {
        return checkedInRSVP;
    }

    public void setCheckedInRSVP(int checkedInRSVP) {
        this.checkedInRSVP = checkedInRSVP;
    }

    public double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public boolean isEventAvailable() {
        return isEventAvailable;
    }

    public void setEventAvailable(boolean eventAvailable) {
        isEventAvailable = eventAvailable;
    }

    private String name,deadlineDate,deadlineTime,paidType,description,description2,eventAddress,categoryName,subCategoryName;

    public boolean isExternalTicket() {
        return isExternalTicket;
    }

    public void setExternalTicket(boolean externalTicket) {
        isExternalTicket = externalTicket;
    }

    @SerializedName("start")
    private String startDate;

    @SerializedName("end")
    private String endDate;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    private UserDAO host;
    private List<ReviewDao> reviews;
    private List<EventCategoryDAO> subCategories;
    private List<EventImages> eventImages;

    public List<EventCategoryDAO> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<EventCategoryDAO> subCategories) {
        this.subCategories = subCategories;
    }

    public List<ReviewDao> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDao> reviews) {
        this.reviews = reviews;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public UserDAO getHost() {
        return host;
    }

    public void setHost(UserDAO host) {
        this.host = host;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(String deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public String getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(String deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getPaidType() {
        return paidType;
    }

    public void setPaidType(String paidType) {
        this.paidType = paidType;
    }

    public String getDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return String.valueOf(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));
        else
            return String.valueOf(Html.fromHtml(description));

        //return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventAddress() {
        return eventAddress;
    }

    public void setEventAddress(String eventAddress) {
        this.eventAddress = eventAddress;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public List<EventImages> getEventImages() {
        return eventImages;
    }

    public void setEventImages(List<EventImages> eventImages) {
        this.eventImages = eventImages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }


    @Override
    public String toString() {
        return "EventDAO{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventType=" + eventType +
                ", reviewCount=" + reviewCount +
                ", rating=" + rating +
                ", ratingCount=" + ratingCount +
                ", totalRSVP=" + totalRSVP +
                ", checkedInRSVP=" + checkedInRSVP +
                ", totalPayment=" + totalPayment +
                ", isEventAvailable=" + isEventAvailable +
                ", venue=" + venue +
                ", name='" + name + '\'' +
                ", deadlineDate='" + deadlineDate + '\'' +
                ", deadlineTime='" + deadlineTime + '\'' +
                ", paidType='" + paidType + '\'' +
                ", description='" + description + '\'' +
                ", description2='" + description2 + '\'' +
                ", eventAddress='" + eventAddress + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", host=" + host +
                ", reviews=" + reviews +
                ", subCategories=" + subCategories +
                ", eventImages=" + eventImages +
                '}';
    }
}
