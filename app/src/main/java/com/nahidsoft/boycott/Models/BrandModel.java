package com.nahidsoft.boycott.Models;

public class BrandModel {
    private String id;
    private String title;
    private String createdTime;
    private String companyName;
    private String image;
    private String country;
    private String owner;
    private String status;

    public BrandModel(String id, String title, String createdTime, String companyName, String image, String country, String owner, String status) {
        this.id = id;
        this.title = title;
        this.createdTime = createdTime;
        this.companyName = companyName;
        this.image = image;
        this.country = country;
        this.owner = owner;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

