package com.ebabu.event365live.host.entities;

import java.util.List;

public class UserResponse {
    private boolean success;
    private String message;
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private EventDAO eventDAO;
    private VenueDAO venueDAO;
    private UpdateEventDao updateEventDao;
    private UserDAO userDAO;
    private List<UserDAO> users;
    private List<VenueDAO> myVenueList;
    private List<VenueDAO> filterVenueList;
    private List<VenueDAO> previouslyUsedVenueList;
    private List<SubVenueDao> subVenueDaoList;
    private List<EventDAO> eventList;
    private List<String> venueImages;

    public UpdateEventDao getUpdateEventDao() {
        return updateEventDao;
    }

    public void setUpdateEventDao(UpdateEventDao updateEventDao) {
        this.updateEventDao = updateEventDao;
    }

    public List<EventDAO> getEventList() {
        return eventList;
    }

    public void setEventList(List<EventDAO> eventList) {
        this.eventList = eventList;
    }

    public List<String> getVenueImages() {
        return venueImages;
    }

    public void setVenueImages(List<String> venueImages) {
        this.venueImages = venueImages;
    }

    public EventDAO getEventDAO() {
        return eventDAO;
    }

    public void setEventDAO(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private UserData userData;

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public VenueDAO getVenueDAO() {
        return venueDAO;
    }

    public void setVenueDAO(VenueDAO venueDAO) {
        this.venueDAO = venueDAO;
    }

    public List<VenueDAO> getMyVenueList() {
        return myVenueList;
    }

    public void setMyVenueList(List<VenueDAO> myVenueList) {
        this.myVenueList = myVenueList;
    }

    public List<VenueDAO> getPreviouslyUsedVenueList() {
        return previouslyUsedVenueList;
    }

    public void setPreviouslyUsedVenueList(List<VenueDAO> previouslyUsedVenueList) {
        this.previouslyUsedVenueList = previouslyUsedVenueList;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<VenueDAO> getFilterVenueList() {
        return filterVenueList;
    }

    public void setFilterVenueList(List<VenueDAO> filterVenueList) {
        this.filterVenueList = filterVenueList;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<UserDAO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDAO> users) {
        this.users = users;
    }

    public List<SubVenueDao> getSubVenueDaoList() {
        return subVenueDaoList;
    }

    public void setSubVenueDaoList(List<SubVenueDao> subVenueDaoList) {
        this.subVenueDaoList = subVenueDaoList;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", code=" + code +
                ", users=" + users +
                '}';
    }
}
