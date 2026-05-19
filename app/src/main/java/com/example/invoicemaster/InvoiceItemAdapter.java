package com.example.invoicemaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceItemAdapter extends RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder> {

    private List<InvoiceItem> invoiceItems;
    private OnItemClickListener listener;



    public interface DataChangeListener {
        void onDataChanged();
    }
    private DataChangeListener dataChangeListener;
    // Include DataChangeListener as a parameter
    public InvoiceItemAdapter(List<InvoiceItem> invoiceItems, OnItemClickListener listener, DataChangeListener dataChangeListener) {
        this.invoiceItems = invoiceItems;
        this.listener = listener;
        this.dataChangeListener = dataChangeListener; // Now correctly assigns the passed listener
    }
    public List<InvoiceItem> getItems() {
        return invoiceItems;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceItem item = invoiceItems.get(position);

        // Bind the data
        holder.itemDescriptionText.setText(item.getItemName());
        String unit = item.getUnit();
        double price = getUnitPrice(item, unit);

        String pricePerUnitText;
        String itemTotalPriceText;

// Format price per unit text
        if (price == (long) price) {
            // Price is a whole number, format without decimal places
            pricePerUnitText = String.format(Locale.US, "%,d CFA", (long) price) + " x " + item.getQuantity() + " " + unit;
        } else {
            // Price has decimals, format with decimal places
            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            pricePerUnitText = formatter.format(price) + " CFA x " + item.getQuantity() + " " + unit;
        }

// Set the formatted price per unit text
        holder.itemPricePerUnitText.setText(pricePerUnitText);

// Format total price text
        if (item.getInvoiceItemTotal() == (long) item.getInvoiceItemTotal()) {
            // Total price is a whole number, format without decimal places
            itemTotalPriceText = String.format(Locale.US, "%,d CFA", (long) item.getInvoiceItemTotal());
        } else {
            // Total price has decimals, format with decimal places
            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            itemTotalPriceText = formatter.format(item.getInvoiceItemTotal()) + " CFA";
        }

// Set the formatted total price text
        holder.itemTotalPriceText.setText(itemTotalPriceText);

        // Set onClickListeners
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item.getInvoiceItemId()));
        holder.itemOptionsButton.setOnClickListener(v -> showPopupMenu(v, item, position));
    }

    @Override
    public int getItemCount() {
        return invoiceItems.size();
    }

    public void setItems(List<InvoiceItem> items) {
        this.invoiceItems = items;
        notifyDataSetChanged();
    }

    private double getUnitPrice(InvoiceItem item, String unit) {
        switch (unit) {
            case "PCS":
                return item.getItemPrice();
            case "PQT":
                return item.getItemPacketPrice();
            default:
                return item.getItemCartonPrice();
        }
    }

    private void showPopupMenu(View view, InvoiceItem item, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.popup_menu);
        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_delete) {
                listener. onDeleteClick(item.getInvoiceItemId(), position);
                return true;
            }
            return false;
        });
        popup.show();
    }
    public void removeAt(int position) {
        invoiceItems.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, invoiceItems.size());
        if (invoiceItems.isEmpty() && dataChangeListener != null) {
            dataChangeListener.onDataChanged();
        }
    }


    public interface OnItemClickListener {
        void onItemClick(int invoiceItemId);
        void  onDeleteClick(int invoiceItemId, int position);

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemDescriptionText, itemPricePerUnitText, itemTotalPriceText;
        ImageView itemOptionsButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemDescriptionText = itemView.findViewById(R.id.itemDescriptionText);
            itemPricePerUnitText = itemView.findViewById(R.id.itemPricePerUnitText);
            itemTotalPriceText = itemView.findViewById(R.id.itemTotalPriceText);
            itemOptionsButton = itemView.findViewById(R.id.itemOptionsButton);
        }
    }
}
