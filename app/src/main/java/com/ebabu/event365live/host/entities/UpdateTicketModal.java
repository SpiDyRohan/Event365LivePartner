package com.ebabu.event365live.host.entities;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateTicketModal {

    private int freeTicketEnabled =-1,VIPTicketEnabled = -1,VipSeatEnabled =-1 ,RSVPTicketEnabled = -1,RSVPSeatEnabled =-1;
    private boolean isViewDisabled;

    public static boolean isAllFreeTicketCanceled, isAllPaidTicketCanceled,
            isAllSeatingTicketCanceled;
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("message")
    @Expose
    private String message;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    public static class FreeTicket{

        private boolean tempCancel;
        private boolean isAddedNewTicket;
        private int isTicketDisable = -1;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("ticketName")
        @Expose
        private String ticketName;
        @SerializedName("ticketType")
        @Expose
        private String ticketType;
        @SerializedName("noOfTables")
        @Expose
        private Integer noOfTables;
        @SerializedName("pricePerTable")
        @Expose
        private Integer pricePerTable;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("cancellationChargeInPer")
        @Expose
        private String cancellationChargeInPer;
        @SerializedName("parsonPerTable")
        @Expose
        private Integer parsonPerTable;
        @SerializedName("actualQuantity")
        @Expose
        private Integer actualQuantity;
        @SerializedName("pricePerTicket")
        @Expose
        private String pricePerTicket;
        @SerializedName("totalQuantity")
        @Expose
        private Integer totalQuantity;
        @SerializedName("isCancelled")
        @Expose
        private boolean isCancelled;
        @SerializedName("isTicketBooked")
        @Expose
        private boolean isTicketBooked;

        @SerializedName("isSoldOut")
        @Expose
        private boolean isSoldOut;


        @SerializedName("sellingStartDate")
        @Expose
        private String sellingStartDate;
        @SerializedName("sellingEndDate")
        @Expose
        private String sellingEndDate;
        @SerializedName("sellingStartTime")
        @Expose
        private String sellingStartTime;
        @SerializedName("sellingEndTime")
        @Expose
        private String sellingEndTime;

        @SerializedName("discount")
        @Expose
        private double discount;

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
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

        public FreeTicket(String id){
            this.id = id;
        }

        public FreeTicket() {

        }

        public boolean isTempCancel() {
            return tempCancel;
        }

        public void setTempCancel(boolean tempCancel) {
            this.tempCancel = tempCancel;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTicketName() {
            return ticketName;
        }

        public void setTicketName(String ticketName) {
            this.ticketName = ticketName;
        }

        public String getTicketType() {
            return ticketType;
        }

        public void setTicketType(String ticketType) {
            this.ticketType = ticketType;
        }

        public Integer getNoOfTables() {
            return noOfTables;
        }

        public void setNoOfTables(Integer noOfTables) {
            this.noOfTables = noOfTables;
        }

        public Integer getPricePerTable() {
            return pricePerTable;
        }

        public void setPricePerTable(Integer pricePerTable) {
            this.pricePerTable = pricePerTable;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getParsonPerTable() {
            return parsonPerTable;
        }

        public void setParsonPerTable(Integer parsonPerTable) {
            this.parsonPerTable = parsonPerTable;
        }

        public String getCancellationChargeInPer() {
            return cancellationChargeInPer;
        }

        public void setCancellationChargeInPer(String cancellationChargeInPer) {
            this.cancellationChargeInPer = cancellationChargeInPer;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public String getPricePerTicket() {
            return pricePerTicket;
        }

        public void setPricePerTicket(String pricePerTicket) {
            this.pricePerTicket = pricePerTicket;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public boolean getIsCancelled() {
            if(isCancelled) isAllFreeTicketCanceled = true; else isAllFreeTicketCanceled = false;
            return isCancelled;
        }

        public void setIsCancelled(boolean isCancelled) {
            Log.d("nlkfsakl", "setIsCancelled: "+isCancelled);

            this.isCancelled = isCancelled;
        }

        public boolean isTicketBooked() {
            return isTicketBooked;
        }

        public void setTicketBooked(boolean ticketBooked) {
            isTicketBooked = ticketBooked;
        }


        public boolean getIsSoldOut() {
            return isSoldOut;
        }

        public void setIsSoldOut(boolean isSoldOut) {
            this.isSoldOut = isSoldOut;
        }

        public boolean isAddedNewTicket() {
            return isAddedNewTicket;
        }

        public void setAddedNewTicket(boolean addedNetTicket) {
            isAddedNewTicket = addedNetTicket;

        }

        public int isTicketDisable() {
            return isTicketDisable;
        }

        public void setTicketDisable(int ticketDisable) {
            isTicketDisable = ticketDisable;
        }


        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof FreeTicket){
                FreeTicket dao=(FreeTicket)obj;
                return dao.id.equalsIgnoreCase(id);
            }
            return false;
        }
    }

    public static class VipTableSeating {

        private boolean isTicketDisable;
        private boolean isAddedNewTicket;
        private boolean tempCancel;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("ticketName")
        @Expose
        private String ticketName;
        @SerializedName("ticketType")
        @Expose
        private String ticketType;
        @SerializedName("noOfTables")
        @Expose
        private Integer noOfTables;
        @SerializedName("pricePerTable")
        @Expose
        private Double pricePerTable;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("parsonPerTable")
        @Expose
        private Integer parsonPerTable;
        @SerializedName("actualQuantity")
        @Expose
        private Integer actualQuantity;
        @SerializedName("pricePerTicket")
        @Expose
        private String pricePerTicket;
        @SerializedName("totalQuantity")
        @Expose
        private Integer totalQuantity;

        @SerializedName("cancellationChargeInPer")
        @Expose
        private Integer cancellationChargeInPer;

        @SerializedName("isCancelled")
        @Expose
        private boolean isCancelled;
        @SerializedName("isTicketBooked")
        @Expose
        private boolean isTicketBooked;

        @SerializedName("isSoldOut")
        @Expose
        private boolean isSoldOut;

        @SerializedName("sellingStartDate")
        @Expose
        private String sellingStartDate;
        @SerializedName("sellingEndDate")
        @Expose
        private String sellingEndDate;
        @SerializedName("sellingStartTime")
        @Expose
        private String sellingStartTime;
        @SerializedName("sellingEndTime")
        @Expose
        private String sellingEndTime;
        @SerializedName("discount")
        @Expose
        private double discount;

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
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

        public VipTableSeating(String id) {
            this.id = id;
        }

        public VipTableSeating() {

        }

        public boolean isTempCancel() {
            return tempCancel;
        }

        public void setTempCancel(boolean tempCancel) {
            this.tempCancel = tempCancel;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTicketName() {
            return ticketName;
        }

        public void setTicketName(String ticketName) {
            this.ticketName = ticketName;
        }

        public String getTicketType() {
            return ticketType;
        }

        public void setTicketType(String ticketType) {
            this.ticketType = ticketType;
        }

        public Integer getNoOfTables() {
            return noOfTables;
        }

        public void setNoOfTables(Integer noOfTables) {
            this.noOfTables = noOfTables;
        }

        public Double getPricePerTable() {
            return pricePerTable;
        }

        public void setPricePerTable(Double pricePerTable) {
            this.pricePerTable = pricePerTable;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getParsonPerTable() {
            return parsonPerTable;
        }

        public void setParsonPerTable(Integer parsonPerTable) {
            this.parsonPerTable = parsonPerTable;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public String getPricePerTicket() {
            return pricePerTicket;
        }

        public void setPricePerTicket(String pricePerTicket) {
            this.pricePerTicket = pricePerTicket;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public boolean getIsCancelled() {
            if(isCancelled) isAllSeatingTicketCanceled = true; else isAllSeatingTicketCanceled = false;
            return isCancelled;
        }

        public void setIsCancelled(boolean isCancelled) {
            this.isCancelled = isCancelled;
        }


        public Integer getCancellationChargeInPer() {
            return cancellationChargeInPer;
        }

        public void setCancellationChargeInPer(Integer cancellationChargeInPer) {
            this.cancellationChargeInPer = cancellationChargeInPer;
        }

        public boolean isTicketBooked() {
            return isTicketBooked;
        }

        public void setTicketBooked(boolean ticketBooked) {
            isTicketBooked = ticketBooked;
        }

        public boolean getIsSoldOut() {
            return isSoldOut;
        }

        public void setIsSoldOut(boolean isSoldOut) {
            this.isSoldOut = isSoldOut;
        }

        public boolean isAddedNewTicket() {
            return isAddedNewTicket;
        }

        public void setAddedNewTicket(boolean addedNewTicket) {
            isAddedNewTicket = addedNewTicket;
        }

        public boolean isTicketDisable() {
            return isTicketDisable;
        }

        public void setTicketDisable(boolean ticketDisable) {
            isTicketDisable = ticketDisable;
        }


        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof VipTableSeating){
                VipTableSeating dao=(VipTableSeating)obj;
                return dao.id.equalsIgnoreCase(id);
            }
            return false;
        }

    }
    public class Data {

        @SerializedName("event")
        @Expose
        private Event event;
        @SerializedName("freeNormal")
        @Expose
        private List<FreeTicket> freeNormal = null;
        @SerializedName("regularPaid")
        @Expose
        private List<FreeTicket> regularPaid = null;
        @SerializedName("normalTicket")
        @Expose
        private NormalTicket normalTicket;
        @SerializedName("tableSeating")
        @Expose
        private TableSeating tableSeating;

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public List<FreeTicket> getFreeNormal() {
            return freeNormal;
        }

        public void setFreeNormal(List<FreeTicket> freeNormal) {
            this.freeNormal = freeNormal;
        }

        public List<FreeTicket> getRegularPaid() {
            return regularPaid;
        }

        public void setRegularPaid(List<FreeTicket> regularPaid) {
            this.regularPaid = regularPaid;
        }

        public NormalTicket getNormalTicket() {
            return normalTicket;
        }

        public void setNormalTicket(NormalTicket normalTicket) {
            this.normalTicket = normalTicket;
        }

        public TableSeating getTableSeating() {
            return tableSeating;
        }

        public void setTableSeating(TableSeating tableSeating) {
            this.tableSeating = tableSeating;
        }

    }
    public static class Event {

        @SerializedName("start")
        @Expose
        private String start;
        @SerializedName("end")
        @Expose
        private String end;
        @SerializedName("sellingStart")
        @Expose
        private String sellingStart;
        @SerializedName("sellingEnd")
        @Expose
        private String sellingEnd;

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

        public String getSellingEnd() {
            return sellingEnd;
        }

        public void setSellingEnd(String sellingEnd) {
            this.sellingEnd = sellingEnd;
        }

    }
    public static class NormalTicket {

        @SerializedName("vipNormal")
        @Expose
        private List<RegularNormal> vipNormal = null;
        @SerializedName("regularNormal")
        @Expose
        private List<RegularNormal> regularNormal = null;

        public List<RegularNormal> getVipNormal() {
            return vipNormal;
        }

        public void setVipNormal(List<RegularNormal> vipNormal) {
            this.vipNormal = vipNormal;
        }

        public List<RegularNormal> getRegularNormal() {
            return regularNormal;
        }

        public void setRegularNormal(List<RegularNormal> regularNormal) {
            this.regularNormal = regularNormal;
        }

    }

    public static class RegularNormal {
        private boolean tempCancel;
        private boolean isAddedNetTicket;
        private boolean isTicketDisable;
        @SerializedName("eventDay")
        @Expose
        private String eventDay;
        @SerializedName("ticketSellingDays")
        @Expose
        private String ticketsellingdays;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("ticketName")
        @Expose
        private String ticketName;
        @SerializedName("ticketType")
        @Expose
        private String ticketType;
        @SerializedName("noOfTables")
        @Expose
        private Integer noOfTables;
        @SerializedName("pricePerTable")
        @Expose
        private Double pricePerTable;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("parsonPerTable")
        @Expose
        private Integer parsonPerTable;
        @SerializedName("actualQuantity")
        @Expose
        private Integer actualQuantity;
        @SerializedName("pricePerTicket")
        @Expose
        private Double pricePerTicket;
        @SerializedName("totalQuantity")
        @Expose
        private Integer totalQuantity;

        @SerializedName("cancellationChargeInPer")
        @Expose
        private Integer cancellationChargeInPer;

        @SerializedName("isCancelled")
        @Expose
        private boolean isCancelled;
        @SerializedName("isSoldOut")
        @Expose
        private boolean isSoldOut;
        @SerializedName("isTicketBooked")
        @Expose
        private boolean isTicketBooked;

        @SerializedName("sellingStartDate")
        @Expose
        private String sellingStartDate;
        @SerializedName("sellingEndDate")
        @Expose
        private String sellingEndDate;
        @SerializedName("sellingStartTime")
        @Expose
        private String sellingStartTime;
        @SerializedName("sellingEndTime")
        @Expose
        private String sellingEndTime;
        @SerializedName("discount")
        @Expose
        private double discount;

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
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


        public RegularNormal(String id) {
            this.id = id;
        }

        public RegularNormal() {

        }

        public boolean isTempCancel() {
            return tempCancel;
        }

        public void setTempCancel(boolean tempCancel) {
            this.tempCancel = tempCancel;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTicketName() {
            return ticketName;
        }

        public void setTicketName(String ticketName) { this.ticketName = ticketName;   }

        public String geteventDay()  {     return eventDay;    }

        public void seteventDay(String ticketName) {  this.eventDay = eventDay; }

        public String getticketsellingdays() {  return ticketsellingdays; }

        public void setticketsellingdays(String ticketName) { this.ticketsellingdays = ticketsellingdays;}

        public String getTicketType() {
            return ticketType;
        }

        public void setTicketType(String ticketType) {
            this.ticketType = ticketType;
        }

        public Integer getNoOfTables() {
            return noOfTables;
        }

        public void setNoOfTables(Integer noOfTables) {
            this.noOfTables = noOfTables;
        }

        public Double getPricePerTable() {
            return pricePerTable;
        }

        public void setPricePerTable(Double pricePerTable) {
            this.pricePerTable = pricePerTable;
        }

        public String  getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getParsonPerTable() {
            return parsonPerTable;
        }

        public void setParsonPerTable(Integer parsonPerTable) {
            this.parsonPerTable = parsonPerTable;
        }

        public Integer getActualQuantity() {
            return actualQuantity;
        }

        public void setActualQuantity(Integer actualQuantity) {
            this.actualQuantity = actualQuantity;
        }

        public Double getPricePerTicket() {
            return pricePerTicket;
        }

        public void setPricePerTicket(Double pricePerTicket) {
            this.pricePerTicket = pricePerTicket;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public boolean getIsCancelled() {
            if(isCancelled) isAllPaidTicketCanceled = true; else isAllPaidTicketCanceled = false;
            return isCancelled;
        }

        public void setIsCancelled(boolean isCancelled) {

            this.isCancelled = isCancelled;
        }

        public boolean isTicketBooked() {
            return isTicketBooked;
        }

        public void setTicketBooked(boolean ticketBooked) {
            isTicketBooked = ticketBooked;
        }


        public boolean getIsSoldOut() {
            return isSoldOut;
        }

        public void setIsSoldOut(boolean isSoldOut) {
            this.isSoldOut = isSoldOut;
        }


        public Integer getCancellationChargeInPer() {
            return cancellationChargeInPer;
        }

        public void setCancellationChargeInPer(Integer cancellationChargeInPer) {
            this.cancellationChargeInPer = cancellationChargeInPer;
        }

        public boolean isAddedNetTicket() {
            return isAddedNetTicket;
        }

        public void setAddedNewTicket(boolean addedNetTicket) {
            isAddedNetTicket = addedNetTicket;
        }

        public boolean isTicketDisable() {
            return isTicketDisable;
        }

        public void setTicketDisable(boolean ticketDisable) {
            isTicketDisable = ticketDisable;
        }


        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof RegularNormal){
                RegularNormal dao=(RegularNormal)obj;
                return dao.id.equalsIgnoreCase(id);
            }
            return false;
        }
    }


    public static class TableSeating {

        @SerializedName("vipTableSeating")
        @Expose
        private List<VipTableSeating> vipTableSeating = null;
        @SerializedName("regularTableSeating")
        @Expose
        private List<VipTableSeating> regularTableSeating = null;

        public List<VipTableSeating> getVipTableSeating() {
            return vipTableSeating;
        }

        public void setVipTableSeating(List<VipTableSeating> vipTableSeating) {
            this.vipTableSeating = vipTableSeating;
        }

        public List<VipTableSeating> getRegularTableSeating() {
            return regularTableSeating;
        }

        public void setRegularTableSeating(List<VipTableSeating> regularTableSeating) {
            this.regularTableSeating = regularTableSeating;
        }
    }

    public int getFreeTicketEnabled() {
        return freeTicketEnabled;
    }

    public void setFreeTicketEnabled(int freeTicketEnabled) {
        this.freeTicketEnabled = freeTicketEnabled;
    }

    public int getVIPTicketEnabled() {
        return VIPTicketEnabled;
    }

    public void setVIPTicketEnabled(int VIPTicketEnabled) {
        this.VIPTicketEnabled = VIPTicketEnabled;
    }

    public int getVipSeatEnabled() {
        return VipSeatEnabled;
    }

    public void setVipSeatEnabled(int vipSeatEnabled) {
        VipSeatEnabled = vipSeatEnabled;
    }

    public int getRSVPTicketEnabled() {
        return RSVPTicketEnabled;
    }

    public void setRSVPTicketEnabled(int RSVPTicketEnabled) {
        this.RSVPTicketEnabled = RSVPTicketEnabled;
    }

    public int getRSVPSeatEnabled() {
        return RSVPSeatEnabled;
    }

    public void setRSVPSeatEnabled(int RSVPSeatEnabled) {
        this.RSVPSeatEnabled = RSVPSeatEnabled;
    }

    public boolean isViewDisabled() {
        return isViewDisabled;
    }

    public void setViewDisabled(boolean viewDisabled) {
        isViewDisabled = viewDisabled;
    }

}
