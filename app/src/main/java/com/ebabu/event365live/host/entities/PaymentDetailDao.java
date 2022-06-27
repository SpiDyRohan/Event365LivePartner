package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentDetailDao {

    private String QRkey;
    private Event events;

    @SerializedName("users")
    private User user;

    public String getQRkey() {
        return QRkey;
    }

    public void setQRkey(String QRkey) {
        this.QRkey = QRkey;
    }

    public Event getEvents() {
        return events;
    }

    public void setEvents(Event events) {
        this.events = events;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "PaymentDetailDao{" +
                "QRkey='" + QRkey + '\'' +
                ", events=" + events +
                ", user=" + user +
                '}';
    }

    public class Address{
        private String venueAddress;

        public String getVenueAddress() {
            return venueAddress;
        }

        public void setVenueAddress(String venueAddress) {
            this.venueAddress = venueAddress;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "venueAddress='" + venueAddress + '\'' +
                    '}';
        }
    }

    public class User{
        private int id;
        private String name,email,latitude,longitude;

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", longitude='" + longitude + '\'' +
                    '}';
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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
    }

    public class Event{
        private String name,start,end;
        private List<Ticket> ticketBooked;
        private List<Address> address;

        public List<Address> getAddress() {
            return address;
        }

        public void setAddress(List<Address> address) {
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public List<Ticket> getTicketBooked() {
            return ticketBooked;
        }

        public void setTicketBooked(List<Ticket> ticketBooked) {
            this.ticketBooked = ticketBooked;
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

        @Override
        public String toString() {
            return "Event{" +
                    "name='" + name + '\'' +
                    ", start='" + start + '\'' +
                    ", end='" + end + '\'' +
                    ", ticketBooked=" + ticketBooked +
                    ", address=" + address +
                    '}';
        }
    }

    public class Ticket{
        private String ticketType;
        private double pricePerTicket;
        private int totalQuantity;

        public String getTicketType() {
            return ticketType;
        }

        public void setTicketType(String ticketType) {
            this.ticketType = ticketType;
        }

        public double getPricePerTicket() {
            return pricePerTicket;
        }

        public void setPricePerTicket(double pricePerTicket) {
            this.pricePerTicket = pricePerTicket;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        @Override
        public String toString() {
            return "Ticket{" +
                    "ticketType='" + ticketType + '\'' +
                    ", pricePerTicket=" + pricePerTicket +
                    ", totalQuantity=" + totalQuantity +
                    '}';
        }
    }
}






