package com.bxsh4r.moneytracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NewUser extends AppCompatActivity {

    private DatabaseHelper db;

    private EditText et_getTotalMoney;
    private Button ok_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(NewUser.this);

        // Locks the screen on portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_time);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initializing variables
        et_getTotalMoney = findViewById(R.id.et_getTotalMoney);
        ok_btn = findViewById(R.id.ok_btn);

        et_getTotalMoney.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;  // A flag that checks if the input is being updated (formatted)
            String formattedAmount;
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 4 && !isUpdating) {  // if the length of input reaches 4 digits or greater, then a comma is inserted and if isUpdating is false (to prevent a stack overflow)
                    isUpdating = true;                 // set to true so if statement doesn't get executed again causing infinite calls
                    formattedAmount = MainActivity.formatAmount(s.toString().replace(",", ""));  // format the amount
                    s.replace(0, s.length(), formattedAmount);
                }else {
                    isUpdating = false;                // The reason isUpdating is used is assuming the user input length is 4. the is statement becomes true and is executed. a comma is added. and the length becomes 5
                    // if the isUpdating flag is not used then the if-statement will be executed again. and again. and again until a stack overflow.
                    // so the isUpdating flag is used to prevent this by becoming true after the first comma. so the second call to this function will directly go to the else-statement and isUpdating becomes false again.
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });


        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTotalMoney().isEmpty()) {
                    Toast.makeText(NewUser.this, "Empty field", Toast.LENGTH_SHORT).show();
                } else {
                    createAlertDialog("You won't be able to change this value (" + MainActivity.formatAmount(getTotalMoney()) + " IQD) again!", result -> {
                        if (result) {

                            // Inserts input value to the database and returns a boolean indicating a success or a failure
                            boolean isSuccess = db.insertToDataBase("Added", getTotalMoney(), "Your total " +
                                    "money when you first used the app", getDate());
                            if (isSuccess) {
                                Toast.makeText(NewUser.this, "Added to history", Toast.LENGTH_SHORT).show();
                                db.updateUserStatus(0);  // updates status to zero meaning false so user isn't new anymore
                                db.updateTotalAmount(getTotalMoney());
                            } else {
                                Toast.makeText(NewUser.this, "Error", Toast.LENGTH_SHORT).show();
                            }

                            // Intent to the MainActivity
                            Intent intent = new Intent(NewUser.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // deletes the activity from the back button stack after it ends
                        }
                    });
                }
            }
        });
        db.close();
    }

    // Gets the date of when it's called
    private String getDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatterDate);
    }

    // Gets the number(String) from the edit view
    private String getTotalMoney() {
        String input = et_getTotalMoney.getText().toString();
        input = input.replace(",", "");

        if(!input.isEmpty()) {
            return String.valueOf(Integer.parseInt(input)); // format the user input into a normal looking integer. For example if the user enters "01" it will become "1:
        }else{
            return "";
        }
    }

    private void createAlertDialog(String message, Consumer<Boolean> callback){
        new AlertDialog.Builder(this)
                .setTitle("ALERT!!!")
                .setMessage(message)
                .setPositiveButton("That's all I got", (dialog, which) -> callback.accept(true))
                .setNegativeButton("No wait", (dialog, which) -> callback.accept(false))
                .show();
    }
}