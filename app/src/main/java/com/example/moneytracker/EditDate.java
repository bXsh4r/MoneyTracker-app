package com.example.moneytracker;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.time.LocalDate;

class EditDate {

    private boolean isDeleting; // A flag to check if a char is being deleted
    private boolean formatText; // A flag to determine when to format the input
    private boolean isUpdating; // A flag to show that the text is being updated (formatted)

    public void watchText(EditText et_date){
        isDeleting = false;
        formatText = false;
        isUpdating = false;

        et_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 3 || s.length() == 6){               // if length is 3 or 6
                    if (!isDeleting) {                                 // and the user is not deleting (pressing backspace)
                        s.insert(s.length()-1, "/");    // insert "/" at index 2 or 5
                    }
                }

                if (formatText && !isUpdating){                        // if the text is max length (10) and is not updating then its eligible to be formatted
                    isUpdating = true;                                 // set isUpdating to true so it doesn't update accidentally
                    s = checkInput(s);                                 // format the input and update it
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;                            // if the length of characters replaced (count) is greater than the length of the characters added/removed (count) then set isDeleting to true
                                                                       // say we have an (s) length of 5, the cursor is at the char at index 4 (start), we press the backspace button on the keyboard...
                                                                       // (count) is now 1 because one char has been changed (removed) and after is 0 because no new char was added
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                formatText = s.length() == 10;                         // if the length of the input is 10 then formatting is allowed
            }
        });
    }

    private Editable checkInput(Editable s){

        // checks if the input format is okay...
        for (int i=0; i<s.length(); i++){
            if ((i == 2 || i == 5) && s.charAt(i) == '/'){continue;}                                        // checks if indexes 2 and 5 are '/'
            else if (((i == 2 || i == 5) && !(s.charAt(i) == '/')) || !Character.isDigit(s.charAt(i))){     // if not then delete the input, or if one of the indexes other than 2 and 5 is not a digit then delete input
                isUpdating = false; // set isUpdating to false so the input is checked again when it reaches length 10
                s.clear();          // clear the input
                return s;           // return
            }
        }


        // format the date digits if they're too big or too small...

        int month = Integer.parseInt(s.toString().substring(3, 5));
        int day = Integer.parseInt(s.toString().substring(0, 2));
        int year = Integer.parseInt(s.toString().substring(6));
        int formattedMonth;
        int formattedYear;
        int monthType;


        if(month > 12){
            s.replace(3, 5, "12");
            formattedMonth = 12;
        }else if(month < 1){
            s.replace(3, 5, "01");
            formattedMonth = 0;
        }else{
            formattedMonth = month;
        }

        if(year > LocalDate.now().getYear() + 5 || year < 1900){
            s.replace(6, 10, LocalDate.now().getYear()+"");
            formattedYear = LocalDate.now().getYear();
        }else {
            formattedYear = year;
        }


        monthType = monthType(formattedMonth, formattedYear);


        if (day > 28){
            if (day > 31 && monthType == 31){
                s.replace(0, 2, "31");}
            else if (day > 30 && monthType == 30){
                s.replace(0, 2, "30");}
            else if (monthType == 28){
                s.replace(0, 2, "28");}
            else if (monthType == 29){
                s.replace(0, 2, "29");}
        } else if (day < 1) {
            s.replace(0, 2, "01");
        }

        isUpdating = false;
        return s;
    }

    private int monthType(int month, int year){
        switch (month){
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31; // 31 days

            case 4:
            case 6:
            case 9:
            case 11:
                return 30; // 30 days

            case 2:
                return yearType(year); // check year and return number of days in feb

            default:
                return -1;
        }
    }

    private int yearType(int year){
        if(LocalDate.of(year, 1, 1).isLeapYear()){return 29;}
        return 28;
    }
}