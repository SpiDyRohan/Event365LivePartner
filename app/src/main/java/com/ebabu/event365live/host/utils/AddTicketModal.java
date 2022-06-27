package com.ebabu.event365live.host.utils;

public class AddTicketModal {
    private String addedTicketId;
    private String ticketName;
    private Integer noOfTables;
    private Integer pricePerTable;
    private String description;
    private Integer parsonPerTable;
    private String ticketType;
    private Integer pricePerTicket;
    private Integer  totalQuantity;
    private Integer  cancellationChargeInPer;
    private boolean isTicketDisable;

    @Override
    public boolean equals(Object obj){
        if(obj instanceof AddTicketModal){
            AddTicketModal temp=(AddTicketModal)obj;
            return temp.addedTicketId.equalsIgnoreCase(addedTicketId);
        }
        return false;
    }

    public AddTicketModal(){
    }

    public boolean isTicketDisable() {
        return isTicketDisable;
    }

    public void setTicketDisable(boolean ticketDisable) {
        isTicketDisable = ticketDisable;
    }

    public AddTicketModal(String addedTicketId) {
        this.addedTicketId = addedTicketId;
    }

    public void setAddedTicketId(String addedTicketId) {
        this.addedTicketId = addedTicketId;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public void setNoOfTables(Integer noOfTables) {
        this.noOfTables = noOfTables;
    }

    public void setPricePerTable(Integer pricePerTable) {
        this.pricePerTable = pricePerTable;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParsonPerTable(Integer parsonPerTable) {
        this.parsonPerTable = parsonPerTable;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public void setPricePerTicket(Integer pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getAddedTicketId() {
        return addedTicketId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public Integer getNoOfTables() {
        return noOfTables;
    }

    public Integer getPricePerTable() {
        return pricePerTable;
    }

    public String getDescription() {
        return description;
    }

    public Integer getParsonPerTable() {
        return parsonPerTable;
    }

    public String getTicketType() {
        return ticketType;
    }

    public Integer getPricePerTicket() {
        return pricePerTicket;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getCancellationChargeInPer() {
        return cancellationChargeInPer;
    }

    public void setCancellationChargeInPer(Integer cancellationChargeInPer) {
        this.cancellationChargeInPer = cancellationChargeInPer;
    }

    @Override
    public String toString() {
        return "AddTicketModal{" +
                "addedTicketId='" + addedTicketId + '\'' +
                ", ticketName='" + ticketName + '\'' +
                ", noOfTables=" + noOfTables +
                ", pricePerTable=" + pricePerTable +
                ", description='" + description + '\'' +
                ", parsonPerTable=" + parsonPerTable +
                ", ticketType='" + ticketType + '\'' +
                ", pricePerTicket=" + pricePerTicket +
                ", cancellationChargeInPer=" + cancellationChargeInPer +
                ", totalQuantity=" + totalQuantity +
                '}';
    }
}
