package com.ebabu.event365live.host.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class CreateEventDAO implements Parcelable {

    private int eventType,categoryId,venueId;
    private String name,eventOccuranceType,catIDSubID,occurredOn,ticketsellingdays,eventDay,startTime,endTime,SellingStartDate,SellingEndDate,start,end,
            startTimeMon,endTimeMon,startTimeTues,endTimeTues,startTimeWednes,endTimeWednes,startTimeThurs,endTimeThurs,
            startTimeFri,endTimeFri,startTimeSatur,endTimeSatur,startTimeSun,endTimeSun,startDate,endDate,venueAddress,lat,longt,
            desc,desc2,paidType,venueName,occorredOnDays,sellStartTime,sellEndTime,sellStartDate,sellEndDate,seatingType,ticketInfoWhenTicketsNotSelected,subCategoryId;

    // New Parameters Add by Lokesh Panchal
    private String paymentId, hostMobile, hostAddress, websiteUrl,websiteUrlOther;

    private List<String> imageList,venueImageList;
    private List<ImageDAO> imageDAOList;
    private List<FreeTicketDao> freeTicketDaoList;
    private List<RegularTicketDao> freeRegularTicketDaoList;
    private List<RsvpVipTicketDao> rsvpVipTicketDaoList;
    private List<TableSeatingTicketDao> tableSeatingTicketDaoList;
    private List<SubVenueDao> selectedSubVenues;

    private List<RSVPTicketDao> rsvpTicketDaoList,vipTicketDaoList;
    private int freeTicketEnabled,VIPTicketEnabled,VipSeatEnabled,RSVPTicketEnabled,RSVPSeatEnabled;
    private List<TableAndSeatingDao> rsvpTableAndSeatingDaos,vipTableAndSeatingDaos;

    public String getCatIDSubID() {
        return catIDSubID;
    }

    public String getTicketsellingdays() {
        return ticketsellingdays;
    }

    public void setTicketsellingdays(String ticketsellingdays) {
        this.ticketsellingdays = ticketsellingdays;
    }

    public String getSellingStartDate() {
        return SellingStartDate;
    }

    public void setSellingStartDate(String sellingStartDate) {
        SellingStartDate = sellingStartDate;
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

    public String getSellingEndDate() {
        return SellingEndDate;
    }

    public void setSellingEndDate(String sellingEndDate) {
        SellingEndDate = sellingEndDate;
    }

    public String getEventDay() {
        return eventDay;
    }

    public RegularTicketDao setEventDay(String eventDay) {
        this.eventDay = eventDay;
        return null;
    }

    public void setCatIDSubID(String catIDSubID) {
        this.catIDSubID = catIDSubID;
    }

    private String countryCode,cityName,helplineNumber;

    public String getHelplineNumber() {
        return helplineNumber;
    }

    public void setHelplineNumber(String helplineNumber) {
        this.helplineNumber = helplineNumber;
    }

    public int getFreeTicketEnabled() {
        return freeTicketEnabled;
    }

    public int getVIPTicketEnabled() {
        return VIPTicketEnabled;
    }

    public int getVipSeatEnabled() {
        return VipSeatEnabled;
    }

    public int getRSVPTicketEnabled() {
        return RSVPTicketEnabled;
    }

    public int getRSVPSeatEnabled() {
        return RSVPSeatEnabled;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getTicketInfoWhenTicketsNotSelected() {
        return ticketInfoWhenTicketsNotSelected;
    }

    public void setTicketInfoWhenTicketsNotSelected(String ticketInfoWhenTicketsNotSelected) {
        this.ticketInfoWhenTicketsNotSelected = ticketInfoWhenTicketsNotSelected;
    }

    public List<ImageDAO> getImageDAOList() {
        return imageDAOList;
    }

    public void setImageDAOList(List<ImageDAO> imageDAOList) {
        this.imageDAOList = imageDAOList;
    }

    public List<TableAndSeatingDao> getRsvpTableAndSeatingDaos() {
        return rsvpTableAndSeatingDaos;
    }

    public void setRsvpTableAndSeatingDaos(List<TableAndSeatingDao> tableAndSeatingDaos) {
        this.rsvpTableAndSeatingDaos= tableAndSeatingDaos;
    }

    public List<TableAndSeatingDao> getVipTableAndSeatingDaos() {
        return vipTableAndSeatingDaos;
    }

    public void setVipTableAndSeatingDaos(List<TableAndSeatingDao> vipTableAndSeatingDaos) {
        this.vipTableAndSeatingDaos = vipTableAndSeatingDaos;
    }

    protected CreateEventDAO(Parcel in) {
        eventType = in.readInt();
        categoryId = in.readInt();

        venueId = in.readInt();
        name = in.readString();
        eventOccuranceType = in.readString();
        occurredOn = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        startTimeMon = in.readString();
        endTimeMon = in.readString();
        startTimeTues = in.readString();
        endTimeTues = in.readString();
        startTimeWednes = in.readString();
        endTimeWednes = in.readString();
        startTimeThurs = in.readString();
        endTimeThurs = in.readString();
        startTimeFri = in.readString();
        endTimeFri = in.readString();
        startTimeSatur = in.readString();
        endTimeSatur = in.readString();
        startTimeSun = in.readString();
        endTimeSun = in.readString();
        catIDSubID = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        venueAddress = in.readString();
        helplineNumber = in.readString();
        ticketInfoWhenTicketsNotSelected = in.readString();
        lat = in.readString();
        longt = in.readString();
        desc = in.readString();
        desc2 = in.readString();
        paidType = in.readString();
        countryCode = in.readString();
        cityName = in.readString();
        venueName = in.readString();
        occorredOnDays = in.readString();
        sellStartDate = in.readString();
        sellEndDate = in.readString();
        sellStartTime = in.readString();
        sellEndTime = in.readString();
        seatingType = in.readString();
        subCategoryId = in.readString();
        freeTicketEnabled=in.readInt();
        RSVPSeatEnabled=in.readInt();
        RSVPSeatEnabled=in.readInt();
        VipSeatEnabled=in.readInt();
        VIPTicketEnabled=in.readInt();

        // New
        paymentId = in.readString();
        hostMobile = in.readString();
        hostAddress = in.readString();
        websiteUrl = in.readString();
        websiteUrlOther = in.readString();
        imageList = in.createStringArrayList();
        venueImageList = in.createStringArrayList();
        freeTicketDaoList = in.createTypedArrayList(FreeTicketDao.CREATOR);
        freeRegularTicketDaoList = in.createTypedArrayList(RegularTicketDao.CREATOR);
        rsvpVipTicketDaoList = in.createTypedArrayList(RsvpVipTicketDao.CREATOR);
        tableSeatingTicketDaoList = in.createTypedArrayList(TableSeatingTicketDao.CREATOR);
        rsvpTicketDaoList = in.createTypedArrayList(RSVPTicketDao.CREATOR);
        vipTicketDaoList= in.createTypedArrayList(RSVPTicketDao.CREATOR);
        rsvpTableAndSeatingDaos= in.createTypedArrayList(TableAndSeatingDao.CREATOR);
        vipTableAndSeatingDaos= in.createTypedArrayList(TableAndSeatingDao.CREATOR);
        imageDAOList= in.createTypedArrayList(ImageDAO.CREATOR);
        selectedSubVenues = in.createTypedArrayList(SubVenueDao.CREATOR);
    }

    public static final Creator<CreateEventDAO> CREATOR = new Creator<CreateEventDAO>()
    {
        @Override
        public CreateEventDAO createFromParcel(Parcel in) {
            return new CreateEventDAO(in);
        }
        @Override
        public CreateEventDAO[] newArray(int size) {
            return new CreateEventDAO[size];
        }
    };

    public List<FreeTicketDao> getFreeTicketDaoList() {
        return freeTicketDaoList;
    }

    public void setFreeTicketDaoList(List<FreeTicketDao> freeTicketDaoList) {this.freeTicketDaoList = freeTicketDaoList;}

    public List<RegularTicketDao> getFreeRegularTicketDaoList() {
        return freeRegularTicketDaoList;
    }

    public void setFreeRegularTicketDaoList(List<RegularTicketDao> freeRegularTicketDaoList) { this.freeRegularTicketDaoList = freeRegularTicketDaoList;}

    public List<RsvpVipTicketDao> getRsvpVipTicketDaoList() {
        return rsvpVipTicketDaoList;
    }

    public void setRsvpVipTicketDaoList(List<RsvpVipTicketDao> rsvpVipTicketDaoList) {this.rsvpVipTicketDaoList = rsvpVipTicketDaoList;}

    public List<TableSeatingTicketDao> getTableSeatingTicketDaoList() { return tableSeatingTicketDaoList;}

    public void setTableSeatingTicketDaoList(List<TableSeatingTicketDao> tableSeatingTicketDaoList) {this.tableSeatingTicketDaoList = tableSeatingTicketDaoList;}

    public List<RSVPTicketDao> getRsvpTicketDaoList() {
        return rsvpTicketDaoList;
    }

    public void setRsvpTicketDaoList(List<RSVPTicketDao> rsvpTicketDaoList) {
        this.rsvpTicketDaoList = rsvpTicketDaoList;
    }

    public CreateEventDAO() {
        freeTicketEnabled=1;
        RSVPSeatEnabled=1;
        RSVPTicketEnabled=1;
        VipSeatEnabled=1;
        VIPTicketEnabled=1;
    }


    public List<RSVPTicketDao> getVipTicketDaoList() {
        return vipTicketDaoList;
    }

    public void setVipTicketDaoList(List<RSVPTicketDao> vipTicketDaoList) {
        this.vipTicketDaoList = vipTicketDaoList;
    }

    public int isFreeTicketEnabled() {
        return freeTicketEnabled;
    }

    public void setFreeTicketEnabled(int freeTicketEnabled) {
        this.freeTicketEnabled = freeTicketEnabled;
    }

    public int  isVIPTicketEnabled() {
        return VIPTicketEnabled;
    }

    public void setVIPTicketEnabled(int VIPTicketEnabled) {
        this.VIPTicketEnabled = VIPTicketEnabled;
    }

    public int  isVipSeatEnabled() {
        return VipSeatEnabled;
    }

    public void setVipSeatEnabled(int  vipSeatEnabled) {
        VipSeatEnabled = vipSeatEnabled;
    }

    public int  isRSVPTicketEnabled() {
        return RSVPTicketEnabled;
    }

    public void setRSVPTicketEnabled(int  RSVPTicketEnabled) {
        this.RSVPTicketEnabled = RSVPTicketEnabled;
    }

    public int  isRSVPSeatEnabled() {
        return RSVPSeatEnabled;
    }

    public void setRSVPSeatEnabled(int  RSVPSeatEnabled) {
        this.RSVPSeatEnabled = RSVPSeatEnabled;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }



    public String getOccurredOn() {
        return occurredOn;
    }

    public void setOccurredOn(String occurredOn) {
        this.occurredOn = occurredOn;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {this.endTime = endTime;}

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongt() {
        return longt;
    }

    public void setLongt(String longt) {
        this.longt = longt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPaidType() {
        return paidType;
    }

    public void setPaidType(String paidType) {
        this.paidType = paidType;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEventOccuranceType() {
        return eventOccuranceType;
    }

    public void setEventOccuranceType(String eventOccuranceType) {
        this.eventOccuranceType = eventOccuranceType;
    }

    public String getSeatingType() {
        return seatingType;
    }

    public void setSeatingType(String seatingType) {
        this.seatingType = seatingType;
    }

    public List<String> getVenueImageList() {
        return venueImageList;
    }

    public void setVenueImageList(List<String> venueImageList) {
        this.venueImageList = venueImageList;
    }

    public String getSellStartTime() {
        return sellStartTime;
    }

    public void setSellStartTime(String sellStartTime) {
        this.sellStartTime = sellStartTime;
    }

    public String getSellEndTime() {
        return sellEndTime;
    }

    public void setSellEndTime(String sellEndTime) {
        this.sellEndTime = sellEndTime;
    }

    public String getSellStartDate() {
        return sellStartDate;
    }

    public void setSellStartDate(String sellStartDate) {
        this.sellStartDate = sellStartDate;
    }

    public String getSellEndDate() {
        return sellEndDate;
    }

    public void setSellEndDate(String sellEndDate) {
        this.sellEndDate = sellEndDate;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public String getWebsiteUrlOther() {
        return websiteUrlOther;
    }

    public void setWebsiteUrlOther(String websiteUrlOther) {
        this.websiteUrlOther = websiteUrlOther;
    }

    @Override
    public String toString() {
        return "CreateEventDAO{" +
                "eventType=" + eventType +
                ", categoryId=" + categoryId +
                ", subCategoryId=" + subCategoryId +
                ", venueId=" + venueId +
                ", name='" + name + '\'' +

                ", paymentId='" + paymentId + '\'' +
                ", hostMobile='" + hostMobile + '\'' +
                ", hostAddress='" + hostAddress + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", websiteUrlOther='" + websiteUrlOther + '\'' +

                ", eventOccuranceType='" + eventOccuranceType + '\'' +
                ", occurredOn='" + occurredOn + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", startTimeMon='" + startTimeMon + '\'' +
                ", endTimeMon='" + endTimeMon + '\'' +
                ", startTimeTues='" + startTimeTues + '\'' +
                ", endTimeTues='" + endTimeTues + '\'' +
                ", startTimeWednes='" + startTimeWednes + '\'' +
                ", endTimeWednes='" + endTimeWednes + '\'' +
                ", startTimeThurs='" + startTimeThurs + '\'' +
                ", endTimeThurs='" + endTimeThurs + '\'' +
                ", startTimeFri='" + startTimeFri + '\'' +
                ", endTimeFri='" + endTimeFri + '\'' +
                ", startTimeSatur='" + startTimeSatur + '\'' +
                ", endTimeSatur='" + endTimeSatur + '\'' +
                ", startTimeSun='" + startTimeSun + '\'' +
                ", endTimeSun='" + endTimeSun + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", venueAddress='" + venueAddress + '\'' +
                ", lat='" + lat + '\'' +
                ", longt='" + longt + '\'' +
                ", desc='" + desc + '\'' +
                ", paidType='" + paidType + '\'' +
                ", venueName='" + venueName + '\'' +
                ", occorredOnDays='" + occorredOnDays + '\'' +
                ", sellStartTime='" + sellStartTime + '\'' +
                ", sellEndTime='" + sellEndTime + '\'' +
                ", sellStartDate='" + sellStartDate + '\'' +
                ", sellEndDate='" + sellEndDate + '\'' +
                ", seatingType='" + seatingType + '\'' +
                ", imageList=" + imageList +
                ", venueImageList=" + venueImageList +
                ", freeTicketEnabled=" + freeTicketEnabled +
                ", VIPTicketEnabled=" + VIPTicketEnabled +
                ", VipSeatEnabled=" + VipSeatEnabled +
                ", RSVPTicketEnabled=" + RSVPTicketEnabled +
                ", RSVPSeatEnabled=" + RSVPSeatEnabled +
                ", freeTicketDaoList=" + freeTicketDaoList +

                ", freeRegularTicketDaoList=" + freeRegularTicketDaoList +
                ", rsvpVipTicketDaoList=" + rsvpVipTicketDaoList +
                ", tableSeatingTicketDaoList=" + tableSeatingTicketDaoList +

                ", rsvpTicketDaoList=" + rsvpTicketDaoList +
                ", vipTicketDaoList=" + vipTicketDaoList +
                ", rsvpTableAndSeatingDaos=" + rsvpTableAndSeatingDaos +
                ", vipTableAndSeatingDaos=" + vipTableAndSeatingDaos +
                '}';
    }

    public String getOccorredOnDays() {
        return occorredOnDays;
    }

    public void setOccorredOnDays(String occorredOnDays) {
        this.occorredOnDays = occorredOnDays;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(eventType);
        parcel.writeInt(categoryId);
        parcel.writeInt(venueId);
        parcel.writeString(subCategoryId);
        parcel.writeString(name);
        parcel.writeString(eventOccuranceType);
        parcel.writeString(occurredOn);
        parcel.writeString(startTime);
        parcel.writeString(endTime);
        parcel.writeString(startTimeMon);
        parcel.writeString(endTimeMon);
        parcel.writeString(startTimeTues);
        parcel.writeString(endTimeTues);
        parcel.writeString(startTimeWednes);
        parcel.writeString(endTimeWednes);
        parcel.writeString(startTimeThurs);
        parcel.writeString(endTimeThurs);
        parcel.writeString(startTimeFri);
        parcel.writeString(endTimeFri);
        parcel.writeString(startTimeSatur);
        parcel.writeString(endTimeSatur);
        parcel.writeString(startTimeSun);
        parcel.writeString(endTimeSun);
        parcel.writeString(startDate);
        parcel.writeString(endDate);
        parcel.writeString(venueAddress);
        parcel.writeString(lat);
        parcel.writeString(longt);
        parcel.writeString(desc);
        parcel.writeString(desc2);
        parcel.writeString(paidType);
        parcel.writeString(venueName);
        parcel.writeString(countryCode);
        parcel.writeString(cityName);
        parcel.writeString(occorredOnDays);
        parcel.writeString(ticketInfoWhenTicketsNotSelected);
        parcel.writeString(helplineNumber);
        parcel.writeString(catIDSubID);

        parcel.writeString(paymentId);
        parcel.writeString(hostMobile);
        parcel.writeString(hostAddress);
        parcel.writeString(websiteUrl);
        parcel.writeString(websiteUrlOther);

        parcel.writeInt(freeTicketEnabled  );
        parcel.writeInt(RSVPSeatEnabled  );
        parcel.writeInt(RSVPSeatEnabled  );
        parcel.writeInt(VipSeatEnabled  );
        parcel.writeInt(VIPTicketEnabled  );

        parcel.writeString(sellStartDate);
        parcel.writeString(sellEndDate);
        parcel.writeString(sellStartTime);
        parcel.writeString(sellEndTime);
        parcel.writeString(seatingType);

        parcel.writeStringList(imageList);
        parcel.writeStringList(venueImageList);
        parcel.writeTypedList(freeTicketDaoList);

        parcel.writeTypedList(freeRegularTicketDaoList);
        parcel.writeTypedList(rsvpVipTicketDaoList);
        parcel.writeTypedList(tableSeatingTicketDaoList);

        parcel.writeTypedList(rsvpTicketDaoList);
        parcel.writeTypedList(vipTicketDaoList);
        parcel.writeTypedList(rsvpTableAndSeatingDaos);
        parcel.writeTypedList(vipTableAndSeatingDaos);
        parcel.writeTypedList(imageDAOList);
        parcel.writeTypedList(selectedSubVenues);
    }

    public List<SubVenueDao> getSelectedSubVenues() {
        return selectedSubVenues;
    }

    public void setSelectedSubVenues(List<SubVenueDao> selectedSubVenues) {
        this.selectedSubVenues = selectedSubVenues;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }
}

