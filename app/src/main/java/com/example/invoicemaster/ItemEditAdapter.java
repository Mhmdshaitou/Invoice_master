package com.example.invoicemaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invoicemaster.fragments.ItemsFragment;

import java.util.ArrayList;
import java.util.List;

public class ItemEditAdapter extends RecyclerView.Adapter<ItemEditAdapter.ItemViewHolder> {

    private List<Item> items;
    private Context context;
    private List<Item> itemsFull; // Full list for reference
    private OnItemClickListener listener;

    public ItemEditAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.itemsFull = new ArrayList<>(items); // Initialize the full list with a copy of items
        this.listener = listener;
    }


    public void updateItemList(List<Item> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        this.itemsFull = new ArrayList<>(newItems); // Also update the full list used for filtering
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }
    public void filter(String text) {
        items.clear();
        if (text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (Item item : itemsFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onDataChanged(items.isEmpty());
        }
    }




    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView itemNameTextView, itemCodeTextView;
        private ImageView imageViewOptions;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNameTextView = itemView.findViewById(R.id.nameTextView);
            itemCodeTextView = itemView.findViewById(R.id.itemPriceTextView);
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position));
                }
            });

            imageViewOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, position);
                }
            });
        }

        void bind(Item item) {
            itemNameTextView.setText(item.getName());
            double itemPrice = item.getItemPrice();
            String formattedPrice;

            if (itemPrice == (long) itemPrice) {
                // Price is a whole number, format without decimal places
                formattedPrice = String.format("%d CFA", (long)itemPrice);
            } else {
                // Price has decimals, format with decimal places
                formattedPrice = String.format("%.2f CFA", itemPrice);
            }

            itemCodeTextView.setText(formattedPrice);
        }

        private void showPopupMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.item_edit_menu); // Assuming you have a menu XML `clientedit.xml` for editing
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.edit_option) {
                    Item itemToEdit = items.get(position);
                    listener.onEditClicked(itemToEdit);
                    return true;
                }
                return false;
            });
            popup.show();
        }

    }
    public interface OnItemClickListener {
        void onItemClick(Item item);
        void onDeleteClicked(Item item);
        void onEditClicked(Item item);
        void onDataChanged(boolean isEmpty);
    }
}
