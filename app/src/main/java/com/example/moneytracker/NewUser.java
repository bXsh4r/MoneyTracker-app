package com.example.moneytracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NewUser extends AppCompatActivity {

    DatabaseHelper db = new DatabaseHelper(NewUser.this);

    EditText et_getTotalMoney;
    Button ok_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Locks the screen on portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);

        // Prevents the back button from going back to the previous intent
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

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


        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTotalMoney().isEmpty()) {
                    Toast.makeText(NewUser.this, "Empty field", Toast.LENGTH_SHORT).show();
                } else {
                    createAlertDialog("You won't be able to change this value (" + getTotalMoney() + " IQD) again!", result -> {
                        if (result) {

                            // Inserts input value to the database and returns a boolean indicating a success or a failure
                            boolean isSuccess = db.insertToDataBase("Added", getTotalMoney(), "This was your total " +
                                    "money when you first used the app", getDateAndTime());
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

                        }
                    });
                }
            }
        });
    }

    // Gets the date and time of when it's called
    private String getDateAndTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm a");
        return dateTime.format(formatterDate) + " at " + dateTime.format(formatterTime);
    }

    // Gets the number(String) from the edit view
    private String getTotalMoney(){
        String totalMoneyInput = et_getTotalMoney.getText().toString();
        if(!totalMoneyInput.isEmpty()){
            return totalMoneyInput;
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