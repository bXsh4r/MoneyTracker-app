package com.example.moneytracker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
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
        holder.tv_opAndAmount.setText(historyList.get(position).getOperation() + ": " + historyList.get(position).getAmount());
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

        holder.editHistoryDesc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if(adapterPosition != RecyclerView.NO_POSITION){
                    // gets the id and pass it the method with the adapter position
                    editDescAlertDialog(historyList.get(adapterPosition).getId(), adapterPosition, result -> {
                        if(result){
                            notifyItemChanged(adapterPosition);
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
        ImageButton editHistoryDesc_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_opAndAmount = itemView.findViewById(R.id.tv_opAndAmount);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_date = itemView.findViewById(R.id.tv_date);
            layout_history = itemView.findViewById(R.id.layout_history);
            editHistoryDesc_btn = itemView.findViewById(R.id.editHistoryDesc_btn);
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

    // Updating description from the database
    private void editDescAlertDialog(int id, int adapterPosition, Consumer<Boolean> callback) {
        DatabaseHelper db = new DatabaseHelper(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Description");
        builder.setMessage("Enter new description:");

        EditText input = new EditText(context);
        input.setHint("Description...");
        input.setTextColor(Color.WHITE);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(35)
        });

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String inputText = input.getText().toString().trim();

            if (inputText.isEmpty()) {
                Toast.makeText(context, "Nothing added", Toast.LENGTH_SHORT).show();
                callback.accept(false);
            } else {
                if (db.updateHistoryDesc(id, inputText)) { // if updated
                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                    historyList.get(adapterPosition).setDescription(inputText); // update the list as well
                    callback.accept(true);
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    callback.accept(false);
                }

            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            callback.accept(false);
        });

        builder.show();
    }
}
