package com.example.invoicemaster;

public class UserInvoice1 {
    private String email;
    private int invoiceCount;
    private int userId;
    public UserInvoice1(int userId, String email, int invoiceCount) {
        this.userId = userId;
        this.email = email;
        this.invoiceCount = invoiceCount;
    }

    public String getEmail() {
        return email;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }
    public int getUserId() {
        return userId;
    }
}