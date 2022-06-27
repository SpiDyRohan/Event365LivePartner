package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateEventDao {
    private int id, venueId, categoryId;
    private String name, start, end, sellingStart, eventType, sellingEnd, description2, description, ticketInfoURL, eventHelpLine,
            eventOccurrenceType, categoryName,countryCode,hostMobile,hostAddress,websiteUrl,otherWebsiteUrl;
    private boolean is_availability, isVenuReg, isExternalTicket;
    private List<EventImages> eventImages;
    private List<EventOccurrance> eventOccurrence;
    private VenueDAO venue;
    private List<UpdateSubVenue> subVenueEvent;
    private List<sponserImages> sponsorImageList;
    private List<telentImages> talentsImageList;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getHostMobile() {
        return hostMobile;
    }

    public void setHostMobile(String hostMobile) {
        this.hostMobile = hostMobile;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getOtherWebsiteUrl() {
        return otherWebsiteUrl;
    }

    public void setOtherWebsiteUrl(String otherWebsiteUrl) {
        this.otherWebsiteUrl = otherWebsiteUrl;
    }

    public List<sponserImages> getSponsorImageList() {
        return sponsorImageList;
    }

    public void setSponsorImageList(List<sponserImages> sponsorImageList) {
        this.sponsorImageList = sponsorImageList;
    }

    public List<telentImages> getTalentsImageList() {
        return talentsImageList;
    }

    public void setTalentsImageList(List<telentImages> talentsImageList) {
        this.talentsImageList = talentsImageList;
    }

    public class sponserImages {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("eventImage")
        @Expose
        private String eventImage = null;
        @SerializedName("imageType")
        @Expose
        private String imageType = null;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEventImage() {
            return eventImage;
        }

        public void setEventImage(String eventImage) {
            this.eventImage = eventImage;
        }

        public String getImageType() {
            return imageType;
        }

        public void setImageType(String imageType) {
            this.imageType = imageType;
        }
    }

    public class telentImages {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("eventImage")
        @Expose
        private String eventImage = null;
        @SerializedName("imageType")
        @Expose
        private String imageType = null;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getEventImage() {
            return eventImage;
        }

        public void setEventImage(String eventImage) {
            this.eventImage = eventImage;
        }

        public String getImageType() {
            return imageType;
        }

        public void setImageType(String imageType) {
            this.imageType = imageType;
        }
    }

    public List<UpdateSubVenue> getSubVenueEvent() {
        return subVenueEvent;
    }

    public void setSubVenueEvent(List<UpdateSubVenue> subVenueEvent) {
        this.subVenueEvent = subVenueEvent;
    }

    public VenueDAO getVenue() {
        return venue;
    }

    public void setVenue(VenueDAO venue) {
        this.venue = venue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart() {

        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getSellingStart() {
        return sellingStart;
    }

    public void setSellingStart(String sellingStart) {
        this.sellingStart = sellingStart;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSellingEnd() {
        return sellingEnd;
    }

    public void setSellingEnd(String sellingEnd) {
        this.sellingEnd = sellingEnd;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTicketInfoURL() {
        return ticketInfoURL;
    }

    public void setTicketInfoURL(String ticketInfoURL) {
        this.ticketInfoURL = ticketInfoURL;
    }

    public String getEventHelpLine() {
        return eventHelpLine;
    }

    public void setEventHelpLine(String eventHelpLine) {
        this.eventHelpLine = eventHelpLine;
    }

    public String getEventOccurrenceType() {
        return eventOccurrenceType;
    }

    public void setEventOccurrenceType(String eventOccurrenceType) {
        this.eventOccurrenceType = eventOccurrenceType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isIs_availability() {
        return is_availability;
    }

    public void setIs_availability(boolean is_availability) {
        this.is_availability = is_availability;
    }

    public boolean isVenuReg() {
        return isVenuReg;
    }

    public void setVenuReg(boolean venuReg) {
        isVenuReg = venuReg;
    }

    public boolean isExternalTicket() {
        return isExternalTicket;
    }

    public void setExternalTicket(boolean externalTicket) {
        isExternalTicket = externalTicket;
    }

    public List<EventImages> getEventImages() {
        return eventImages;
    }

    public void setEventImages(List<EventImages> eventImages) {
        this.eventImages = eventImages;
    }

    public List<EventOccurrance> getEventOccurrence() {
        return eventOccurrence;
    }

    public void setEventOccurrence(List<EventOccurrance> eventOccurrence) {
        this.eventOccurrence = eventOccurrence;
    }

    @Override
    public String toString() {
        return "UpdateEventDao{" +
                "id=" + id +
                ", venueId=" + venueId +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", sellingStart='" + sellingStart + '\'' +
                ", eventType='" + eventType + '\'' +
                ", sellingEnd='" + sellingEnd + '\'' +
                ", description2='" + description2 + '\'' +
                ", description='" + description + '\'' +
                ", ticketInfoURL='" + ticketInfoURL + '\'' +
                ", eventHelpLine='" + eventHelpLine + '\'' +
                ", eventOccurrenceType='" + eventOccurrenceType + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", is_availability=" + is_availability +
                ", isVenuReg=" + isVenuReg +
                ", isExternalTicket=" + isExternalTicket +
                ", eventImages=" + eventImages +
                ", eventOccurrence=" + eventOccurrence +
                ", venue=" + venue +
                '}';
    }

    public class EventOccurrance {
        String eventOccurrence;
        int occurredOn;

        public String getEventOccurrence() {
            return eventOccurrence;
        }

        public void setEventOccurrence(String eventOccurrence) {
            this.eventOccurrence = eventOccurrence;
        }

        public int getOccurredOn() {
            return occurredOn;
        }

        public void setOccurredOn(int occurredOn) {
            this.occurredOn = occurredOn;
        }

        @Override
        public String toString() {
            return "EventOccurrance{" +
                    "eventOccurrence='" + eventOccurrence + '\'' +
                    ", occurredOn=" + occurredOn +
                    '}';
        }
    }
}
