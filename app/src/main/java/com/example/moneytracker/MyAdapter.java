package com.example.moneytracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                            db.delete(historyList.get(adapterPosition).getId()); // First delete from the database
                            historyList.remove(adapterPosition); // Then from the list
                            notifyItemRemoved(adapterPosition); // Then notify the recycler view
                            db.close();
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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_opAndAmount = itemView.findViewById(R.id.tv_opAndAmount);
            tv_description = itemView.findViewById(R.id.tv_description);
            tv_date = itemView.findViewById(R.id.tv_date);
            layout_history = itemView.findViewById(R.id.layout_history);
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
}
