package com.example.invoicemaster;

import java.sql.Timestamp;

public class StockOperation {
    private int id;
    private int itemId;
    private String operationType;
    private int quantity;
    private int currentQty;
    private Timestamp operationDate;

    // Constructor
    public StockOperation(int id, int itemId, String operationType, int quantity, int currentQty, Timestamp operationDate) {
        this.id = id;
        this.itemId = itemId;
        this.operationType = operationType;
        this.quantity = quantity;
        this.currentQty = currentQty;
        this.operationDate = operationDate;
    }

    // Getters
    public int getId() { return id; }
    public int getItemId() { return itemId; }
    public String getOperationType() { return operationType; }
    public int getQuantity() { return quantity; }
    public int getCurrentQty() { return currentQty; }
    public Timestamp getOperationDate() { return operationDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCurrentQty(int currentQty) { this.currentQty = currentQty; }
    public void setOperationDate(Timestamp operationDate) { this.operationDate = operationDate; }
}
