package com.nahidsoft.boycott.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String id;
    private String title;
    private String createdTime;
    private String barCode;
    private String companyName;
    private String parentCompany;
    private String reason;
    private String category;
    private String status;
    private String image;

    public Product(String id, String title, String createdTime, String barCode, String companyName, String parentCompany, String reason, String category, String status, String image) {
        this.id = id;
        this.title = title;
        this.createdTime = createdTime;
        this.barCode = barCode;
        this.companyName = companyName;
        this.parentCompany = parentCompany;
        this.reason = reason;
        this.category = category;
        this.status = status;
        this.image = image;
    }

    protected Product(Parcel in) {
        id = in.readString();
        title = in.readString();
        createdTime = in.readString();
        barCode = in.readString();
        companyName = in.readString();
        parentCompany = in.readString();
        reason = in.readString();
        category = in.readString();
        status = in.readString();
        image = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(createdTime);
        dest.writeString(barCode);
        dest.writeString(companyName);
        dest.writeString(parentCompany);
        dest.writeString(reason);
        dest.writeString(category);
        dest.writeString(status);
        dest.writeString(image);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreatedTime() { return createdTime; }
    public String getBarCode() { return barCode; }
    public String getCompanyName() { return companyName; }
    public String getParentCompany() { return parentCompany; }
    public String getReason() { return reason; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getImage() { return image; }
}
