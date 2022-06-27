package com.ebabu.event365live.host.fragments.rsvp_invites.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AttendeesData {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("ticketBooked")
    @Expose
    private List<TicketBooked> ticketBooked = null;

    private boolean isSelectAll;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public List<TicketBooked> getTicketBooked() {
        return ticketBooked;
    }

    public void setTicketBooked(List<TicketBooked> ticketBooked) {
        this.ticketBooked = ticketBooked;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public void setSelectAll(boolean selectAll) {
        isSelectAll = selectAll;
    }

    public class TicketBooked {

        @SerializedName("userId")
        @Expose
        private Integer userId;
        @SerializedName("users")
        @Expose
        private Users users;

        private boolean isChecked;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Users getUsers() {
            return users;
        }

        public void setUsers(Users users) {
            this.users = users;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public class Users {

            @SerializedName("name")
            @Expose
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

        }
    }
}
