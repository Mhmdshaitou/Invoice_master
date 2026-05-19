package com.example.invoicemaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {

    private List<Invoice> invoiceList;
    private OnInvoiceClickListener listener;

    public InvoiceAdapter(List<Invoice> invoiceList, OnInvoiceClickListener listener) {
        this.invoiceList = invoiceList;
        this.listener = listener;
    }

    public interface OnInvoiceClickListener {
        void onInvoiceClick(String invoiceId, String status);
    }


    public void updateList(List<Invoice> newList) {
        invoiceList.clear();
        invoiceList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.bind(invoice);
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView invoiceIdTextView;
        private TextView clientNameView;
        private TextView dateTextView;
        private TextView statusTextView;
        private TextView invoiceAmountView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            invoiceIdTextView = itemView.findViewById(R.id.tvInvoiceNumber);
            clientNameView = itemView.findViewById(R.id.tvClientName);
            dateTextView = itemView.findViewById(R.id.tvInvoiceDate);
            statusTextView = itemView.findViewById(R.id.tvInvoiceStatus);
            invoiceAmountView = itemView.findViewById(R.id.tvInvoiceAmount);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Invoice clickedInvoice = invoiceList.get(position);
                listener.onInvoiceClick(clickedInvoice.getInvoiceId(), clickedInvoice.getStatus());
            }
        }


        public void bind(Invoice invoice) {
            invoiceIdTextView.setText("#INV00" + invoice.getInvoiceId());
            clientNameView.setText(invoice.getclientName());
            dateTextView.setText(invoice.getDate());

            double totalAmount = Double.parseDouble(invoice.getTotalAmount());
            String formattedTotalAmount;

            if (totalAmount == (long) totalAmount) {
                // Total amount is a whole number, format without decimal places
                formattedTotalAmount = String.format("%,.0f CFA", totalAmount);
            } else {
                // Total amount has decimals, format with two decimal places
                formattedTotalAmount = String.format("%,.2f CFA", totalAmount);
            }

            String formattedAmount = formattedTotalAmount;

            invoiceAmountView.setText(formattedAmount);
            if (invoice.getStatus().equalsIgnoreCase("Paid")) {
                // If the status is Paid, show tvInvoiceStatus2 and hide tvInvoiceStatus
                statusTextView.setVisibility(View.GONE); // Assuming statusTextView is tvInvoiceStatus
                itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.VISIBLE);
                ((TextView)itemView.findViewById(R.id.tvInvoiceStatus2)).setText("validé");
            } else if (invoice.getStatus().equalsIgnoreCase("Unpaid")) {
                // If the status is Unpaid, show tvInvoiceStatus and hide tvInvoiceStatus2
                statusTextView.setVisibility(View.VISIBLE); // Assuming statusTextView is tvInvoiceStatus
                statusTextView.setText("non validé");
                itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.GONE);
            } else {
                // If the status is neither Paid nor Unpaid, you can choose to hide both or handle accordingly
                statusTextView.setVisibility(View.GONE);
                itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.GONE);
            }
        }
    }
}
