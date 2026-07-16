package com.bxsh4r.moneytracker;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SearchView searchView;
    private ImageButton searchViewHelp_btn;
    private TextView tv_searchResult; // The text that appears if the recyclerView is empty
    private TextView tv_calculatedAmountRemoved;
    private TextView tv_calculatedAmountAdded;


    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Locks the screen on portrait mode
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(HistoryActivity.this); // DatabaseHelper class to use the getHistory method

        tv_calculatedAmountRemoved = findViewById(R.id.tv_calculatedAmountRemoved);
        tv_calculatedAmountAdded = findViewById(R.id.tv_calculatedAmountAdded);

        tv_searchResult = findViewById(R.id.tv_searchResult);

        if(db.getHistory().isEmpty()){
            tv_searchResult.setVisibility(View.VISIBLE);
            tv_searchResult.setText("List is empty...");
        }

        // Help ImageButton
        searchViewHelp_btn = findViewById(R.id.searchViewHelp_btn);
        searchViewHelp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpAlertDialog();
            }
        });

        // Search View
        searchView = findViewById(R.id.sv_historySearchView);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text); // finds the Id of the search view EditText
                                                                                                // by searching the resources of androidx.appcompat and finding the EditText id
                                                                                            // which is search_src_text
        searchEditText.setHintTextColor(getColor(R.color.light_gray));
        searchView.setQueryHint("Search here...");
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // This is for executing code upon submission
                filteredList(query, View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // This is for when the search bar's text change
                filteredList(newText, View.INVISIBLE); // When search text changes the new text is sent to the filteredList method
                return true;
            }
        });

        // Recycler View
        recyclerView = findViewById(R.id.rv_historyRecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(db.getHistory(), HistoryActivity.this); // getHistory method from DatabaseHelper class
        recyclerView.setAdapter(mAdapter);

        db.close();
    }

    private void filteredList(String text, int visibility) {
        List<HistoryData> filteredList = new ArrayList<>();
        int amountRemoved = 0;
        int amountAdded = 0;
        tv_searchResult.setVisibility(View.GONE);

        for(HistoryData item : db.getHistory()){

            if(text.isEmpty()){ // if no text in search bar add all items to the filteredList to display them
                filteredList.add(item);
                amountRemoved = 0;
                amountAdded = 0;
            }
            else if(item.getDescription().toLowerCase().contains(text.toLowerCase())){ // if the searched item description is in the database then add it to the filteredList
                filteredList.add(item);

                if(item.getOperation().equals("Removed")) {
                    amountRemoved += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }else{
                    amountAdded += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }

            }
            else if(item.getDate().contains(text)){ // if the searched item is a date
                filteredList.add(item);

                if(item.getOperation().equals("Removed")) {
                    amountRemoved += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }else{
                    amountAdded += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }
            }
            else if((item.getDate() + " " + item.getDescription().toLowerCase()).contains(text.toLowerCase())){ // if the searched item is date + description
                filteredList.add(item);

                if(item.getOperation().equals("Removed")) {
                    amountRemoved += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }else{
                    amountAdded += Integer.parseInt(item.getAmount().replace(" IQD", ""));
                }
            }
        }

        if(filteredList.isEmpty()){
            tv_searchResult.setVisibility(View.VISIBLE);
            tv_searchResult.setText("Nothing found...");
        }

        mAdapter = new MyAdapter(filteredList, HistoryActivity.this); // update the recycler view with filteredList items
        recyclerView.setAdapter(mAdapter);
        db.close();
        // only make the TextView visible when user submits text (check onQueryTextSubmit method)
        tv_calculatedAmountRemoved.setVisibility(visibility);
        tv_calculatedAmountAdded.setVisibility(visibility);
        tv_calculatedAmountRemoved.setText("Spent: " + MainActivity.formatAmount(amountRemoved + " IQD"));
        tv_calculatedAmountAdded.setText("Received: " + MainActivity.formatAmount(amountAdded + " IQD"));
    }

    // to show users how to search properly
    private void helpAlertDialog(){
        new AlertDialog.Builder(this)
                .setTitle("How to search...")
                .setMessage("To search for a description such as: electricity. Only enter electricity in the search bar.\n\n" +
                        "To search using date such as 23/1/2026. Enter the date in this format 23/01/2026\n" +
                        "for all data in a specific month such as january enter /01 or /01/2026 for month and year.\n\n" +
                        "You can also search for date and description like this:\n" +
                        "23/01/2026 electricity (the space between them is important)\n" +
                        "for all data in a specific month: /01/2026 electricity.\n\n" +
                        "After finding desired results press the search button from your keyboard to get total amount spent and received.")
                .setNeutralButton("Ok", (dialog, which) -> dialog.dismiss())
                .show();
    }
}