package com.example.invoicemaster;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvoiceAdapter2 extends RecyclerView.Adapter<InvoiceAdapter2.ViewHolder> {

    private List<Invoice> invoiceList;
    private OnInvoiceClickListener listener;

    public InvoiceAdapter2(List<Invoice> invoiceList, OnInvoiceClickListener listener) {
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
                formattedTotalAmount = String.format("%,.0f CFA", totalAmount);
            } else {
                formattedTotalAmount = String.format("%,.2f CFA", totalAmount);
            }

            invoiceAmountView.setText(formattedTotalAmount);

            // Reset visibility for both status TextViews
            statusTextView.setVisibility(View.GONE);
            itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.GONE);

            // Log the status for debugging
            Log.d("InvoiceAdapter2", "Status: " + invoice.getStatus());

            // Handle status
            if (invoice.getStatus().equalsIgnoreCase("Paid")) {
                itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.VISIBLE);
                ((TextView) itemView.findViewById(R.id.tvInvoiceStatus2)).setText("validé");
            } else if (invoice.getStatus().equalsIgnoreCase("Unpaid")) {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText("non validé");
            } else {
                // If status is neither Paid nor Unpaid
                statusTextView.setVisibility(View.GONE);
                itemView.findViewById(R.id.tvInvoiceStatus2).setVisibility(View.GONE);
            }
        }

    }
}
