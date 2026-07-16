package com.bxsh4r.moneytracker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

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

    public void setDescription(String newDesc){
        this.description = newDesc;
    }

    public void setDate(String newDate){
        this.date = newDate;
    }

    // A comparator anonymous class to compare dates helping in sorting them in a different function
    public static Comparator<HistoryData> dateComparator = new Comparator<>() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        @Override
        public int compare(HistoryData date1String, HistoryData date2String) {
            LocalDate date1 = LocalDate.parse(date1String.getDate(), formatter);
            LocalDate date2 = LocalDate.parse(date2String.getDate(), formatter);

            return date2.compareTo(date1);
        }
    };
}
