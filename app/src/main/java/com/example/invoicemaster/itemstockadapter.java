package com.example.invoicemaster;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.fragments.ItemsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class itemstockadapter extends RecyclerView.Adapter<itemstockadapter.ItemViewHolder> {

    private List<Item> items;
    private Context context;

    private List<Item> itemsFull; // Full list for reference
    private OnItemClickListener listener;
    public void addItem(Item item) {
        items.add(0, item); // Add the item to the beginning of the list
        notifyItemInserted(0); // Notify adapter about the new item at position 0
    }

    public itemstockadapter(List<Item> items, OnItemClickListener listener) {
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
        if(listener instanceof ItemsFragment){
            ((ItemsFragment)listener).updateEmptyViewVisibility(items.isEmpty());
        }
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_itemstockadapter, parent, false);
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
            String itemPrice = String.valueOf(item.getCartonQty());


            itemCodeTextView.setText(itemPrice);



        }

        public void deleteItem(final int position) {
            Item itemToDelete = items.get(position);
            String deleteUrl = "https://invoicemaster.top/delete_item.php";
            RequestQueue queue = Volley.newRequestQueue(context); // Make sure 'context' is available in your adapter

            StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteUrl,
                    response -> {
                        // Handle response
                        Log.d("Delete Item", "Response: " + response);
                        items.remove(position); // Remove the item from the list
                        notifyItemRemoved(position); // Notify adapter about item being removed
                        notifyItemRangeChanged(position, items.size());
                    },
                    error -> Log.d("Delete Item", "Error: " + error.toString())
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", String.valueOf(itemToDelete.getId())); // Passing the 'id' parameter to your PHP script
                    return params;
                }
            };

            queue.add(stringRequest);
        }



        private void showPopupMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.item_menu); // Make sure your menu XML is correctly named
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_option) {
                    // Show confirmation dialog for item deletion
                    showDeleteConfirmationDialog(position);
                    return true;
                }
                return false;
            });
            popup.show();
        }
        private void showDeleteConfirmationDialog(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Are you sure you want to delete this item?");
            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Call the method to delete the item, which should be implemented in your adapter
                deleteItem(position); // Assuming deleteItem is a method in your ItemAdapter
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
        void onDeleteClicked(Item item);
    }
}
