package com.mediscreen.common.dtos;

import java.util.Date;

public class Patient {
    private String family;
    private String given;
    private Date dob;
    private String sex;
    private String address;
    private String phone;

    public Patient() {
    }

    public Patient(String family, String given, Date dob, String sex, String address, String phone) {
        this.family = family;
        this.given = given;
        this.dob = dob;
        this.sex = sex;
        this.address = address;
        this.phone = phone;
    }

    public String getFamily() {
        return family;
    }

    public String getGiven() {
        return given;
    }

    public Date getDob() {
        return dob;
    }

    public String getSex() {
        return sex;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}