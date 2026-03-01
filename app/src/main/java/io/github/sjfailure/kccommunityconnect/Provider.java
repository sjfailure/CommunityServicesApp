package io.github.sjfailure.kccommunityconnect;

public class Provider {

    private final String name;
    private final String address;
    private final String phone;
    private final String email;

    public Provider(String name, String address, String phone, String email) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters for the fields
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

}
