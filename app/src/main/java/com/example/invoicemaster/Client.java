package com.example.invoicemaster;

public class Client {
    private String clientId;
    private String clientName;
    private String phoneNumber;
    private String clientAddress;

    // Constructor
    public Client(String clientId, String clientName, String phoneNumber, String clientAddress) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.clientAddress = clientAddress;
    }
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getClientAddress() {
        return clientAddress;
    }

}
