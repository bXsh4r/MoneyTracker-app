package com.example.moneytracker;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

        // Search View
        searchView = findViewById(R.id.sc_historySearchView);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text); // finds the Id of the search view EditText
                                                                                                // by searching the resources of androidx.appcompat and finding the EditText id
                                                                                            // which is search_src_text
        searchEditText.setHintTextColor(getColor(R.color.light_gray));
        searchView.setQueryHint("Search description...");
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // This is for executing code upon submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // This is for when the search bar's text change
                filteredList(newText); // When search text changes the new text is sent to the filteredList method
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

    private void filteredList(String text) {
        List<HistoryData> filteredList = new ArrayList<>();

        for(HistoryData description : db.getHistory()){
            if(description.getDescription().toLowerCase().contains(text.toLowerCase())){ // if the searched item is in the database then add it to the filteredList
                filteredList.add(description);
            }
        }

        mAdapter = new MyAdapter(filteredList, HistoryActivity.this); // update the recycler view with filteredList items
        recyclerView.setAdapter(mAdapter);
        db.close();
    }
}