package com.codegud.financeapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class NumberTextWatcher implements TextWatcher {

    private DecimalFormat df;
    private DecimalFormat dfnd;
    private boolean hasFractionalPart;

    private EditText et;
    private String current = "";

    public NumberTextWatcher(EditText et)
    {
        DecimalFormat dec = new DecimalFormat("0.00");

        this.et =et;
    }

    @SuppressWarnings("unused")
    private static final String TAG = "NumberTextWatcher";

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            et.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[$,.]", "");

            double parsed = Double.parseDouble(cleanString);
            String formatted = NumberFormat.getCurrencyInstance().format((parsed / 100));

            current = formatted;
            et.setText(formatted);
            et.setSelection(formatted.length());

            et.addTextChangedListener(this);
        }

    }

}
