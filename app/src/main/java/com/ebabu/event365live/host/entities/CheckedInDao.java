package com.ebabu.event365live.host.entities;

import androidx.annotation.Nullable;

import java.util.List;

public class CheckedInDao {
    private int id, userId, eventId, ticketId,ticketBookedId;
    private String status, created_at, updated_at, ticketType, ticketName, pricePerTicket, QRkey;

    private List<TicketBookedRelation> ticket_number_booked_rel;

    public String getQRkey() {
        return QRkey;
    }

    public void setQRkey(String QRkey) {
        this.QRkey = QRkey;
    }

    private User users;

    public int getTicketBookedId() {
        return ticketBookedId;
    }

    public void setTicketBookedId(int ticketBookedId) {
        this.ticketBookedId = ticketBookedId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getPricePerTicket() {
        return pricePerTicket;
    }

    public void setPricePerTicket(String pricePerTicket) {
        this.pricePerTicket = pricePerTicket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public User getUsers() {
        return users;
    }

    public void setUsers(User users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "CheckedInDao{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventId=" + eventId +
                ", status='" + status + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", ticketType='" + ticketType + '\'' +
                ", ticketName='" + ticketName + '\'' +
                ", pricePerTicket='" + pricePerTicket + '\'' +
                ", users=" + users +
                '}';
    }

    public class User {
        private String name;
        private String profilePic;
        private int id;

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

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", profilePic='" + profilePic + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof CheckedInDao) {
            return id == ((CheckedInDao) obj).getId();
        }
        return false;
    }

    public List<TicketBookedRelation> getTicket_number_booked_rel() {
        return ticket_number_booked_rel;
    }

    public void setTicket_number_booked_rel(List<TicketBookedRelation> ticket_number_booked_rel) {
        this.ticket_number_booked_rel = ticket_number_booked_rel;
    }


}


