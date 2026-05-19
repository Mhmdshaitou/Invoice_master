package com.example.invoicemaster;

public class Item {
    private int id;
    private String name;
    private String code;
    private int cartonNumber;
    private int packetNumber;
    private int cartonQty;
    private double costPrice;
    private int itemPrice;
    private double cartonPrice;
    private double packetPrice;

    // Constructor
    public Item(int id, String name, String code, int cartonNumber, int packetNumber, int cartonQty, double costPrice, int itemPrice, double cartonPrice, double packetPrice) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.cartonNumber = cartonNumber;
        this.packetNumber = packetNumber;
        this.cartonQty = cartonQty;
        this.costPrice = costPrice;
        this.itemPrice = itemPrice;
        this.cartonPrice = cartonPrice;
        this.packetPrice = packetPrice;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }


    public int getCartonNumber() { return cartonNumber; }
    public int getPacketNumber() { return packetNumber; }
    public int getCartonQty() { return cartonQty; }
    public double getCostPrice() { return costPrice; }
    public int getItemPrice() { return itemPrice; }
    public double getCartonPrice() { return cartonPrice; }
    public double getPacketPrice() { return packetPrice; }
    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }



}
