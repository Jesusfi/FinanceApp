package com.codegud.financeapp;

public class Transactions {
    private String date;
    private String amount;
    private String transactionType;
    private String message;
    private String category;

    public Transactions(){

    }

    public Transactions(String date, String amount, String transactionType, String message, String category){
        this.date = date;
        this.amount = amount;
        this.transactionType = transactionType;
        this.message = message;
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
