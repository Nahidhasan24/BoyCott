package com.nahidsoft.boycott.Models;

public class Country {
    private String id;
    private String countryName;

    public Country(String id, String countryName) {
        this.id = id;
        this.countryName = countryName;
    }

    public String getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }
}
