package com.example.moneytracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.function.Consumer;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    List<HistoryData> historyList;
    Context context;
    public MyAdapter(List<HistoryData> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        holder.tv_opAndAmount.setText(historyList.get(position).getOperation() + ": " + MainActivity.formatAmount(historyList.get(position).getAmount()));
        holder.tv_description.setText(historyList.get(position).getDescription());
        holder.tv_date.setText(historyList.get(position).getDate());
        holder.layout_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getBindingAdapterPosition(); // Gets the current position
                if (adapterPosition != RecyclerView.NO_POSITION) { // Makes sure that the position is valid
                    deletionAlertDialog(result -> {
                        if (result) {
                            DatabaseHelper db = new DatabaseHelper(context);
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                            db.deleteHistory(historyList.get(adapterPosition).getId()); // First delete from the database
                            historyList.remove(adapterPosition); // Then from the list
                            notifyItemRemoved(adapterPosition); // Then notify the recycler view
                            db.close();
                        }
                    });
                }
            }
        });

        holder.editHistory_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if(adapterPosition != RecyclerView.NO_POSITION){
                    // gets the id and pass it the method with the adapter position
                    editAlertDialog(historyList.get(adapterPosition).getId(), adapterPosition, historyList.get(adapterPosition).getDescription(), historyList.get(adapterPosition).getDate(), result -> {
                        if(result){
                            historyList.sort(HistoryData.dateComparator); // sort the list
                            notifyDataSetChanged(); // notify that the items have changed (position and contents)
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_opAndAmount;
        TextView tv_description;
        TextView tv_date;
        ConstraintLayout layout_history;
        ImageButton editHistory_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_opAndAmount = itemView.findViewById(R.id.tv_opAndAmount);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_date = itemView.findViewById(R.id.tv_date);
            layout_history = itemView.findViewById(R.id.layout_history);
            editHistory_btn = itemView.findViewById(R.id.editHistory_btn);
        }
    }

    // Creates an AlertDialog for deletion confirmation
    private void deletionAlertDialog(Consumer<Boolean> callback) {
        new AlertDialog.Builder(context)
                .setTitle("ALERT!!!")
                .setMessage("Are you sure you want to delete this?")
                .setPositiveButton("Yes", (dialog, which) -> callback.accept(true))
                .setNegativeButton("No", (dialog, which) -> callback.accept(false))
                .show();
    }

    // Updating description and date in the recycler view list
    private void editAlertDialog(int id, int adapterPosition, String oldDesc, String oldDate, Consumer<Boolean> callback) {
        DatabaseHelper db = new DatabaseHelper(context);
        EditDate editDate = new EditDate();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context); // A LayoutInflater is used here to take the XML file for the alertDialog I made. to get its View so I can use it programmatically
        View dialogView = inflater.inflate(R.layout.two_input_dialog_box, null);

        builder.setTitle("Edit");

        EditText newDesc = dialogView.findViewById(R.id.input_one);
        EditText newDate = dialogView.findViewById(R.id.input_two);

        editDate.watchText(newDate);

        newDesc.setHint(oldDesc);
        newDesc.setTextColor(Color.WHITE);

        newDate.setHint(oldDate);
        newDate.setTextColor(Color.WHITE);

        builder.setView(dialogView);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newDescString = newDesc.getText().toString().trim();
            String newDateString = newDate.getText().toString().trim();

            newDescString = newDescString.isEmpty() ? oldDesc : newDescString;
            newDateString = newDateString.isEmpty() || (newDateString.length() < 10) ? oldDate : newDateString;

                if (db.updateHistory(id, newDescString, newDateString)) { // if updated
                    historyList.get(adapterPosition).setDescription(newDescString); // update the RecyclerView as well
                    historyList.get(adapterPosition).setDate(newDateString);
                    callback.accept(true);
                } else {
                    callback.accept(false);
                }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            callback.accept(false);
        });

        db.close();
        builder.show();

    }
}
