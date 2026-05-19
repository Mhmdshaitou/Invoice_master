package com.example.invoicemaster;

public class Invoiceforreport {
    private String invoiceId;
    private String clientId;
    private String userId;
    private String date;
    private String status;
    private String totalAmount;
    private String discount;
    private String tax;
    private String clientName;
    private String profit; // Added profit attribute

    public Invoiceforreport(String invoiceId, String clientId, String userId, String date, String status, String totalAmount, String discount, String tax, String clientName, String profit) {
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        this.userId = userId;
        this.date = date;
        this.status = status;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.tax = tax;
        this.clientName = clientName;
        this.profit = profit; // Initialize profit
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getDiscount() {
        return discount;
    }

    public String getTax() {
        return tax;
    }

    public String getclientName() {
        return clientName;
    }

    public String getProfit() {
        return profit; // Getter for profit
    }

    // Optional setter for profit
    public void setProfit(String profit) {
        this.profit = profit;
    }
}
