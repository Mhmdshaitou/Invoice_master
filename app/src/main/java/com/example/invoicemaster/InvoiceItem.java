package com.example.invoicemaster;

public class InvoiceItem {
    private String itemName;
    private int quantity;
    private String unit;
    private double itemPrice;
    private String code;
    private int invoiceItemId;
    private int itemId;
    private double invoice_item_total;
    private double item_packet_price, item_carton_price;
    private int nu;
    // Constructor
    public InvoiceItem(int invoiceItemId, int itemId, String itemName, int quantity, String unit, double itemPrice, String code, double invoice_item_total, double item_packet_price, double item_carton_price,int nu) {
        this.invoiceItemId = invoiceItemId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.nu = nu;
        this.unit = unit;
        this.itemPrice = itemPrice;
        this.code = code;
        this.invoice_item_total = invoice_item_total;
        this.item_packet_price = item_packet_price;
        this.item_carton_price = item_carton_price;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public int getInvoiceItemId() {
        return invoiceItemId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getItemTotal() {
        return getQuantity() * getItemSinglePrice();
    }

    public String getUnitqty() {
        return quantity + "   " + unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getNu() {
        return nu;
    }

    public String getUnit() {
        return unit;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public double getInvoiceItemTotal() {
        return invoice_item_total;
    }

    public double getItemPacketPrice() {
        return item_packet_price;
    }

    public double getItemCartonPrice() {
        return item_carton_price;
    }


    public double getItemSinglePrice() {
        if ("PQT".equalsIgnoreCase(unit)) {
            return item_packet_price;
        } else if ("CTS".equalsIgnoreCase(unit)) {
            return item_carton_price;
        } else {
            return itemPrice;
        }
    }



}
