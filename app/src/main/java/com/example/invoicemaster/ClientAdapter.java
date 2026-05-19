package com.example.invoicemaster;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<Client> clients;
    private List<Client> clientsFull; // Full list for reference
    private Context context;
    private OnItemClickListener listener;

    public void updateClientInAdapter(Client updatedClient) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getClientId().equals(updatedClient.getClientId())) {
                clients.set(i, updatedClient);
                notifyItemChanged(i);
                return; // Stop once the client is updated
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Client client);
        void onDeleteClicked(int position);
        void onFilterResult(boolean isEmpty);
    }

    public ClientAdapter(List<Client> clients, OnItemClickListener listener) {
        this.clients = clients;
        this.clientsFull = new ArrayList<>(clients); // Make a copy for the full list
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.client_item_layout, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clients.get(position);
        holder.bind(client);
    }
    public void updateClientsList(List<Client> newClients) {
        clientsFull.clear();
        clientsFull.addAll(newClients);
        this.clients = new ArrayList<>(clientsFull); // Reset displayed list to full
        notifyDataSetChanged();
    }
    public void filter(String text) {
        clients.clear();
        if (text.isEmpty()) {
            clients.addAll(clientsFull);
        } else {
            text = text.toLowerCase();
            for (Client client : clientsFull) {
                if (client.getClientName().toLowerCase().contains(text)) {
                    clients.add(client);
                }
            }
        }
        notifyDataSetChanged();

        // Inform the listener about the filter result
        if (listener != null) {
            listener.onFilterResult(clients.isEmpty());
        }
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void addClientsAtTop(List<Client> newClients) {
        List<Client> uniqueNewClients = new ArrayList<>();

        for (Client newClient : newClients) {
            boolean exists = false;
            for (Client existingClient : clients) {
                if (newClient.getClientId().equals(existingClient.getClientId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueNewClients.add(newClient);
            }
        }

        if (!uniqueNewClients.isEmpty()) {
            clients.addAll(0, uniqueNewClients);
            notifyItemRangeInserted(0, uniqueNewClients.size());
        }
    }

    public void removeClient(int position) {
        clients.remove(position);
        notifyItemRemoved(position);
    }

    public Client getClientAt(int position) {
        return clients.get(position);
    }

    class ClientViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView, addressTextView, phoneTextView, imageViewProfile;
        private ImageView imageViewOptions;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(clients.get(position));
                }
            });

            imageViewOptions.setOnClickListener(v -> showPopupMenu(v, getAdapterPosition()));
        }

        void bind(Client client) {
            nameTextView.setText(client.getClientName());
            addressTextView.setText(client.getClientAddress());
            phoneTextView.setText(client.getPhoneNumber());

            String initialLetter = client.getClientName().substring(0, 1).toUpperCase();
            imageViewProfile.setText(initialLetter);

            imageViewProfile.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_background));
            imageViewProfile.setTextColor(Color.parseColor("#3F51B5"));
        }


        private void showPopupMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.client_menu); // Ensure you have client_menu.xml in res/menu/
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete_option) {
                    // Show confirmation dialog
                    showDeleteConfirmationDialog(position);
                    return true;
                }
                return false;
            });
            popup.show();
        }

        private void showDeleteConfirmationDialog(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Êtes-vous sûr de vouloir supprimer ce client ?");
            builder.setPositiveButton("supprimer", (dialog, which) -> {
                // Call the method to delete the client
                listener.onDeleteClicked(position);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Dismiss the dialog and do nothing
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
}
