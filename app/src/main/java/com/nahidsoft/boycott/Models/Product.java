package com.nahidsoft.boycott.Models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Product {
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Integer> getCategoryList() {
        return Arrays.stream(category.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    // Getters and setters
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