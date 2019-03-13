package com.codegud.financeapp;

import java.text.DecimalFormat;

public class MoneyManager {

    public static String FormatMoney(String moneyToBeFormated){
        DecimalFormat d = new DecimalFormat("0.00");
        moneyToBeFormated = moneyToBeFormated.replace(",","");
        String formatedAmount = d.format(Double.parseDouble(moneyToBeFormated));
        return formatedAmount;
    }
    public static String add(String oldAmount, String amountToAdd){
        double sum = Double.parseDouble(oldAmount) + Double.parseDouble(amountToAdd);

        return  FormatMoney(""+sum);
    }
    public static String subtract(String oldAmount,String amountToSubtract){
        double diff = Double.parseDouble(oldAmount) - Double.parseDouble(amountToSubtract);
        return  FormatMoney(""+diff);
    }
}
