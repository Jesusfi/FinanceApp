package com.codegud.financeapp;

public class Envelope {
    private String category;
    private String amount;
    private String goal;
    private int progress;

    public  Envelope(String category, String amount, String goal){
        this.category = category;
        this.amount = amount;
        this.goal = goal;
        this.progress = 0;
    }
    public  Envelope(){

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }
    public String getGoal(){
        return goal;
    }
    public void setGoal(String goal){
        this.goal = goal;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public int getProgress(){
        progress = calculateProgress();
        return progress;
    }
    public void setProgress(int progress){
        this.progress = progress;
    }
    private int calculateProgress(){
        String tempAmount = amount.replace(",","");
        String tempGoal = goal.replace(",","");

        double currentAmount  = Double.parseDouble(tempAmount);
        double currentGoal =  Double.parseDouble(MoneyManager.formatMoneyForCalculations(tempGoal));

        double progress = ((currentAmount*100)/currentGoal);
        return (int)progress;
    }
}
