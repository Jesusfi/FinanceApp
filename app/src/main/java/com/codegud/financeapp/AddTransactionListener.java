package com.codegud.financeapp;

public interface AddTransactionListener {
    void addNewTransactionAndUpdateEnvelope(String amount, String type);
}
