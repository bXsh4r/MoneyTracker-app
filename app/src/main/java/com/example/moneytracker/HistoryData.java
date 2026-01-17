package com.example.moneytracker;

public class HistoryData {
    private int id;
    private String operation;
    private String amount;
    private String description;
    private String date;

    public HistoryData(int id, String operation, String amount, String description, String date) {
        this.id = id;
        this.operation = operation;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public int getId(){
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}
