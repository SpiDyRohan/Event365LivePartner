package com.ebabu.event365live.host.entities;

public class TicketBookedRelation {
    int id,userId;
    String status;
    String name;
    String profilePic,ticketType, pricePerTicket;

    public TicketBookedRelation(int id, String status, String name, String profilePic,int userId,String ticketType,String pricePerTicket) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.profilePic = profilePic;
        this.userId = userId;
        this.ticketType = ticketType;
        this.pricePerTicket = pricePerTicket;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(String pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
