package com.ebabu.event365live.host.entities;

import java.util.List;

public class UserData {

    private User user;
    private List<EventDAO> upcomingEvent;
    private int countUpcomingEvent,countPastEvent,countRSVP;
    private boolean isCreateEvent,isManageUser;



    @Override
    public String toString() {
        return "UserData{" +
                "user=" + user +
                ", upcomingEvent=" + upcomingEvent +
                ", countUpcomingEvent=" + countUpcomingEvent +
                ", countPastEvent=" + countPastEvent +
                ", countRSVP=" + countRSVP +
                ", isCreateEvent=" + isCreateEvent +
                ", isManageUser=" + isManageUser +
                '}';
    }

    public List<EventDAO> getUpcomingEvent() {
        return upcomingEvent;
    }

    public void setUpcomingEvent(List<EventDAO> upcomingEvent) {
        this.upcomingEvent = upcomingEvent;
    }

    public boolean isCreateEvent() {
        return isCreateEvent;
    }

    public void setCreateEvent(boolean createEvent) {
        isCreateEvent = createEvent;
    }

    public boolean isManageUser() {
        return isManageUser;
    }

    public void setManageUser(boolean manageUser) {
        isManageUser = manageUser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getCountUpcomingEvent() {
        return countUpcomingEvent;
    }

    public void setCountUpcomingEvent(int countUpcomingEvent) {
        this.countUpcomingEvent = countUpcomingEvent;
    }

    public int getCountPastEvent() {
        return countPastEvent;
    }

    public void setCountPastEvent(int countPastEvent) {
        this.countPastEvent = countPastEvent;
    }

    public int getCountRSVP() {
        return countRSVP;
    }

    public void setCountRSVP(int countRSVP) {
        this.countRSVP = countRSVP;
    }


    public class User{
        private int id,createdBy;

        private String profilePic,userType,name, customerId, lastLoginTime,roles;

        public int getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(int createdBy) {
            this.createdBy = createdBy;
        }

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(String lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", profilePic='" + profilePic + '\'' +
                    ", userType='" + userType + '\'' +
                    ", name='" + name + '\'' +
                    ", createdBy='" + createdBy + '\'' +
                    ", roles='" + roles + '\'' +
                    ", customerId='" + customerId + '\'' +
                    ", lastLoginTime='" + lastLoginTime + '\'' +

                    '}';
        }
    }


}


