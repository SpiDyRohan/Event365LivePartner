package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateSubVenue {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("venueEventId")
    @Expose
    private Integer venueEventId;
    @SerializedName("venueId")
    @Expose
    private Integer venueId;
    @SerializedName("subVenueId")
    @Expose
    private Integer subVenueId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("reserveTime")
    @Expose
    private String reserveTime;
    @SerializedName("userId")
    @Expose
    private Integer userId;
    @SerializedName("eventId")
    @Expose
    private Integer eventId;
    @SerializedName("subVenues")
    @Expose
    private List<SubVenue> subVenues = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getVenueEventId() {
        return venueEventId;
    }

    public void setVenueEventId(Integer venueEventId) {
        this.venueEventId = venueEventId;
    }

    public Integer getVenueId() {
        return venueId;
    }

    public void setVenueId(Integer venueId) {
        this.venueId = venueId;
    }

    public Integer getSubVenueId() {
        return subVenueId;
    }

    public void setSubVenueId(Integer subVenueId) {
        this.subVenueId = subVenueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReserveTime() {
        return reserveTime;
    }

    public void setReserveTime(String reserveTime) {
        this.reserveTime = reserveTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public List<SubVenue> getSubVenues() {
        return subVenues;
    }

    public void setSubVenues(List<SubVenue> subVenues) {
        this.subVenues = subVenues;
    }

}

