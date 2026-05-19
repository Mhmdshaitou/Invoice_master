package com.example.invoicemaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserInvoiceAdapter extends RecyclerView.Adapter<UserInvoiceAdapter.UserInvoiceViewHolder> {
    private List<UserInvoice1> userInvoiceList;
    private OnItemClickListener onItemClickListener;

    public UserInvoiceAdapter(List<UserInvoice1> userInvoiceList, OnItemClickListener onItemClickListener) {
        this.userInvoiceList = userInvoiceList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public UserInvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_invoice_item_layout, parent, false);
        return new UserInvoiceViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserInvoiceViewHolder holder, int position) {
        UserInvoice1 userInvoice = userInvoiceList.get(position);
        holder.bind(userInvoice);
    }

    @Override
    public int getItemCount() {
        return userInvoiceList.size();
    }

    public void updateUserInvoiceList(List<UserInvoice1> userInvoices) {
        this.userInvoiceList.clear();
        this.userInvoiceList.addAll(userInvoices);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int userId);
    }

    static class UserInvoiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView emailTextView, invoiceCountTextView;
        int userId;
        private OnItemClickListener onItemClickListener;

        UserInvoiceViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;
            emailTextView = itemView.findViewById(R.id.emailTextView);
            invoiceCountTextView = itemView.findViewById(R.id.invoiceCountTextView);
            itemView.setOnClickListener(this);
        }

        void bind(UserInvoice1 userInvoice) {
            emailTextView.setText(userInvoice.getEmail());
            invoiceCountTextView.setText(String.valueOf(userInvoice.getInvoiceCount()));
            userId = userInvoice.getUserId();
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(userId);
            }
        }
    }
}