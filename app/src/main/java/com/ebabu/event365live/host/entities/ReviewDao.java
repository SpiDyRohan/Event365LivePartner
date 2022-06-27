package com.ebabu.event365live.host.entities;

import java.io.Serializable;

public class ReviewDao implements Serializable {
    private int id;
    private String reviewStar,reviewText,updated_at;
    private UserDAO reviewer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReviewStar() {
        return reviewStar;
    }

    public void setReviewStar(String reviewStar) {
        this.reviewStar = reviewStar;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public UserDAO getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserDAO reviewer) {
        this.reviewer = reviewer;
    }
}
