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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.fragments.ItemsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private Context context;

    private List<Item> itemsFull; // Full list for reference
    private OnItemClickListener listener;

    public ItemAdapter(List<Item> items, OnItemClickListener listener) {
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

        public void deleteItem(final int position) {
            Item itemToDelete = items.get(position);
            String deleteUrl = "https://invoicemaster.top/delete_item.php";
            RequestQueue queue = Volley.newRequestQueue(context); // Make sure 'context' is available in your adapter

            StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteUrl,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("message")) {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                items.remove(position); // Remove the item from the list
                                notifyItemRemoved(position); // Notify adapter about item being removed
                                notifyItemRangeChanged(position, items.size());
                            } else if (jsonResponse.has("error")) {
                                String error = jsonResponse.getString("error");
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Vous ne pouvez pas supprimer cet article car il existe dans une facture." + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(context, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()
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
            builder.setMessage("Êtes-vous sûr de vouloir supprimer cet élément ?");
            builder.setPositiveButton("supprimer", (dialog, which) -> {
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
    }
}
