package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubVenue {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("venueId")
    @Expose
    private Integer venueId;
    @SerializedName("subVenueName")
    @Expose
    private String subVenueName;
    @SerializedName("subVenueCapacity")
    @Expose
    private Integer subVenueCapacity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVenueId() {
        return venueId;
    }

    public void setVenueId(Integer venueId) {
        this.venueId = venueId;
    }

    public String getSubVenueName() {
        return subVenueName;
    }

    public void setSubVenueName(String subVenueName) {
        this.subVenueName = subVenueName;
    }

    public Integer getSubVenueCapacity() {
        return subVenueCapacity;
    }

    public void setSubVenueCapacity(Integer subVenueCapacity) {
        this.subVenueCapacity = subVenueCapacity;
    }

}
