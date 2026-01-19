package com.example.moneytracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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

/* TODO:
    search history
    fix stuff
    add notes
 */


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    MoneyHandling handleMoney = new MoneyHandling();  // Object of MoneyHandling class
    DatabaseHelper db = new DatabaseHelper(MainActivity.this); // Object of DataBaseHelper class


    TextView tv_totalMoney;
    EditText et_inputMoney;
    EditText et_description;
    Button add_btn;
    Button sub_btn;
    ImageButton history_btn;
    TextView tv_result;
    Spinner spinner;
    ImageButton addToSpinner_btn;
    ImageButton removeFromSpinner_btn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Locks the screen on portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (db.isUserNew()) { // Checks if the user is new
            Intent NewActivityIntent = new Intent(MainActivity.this, NewUser.class);
            startActivity(NewActivityIntent);
        }

        super.onCreate(savedInstanceState);

        // Prevents the back button from going back to the previous intent
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initializing variables
        tv_totalMoney = findViewById(R.id.tv_totalMoney);
        et_inputMoney = findViewById(R.id.et_inputMoney);
        tv_result = findViewById(R.id.tv_result);
        add_btn = findViewById(R.id.add_btn);
        sub_btn = findViewById(R.id.sub_btn);
        history_btn = findViewById(R.id.history_btn);
        et_description = findViewById(R.id.et_description);
        spinner = findViewById(R.id.spinner);
        addToSpinner_btn = findViewById(R.id.addToSpinner_btn);
        removeFromSpinner_btn = findViewById(R.id.removeFromSpinner_btn);


        // Listens for when a spinner item is selected
        spinner.setOnItemSelectedListener(this);


        // Spinner Adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                db.getSpinnerItems()
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);


        String totalMoney = db.getTotalAmount(); // Gets the total amount in the database
        totalMoney = (totalMoney == null) ? "0" : totalMoney; // Makes sure that no null value is assigned

        tv_totalMoney.setText(totalMoney + " IQD"); // Sets the totalMoney TextView to the value from the database
        handleMoney.setTotalMoney(totalMoney); // Sets the totalMoneyStr variable in the
        // MoneyHandling class to the value of the total money


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAmountOfMoney().isEmpty() || getDescriptionText().isEmpty()) {  // Checks if inputs are empty
                    Toast.makeText(MainActivity.this, "Empty field(s)", Toast.LENGTH_SHORT).show();
                } else {
                    // If "yes" is pressed in the alertDialog the code inside the if statement will be executed
                    confirmationAlertDialog("Are you sure you want to add " + getAmountOfMoney() + " IQD to your total amount?", result -> {

                        if (result) {
                            String newTextViewText = handleMoney.addMoney(getAmountOfMoney()); // Assigns the object to a String variable for convenience

                            if (newTextViewText.contains("true")) { // Checks if the returned value has "true" indicating a big number, if yes then it displays a warning
                                warningAlertDialog("That's too much money! Nothing was added to your history");
                                et_inputMoney.setText("");
                                et_description.setText("");
                            } else {
                                insertToHistory("Added", getAmountOfMoney(), getDescriptionText(), getDateAndTime());  // calls the method that inserts a new row in the database
                                db.updateTotalAmount(newTextViewText);  // Updates the totalAmount in the database
                                tv_totalMoney.setText(db.getTotalAmount() + " IQD"); // Updates the total money TextView by getting the total amount from the database
                                tv_result.setVisibility(View.VISIBLE);
                                tv_result.setTextColor(Color.rgb(60, 179, 52)); // Green
                                tv_result.setText("Added " + getAmountOfMoney() + " IQD");
                                et_inputMoney.setText(""); // Sets the EditViews to "" so what the user typed gets removed
                                et_description.setText("");
                                spinner.setSelection(0); // Goes back to the first item
                                tv_result.postDelayed(new Runnable() { // A Runnable so the message can stay for 1500ms then disappears
                                    @Override
                                    public void run() {
                                        tv_result.setVisibility(View.GONE);
                                    }
                                }, 1500);
                            }
                        }
                    });
                }
            }
        });

        sub_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAmountOfMoney().isEmpty() || getDescriptionText().isEmpty()) {  // Checks if inputs are empty
                    Toast.makeText(MainActivity.this, "Empty field(s)", Toast.LENGTH_SHORT).show();
                } else {
                    confirmationAlertDialog("Are you sure you want to subtract " + getAmountOfMoney() + " IQD from your total amount?", result -> {
                        if (result) {
                            String newTextViewText = handleMoney.subMoney(getAmountOfMoney()); // Assigns the object to a String variable for convenience

                            if (newTextViewText.contains("true")) { // Checks if the returned value has "true" indicating a negative number, if yes it displays a warning
                                warningAlertDialog("Can't have negative money! Nothing was added to your history");
                                et_inputMoney.setText("");
                                et_description.setText("");
                            } else {
                                insertToHistory("Removed", getAmountOfMoney(), getDescriptionText(), getDateAndTime());
                                db.updateTotalAmount(newTextViewText);
                                tv_totalMoney.setText(db.getTotalAmount() + " IQD");
                                tv_result.setVisibility(View.VISIBLE);
                                tv_result.setTextColor(Color.RED);
                                tv_result.setText("Removed " + getAmountOfMoney() + " IQD");
                                et_inputMoney.setText(""); // Sets the EditViews to "" so what the users typed gets removed
                                et_description.setText("");
                                spinner.setSelection(0); // Goes back to the first item
                                tv_result.postDelayed(new Runnable() {  // A Runnable so the message can stay for 1500ms then disappears
                                    @Override
                                    public void run() {
                                        tv_result.setVisibility(View.GONE);
                                    }
                                }, 1500);
                            }
                        }
                    });
                }
            }
        });

        history_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        addToSpinner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerAddAlertDialog();
            }
        });

        removeFromSpinner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemSelected = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
                spinnerRemoveAlertDialog(result -> {
                    if (result) {
                        db.deleteFromSpinner(itemSelected);
                        Toast.makeText(MainActivity.this, "Removed " + itemSelected, Toast.LENGTH_SHORT).show();
                        // Resets the spinner adapter by making and setting a new one
                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                db.getSpinnerItems()
                        );
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerAdapter);
                    }
                });
            }
        });
    }

    // Gets the input typed in the money EditView
    private String getAmountOfMoney() {
        String input = et_inputMoney.getText().toString();

        if(!input.isEmpty()) {
            return input;
        }else{
            return "";
        }
    }

    // Gets the input typed in the description
    private String getDescriptionText(){
        String input = et_description.getText().toString().trim();

        if(!input.isEmpty()){
            return input;
        }else{
            return "";
        }
    }
    // To get date and time
    private String getDateAndTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("hh:mm a");
        return dateTime.format(formatterDate) + " at " + dateTime.format(formatterTime);
    }

    // Creates an AlertDialog for input confirmation
    private void confirmationAlertDialog(String message, Consumer<Boolean> callback) {
        new AlertDialog.Builder(this)
                .setTitle("ALERT!")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> callback.accept(true))
                .setNegativeButton("No", (dialog, which) -> callback.accept(false))
                .show();
    }

    // Creates an AlertDialog for warning messages
    private void warningAlertDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("WARNING!")
                .setMessage(message)
                .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Creates an AlertDialog for input text to the spinner
    private void spinnerAddAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to list");
        builder.setMessage("Enter Item:");

        EditText input = new EditText(this);
        input.setHint("Description...");
        input.setTextColor(Color.WHITE);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Specifies the input type
        input.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(15) // Sets filters to limit the length of the input text
        });

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString().trim();

                if(inputText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Nothing added", Toast.LENGTH_SHORT).show();
                }
                else if(db.getSpinnerItems().contains(inputText)){
                    Toast.makeText(MainActivity.this, "Can't have duplicates", Toast.LENGTH_SHORT).show();
                }
                else if(db.addToSpinner(inputText)){
                    Toast.makeText(MainActivity.this, "Added to list", Toast.LENGTH_SHORT).show();

                    // Resets the spinner adapter by making and setting a new one
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                            MainActivity.this,
                            android.R.layout.simple_spinner_item,
                            db.getSpinnerItems()
                    );
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerAdapter);
                }else{
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // AlertDialog to remove a spinner item
    private void spinnerRemoveAlertDialog(Consumer<Boolean> callback){
        String itemSelected = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
        if(itemSelected.equals("Other") || itemSelected.equals("Description:")){
            Toast.makeText(MainActivity.this, "Can't remove this", Toast.LENGTH_SHORT).show();
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("WARNING!")
                    .setMessage("Are you sure you want to remove " + spinner.getItemAtPosition(spinner.getSelectedItemPosition()) + " from the list?")
                    .setPositiveButton("Yes", (dialog, which) -> callback.accept(true))
                    .setNegativeButton("No", (dialog, which) -> callback.accept(false))
                    .show();
        }
    }

    private void insertToHistory(String operation, String amount, String description, String date){
        boolean isSuccess = db.insertToDataBase(operation, amount, description, date);

        if(isSuccess){
            Toast.makeText(this, "Added to history", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position == 0){
            et_description.setVisibility(View.INVISIBLE);
        }
        else if(parent.getItemAtPosition(position).toString().equals("Other")){
            et_description.setVisibility(View.VISIBLE);
            et_description.setText("");

        }else{
            et_description.setVisibility(View.INVISIBLE);
            et_description.setText(parent.getItemAtPosition(position).toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}