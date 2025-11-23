package com.example.moneytracker;

public class MoneyHandling {


    private String totalMoneyStr; // A string representation of the total amount of money

    // Adds the money
    public String addMoney(String valueStr){
        int value = Integer.parseInt(valueStr);
        int totalMoney = Integer.parseInt(totalMoneyStr);

        totalMoney+=value;

        // If the calculated value is greater than 999,999,999 then it returns "999,999,999" with "true" to indicate that it's too big
        if(totalMoney > 999999999){
            totalMoney = Integer.parseInt(totalMoneyStr); // Reassigns back to the total value
            totalMoneyStr = totalMoney + "";
            return totalMoneyStr + " true";
        }

        totalMoneyStr = totalMoney + "";
        return totalMoneyStr;
    }

    // Subtracts the money
    public String subMoney(String valueStr){
        int value = Integer.parseInt(valueStr);
        int totalMoney = Integer.parseInt(totalMoneyStr);

        totalMoney -= value;

        // If the calculated value is less than 0 then it returns 0 with "true" to indicate that it's a negative
        if(totalMoney < 0){
            totalMoney = Integer.parseInt(totalMoneyStr); // Reassigns back to the total value
            totalMoneyStr = totalMoney + "";
            return totalMoneyStr + " true";
        }

        totalMoneyStr = totalMoney + "";
        return totalMoneyStr;
    }

    // Sets the global variable to an amount
    public void setTotalMoney(String valueStr){
        totalMoneyStr = valueStr;
    }
}
