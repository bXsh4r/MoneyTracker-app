package com.example.moneytracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.MotionEvent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/* TODO:

 */


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private MoneyHandling handleMoney = new MoneyHandling();  // Object of MoneyHandling class
    private DatabaseHelper db; // Object of DataBaseHelper class


    private TextView tv_totalMoney;
    private EditText et_inputMoney;
    private EditText et_description;
    private Button add_btn;
    private Button sub_btn;
    private ImageButton history_btn;
    private TextView tv_result;
    private Spinner spinner;
    private ImageButton addToSpinner_btn;
    private ImageButton removeFromSpinner_btn;
    private ImageButton showAmountEye_btn;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(MainActivity.this);

        // Locks the screen on portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (db.isUserNew()) { // Checks if the user is new
            Intent NewActivityIntent = new Intent(MainActivity.this, NewUser.class);
            startActivity(NewActivityIntent);
            finish(); // deletes the activity from the back button stack after it ends
        }


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
        showAmountEye_btn = findViewById(R.id.showAmountEye_btn);

        et_inputMoney.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;  // A flag that checks if the input is being updated (formatted)
            String formattedAmount;
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 4 && !isUpdating) {  // if the length of input reaches 4 digits or greater, then a comma is inserted and if isUpdating is false (to prevent a stack overflow)
                    isUpdating = true;                 // set to true so if statement doesn't get executed again causing infinite calls
                    formattedAmount = formatAmount(s.toString().replace(",", ""));  // format the amount
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

        tv_totalMoney.setText("****"); // showing this to hide the amount
        handleMoney.setTotalMoney(totalMoney); // Sets the totalMoneyStr variable in the
        // MoneyHandling class to the value of the total money


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAmountOfMoney().isEmpty() || getDescriptionText().isEmpty()) {  // Checks if inputs are empty
                    Toast.makeText(MainActivity.this, "Empty field(s)", Toast.LENGTH_SHORT).show();
                } else {
                    // If "yes" is pressed in the alertDialog the code inside the if statement will be executed
                    confirmationAlertDialog("Are you sure you want to add " + formatAmount(getAmountOfMoney()) + " IQD to your total amount?", result -> {

                        if (result) {
                            String newTextViewText = handleMoney.addMoney(getAmountOfMoney()); // Assigns the object to a String variable for convenience

                            if (newTextViewText.contains("true")) { // Checks if the returned value has "true" indicating a big number, if yes then it displays a warning
                                warningAlertDialog("Can't add this amount");
                                et_inputMoney.setText("");
                                spinner.setSelection(0); // Goes back to the first item
                            } else {
                                insertToHistory("Added", getAmountOfMoney(), getDescriptionText(), getDate());  // calls the method that inserts a new row in the database
                                db.updateTotalAmount(newTextViewText);  // Updates the totalAmount in the database
                                tv_result.setVisibility(View.VISIBLE);
                                tv_result.setTextColor(Color.rgb(60, 179, 52)); // Green
                                tv_result.setText("Added " + formatAmount(getAmountOfMoney()) + " IQD");
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
                    confirmationAlertDialog("Are you sure you want to subtract " + formatAmount(getAmountOfMoney()) + " IQD from your total amount?", result -> {
                        if (result) {
                            String newTextViewText = handleMoney.subMoney(getAmountOfMoney()); // Assigns the object to a String variable for convenience

                            if (newTextViewText.contains("true")) { // Checks if the returned value has "true" indicating a negative number, if yes it displays a warning
                                warningAlertDialog("Can't remove this amount");
                                et_inputMoney.setText("");
                                spinner.setSelection(0); // Goes back to the first item
                            } else {
                                insertToHistory("Removed", getAmountOfMoney(), getDescriptionText(), getDate());
                                db.updateTotalAmount(newTextViewText);
                                tv_result.setVisibility(View.VISIBLE);
                                tv_result.setTextColor(Color.RED);
                                tv_result.setText("Removed " + formatAmount(getAmountOfMoney()) + " IQD");
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

        showAmountEye_btn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String totalMoney = db.getTotalAmount();
                totalMoney = (totalMoney == null) ? "0" : totalMoney;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    tv_totalMoney.setText(formatAmount(totalMoney) + " IQD");
                    return true;
                }else if (event.getAction() == MotionEvent.ACTION_UP  || event.getAction() == MotionEvent.ACTION_CANCEL){
                    tv_totalMoney.setText("****");
                    return true;
                }
                return false;
            }
        });

        db.close();
    }

    // Gets the input typed in the money EditView
    private String getAmountOfMoney() {
        String input = et_inputMoney.getText().toString();
        input = input.replace(",", "");

        if(!input.isEmpty()) {
            return String.valueOf(Integer.parseInt(input)); // format the user input into a normal looking integer. For example if the user enters "01" it will become "1:
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
    // To get date
    private String getDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatterDate);
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
        db.close();
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

        if(!isSuccess){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        db.close();
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

    // a function for formatting money amount by adding commas to make it easier to look at
    public static String formatAmount(String amount){
        boolean hasIQD = false;  // This boolean is to determine if the argument contained the substring " IQD" or not

        if(amount.contains(" IQD")){ // If yes then we remove it otherwise it will break the function
            amount = amount.replace(" IQD", "");
            hasIQD = true; // this is so we can later add it back to the string
        }

        List<String> formattedAmount = new ArrayList<>(Arrays.asList(amount.split(""))); // change the string to an arraylist so we can operate on it

        int index=3; // index 3 is the index where we want the first comma (reading the string from right to left)

        /*Let's assume the amount we want to format is "1523443"
        * we read from right to left so we start the loop from the amount's size which is 7 here
        * we need the indexing from right to left as 0,1,2,3... so we subtract the size from i
        * 7 - 7 = 0
        * 7 - 6 = 1
        * 7 - 5 = 2
        * 7 - 4 = 3 we got 3 which is equal to the index 3
        * we add the comma at i=1 and add the index by 4 so it becomes 7
        * 8 - 3 = 5 (here 7 becomes 8 because we added a comma to the ArrayList so now its size is 8)
        * 8 - 2 = 6
        * 8 - 1 = 7 which is equal to index
        * we add the comma at i=1*/

        for(int i = formattedAmount.size(); i>0; i--){
            if(formattedAmount.size()-i==index){ //
                formattedAmount.add(i, ",");
                index+=4;
            }
        }
        if (hasIQD){
            return String.join("", formattedAmount) + " IQD";
        }else {
            return String.join("", formattedAmount);
        }
    }
}