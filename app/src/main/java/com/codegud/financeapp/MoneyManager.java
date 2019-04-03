package com.codegud.financeapp;

import java.text.DecimalFormat;

public class MoneyManager {

    public static String formatMoneyForCalculations(String moneyToBeFormated){
        DecimalFormat d = new DecimalFormat("0.00");
        moneyToBeFormated = moneyToBeFormated.replaceAll("[$,]","");
        return d.format(Double.parseDouble(moneyToBeFormated));
    }
    public static String add(String oldAmount, String amountToAdd){
        double sum = Double.parseDouble(oldAmount) + Double.parseDouble(amountToAdd);
        return  formatMoneyForCalculations(""+sum);
    }
    public static String subtract(String oldAmount,String amountToSubtract){
        double diff = Double.parseDouble(oldAmount) - Double.parseDouble(amountToSubtract);
        return  formatMoneyForCalculations(""+diff);
    }
    public static String formatMoneyForDisplay(String moneyToBeFormatted){
        if(moneyToBeFormatted.equals("0.00")){
            return "0.00";
        }
        double amount = Double.parseDouble(moneyToBeFormatted);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);
    }
}
