package com.subwise.model;

public class Customer extends User {

    private String phoneNumber;

    public Customer(long userId, String name, String email,
                    String password, String phoneNumber) {
        super(userId, name, email, password);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
