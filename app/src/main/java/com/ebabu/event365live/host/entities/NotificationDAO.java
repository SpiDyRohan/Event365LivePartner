package com.ebabu.event365live.host.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationDAO {

    private int id,eventId;

    private String name;
    private String msg,type;
    @SerializedName("dateTime")
    private String time;
    private String imagePath;

    private List<User> sender;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<User> getSender() {
        return sender;
    }

    public void setSender(List<User> sender) {
        this.sender = sender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "NotificationDAO{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", name='" + name + '\'' +
                ", msg='" + msg + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", sender=" + sender +
                '}';
    }

    public class User{

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", profilePic='" + profilePic + '\'' +
                    '}';
        }

        private int id;
        private String name,profilePic;

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
    }

}

