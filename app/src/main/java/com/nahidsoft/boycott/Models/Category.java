package com.nahidsoft.boycott.Models;

public class Category {
    private String id;
    private String name;
    private String categoryType;

    public Category(String id, String name, String categoryType) {
        this.id = id;
        this.name = name;
        this.categoryType = categoryType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", categoryType='" + categoryType + '\'' +
                '}';
    }
}
