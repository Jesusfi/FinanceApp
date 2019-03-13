package com.codegud.financeapp;

/**
 * An interface to communicate between AddEnvelopeDialogFragment
 * and Dashboard Activity
 */
public interface AddCategoryListener {
    void addNewEnvelope(String categoryName,String goal);
}
