package com.example.invoicemaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invoicemaster.StockOperation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StockOperationAdapter extends RecyclerView.Adapter<StockOperationAdapter.StockOperationViewHolder> {
    private List<StockOperation> stockOperationList;

    // Constructor
    public StockOperationAdapter(List<StockOperation> stockOperationList) {
        this.stockOperationList = stockOperationList;
    }

    @NonNull
    @Override
    public StockOperationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_operation_item, parent, false);
        return new StockOperationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockOperationViewHolder holder, int position) {
        StockOperation stockOperation = stockOperationList.get(position);
        holder.bind(stockOperation);
    }

    @Override
    public int getItemCount() {
        return stockOperationList.size();
    }

    // ViewHolder class
    public static class StockOperationViewHolder extends RecyclerView.ViewHolder {
        private TextView operationTypeTextView;
        private TextView quantityTextView;
        private TextView currentQuantityTextView;
        private TextView operationDateTextView;

        public StockOperationViewHolder(@NonNull View itemView) {
            super(itemView);
            operationTypeTextView = itemView.findViewById(R.id.operation_type_text_view);
            quantityTextView = itemView.findViewById(R.id.quantity_text_view);
            currentQuantityTextView = itemView.findViewById(R.id.current_quantity_text_view);
            operationDateTextView = itemView.findViewById(R.id.operation_date_text_view);
        }

        public void bind(StockOperation stockOperation) {
            String operationType = stockOperation.getOperationType();

            // Translate "out" and "add" to French
            String operationTypeInFrench = operationType.equals("out") ? "Sortie" : "Entreé";

            operationTypeTextView.setText(operationTypeInFrench);
            String quantityPrefix = operationType.equals("out") ? "-" : "";
            quantityTextView.setText("Quantité: " + quantityPrefix + String.valueOf(stockOperation.getQuantity()));
            currentQuantityTextView.setText("Quantité actuelle: " + String.valueOf(stockOperation.getCurrentQty()));
            operationDateTextView.setText("Date: " + formatDate(stockOperation.getOperationDate()));

            // Change the color of the operation type text based on the operation type
            if (operationType.equals("out")) {
                operationTypeTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                operationTypeTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

            // Change the color of the quantity text based on the operation type
            if (operationType.equals("out")) {
                quantityTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
            } else {
                quantityTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }
        }

        // Method to format date without hours, minutes, and seconds
        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(date);
        }
    }
}
