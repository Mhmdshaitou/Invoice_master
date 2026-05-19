package com.example.invoicemaster;


public class Invoice {
    private String invoiceId;
    private String clientId;
    private String userId;
    private String date;
    private String status;
    private String totalAmount;
    private String discount;
    private String tax;
    private String clientName;
    public Invoice(String invoiceId, String clientId, String userId, String date, String status, String totalAmount, String discount, String tax, String clientName) {
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        this.userId = userId;
        this.date = date;
        this.status = status;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.tax = tax;
        this.clientName=clientName;
    }

    public String getInvoiceId() {
        return invoiceId;
    }
    public String getclientName() {
        return clientName;
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

    public String getClientName() {return clientName;
    }
}
