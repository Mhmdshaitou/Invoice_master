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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final FilterResultListener filterResultListener;
    private List<User> userList;
    private List<User> usersFull;
    private Context context;

    public interface FilterResultListener {
        void onFilterResult(boolean isEmpty);
    }

    private OnItemClickListener listener;

    public UserAdapter(List<User> userList, OnItemClickListener listener, FilterResultListener filterResultListener) {
        this.userList = new ArrayList<>(userList);
        this.usersFull = new ArrayList<>(userList); // Initialize usersFull with the full list
        this.listener = listener;
        this.filterResultListener = filterResultListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_layout, parent, false);
        return new UserViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        if (position < userList.size()) { // Ensure position is within bounds
            User user = userList.get(position);
            holder.bind(user);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> users) {
        this.userList.clear();
        this.userList.addAll(users);
        this.usersFull = new ArrayList<>(users); // Update the full list on updating userList
        notifyDataSetChanged();
    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(usersFull);
        } else {
            text = text.toLowerCase();
            for (User user : usersFull) {
                if (user.getEmail().toLowerCase().contains(text)) {
                    userList.add(user);
                }
            }
        }
        notifyDataSetChanged();

        // Notify about the filter result
        if (filterResultListener != null) {
            filterResultListener.onFilterResult(userList.isEmpty());
        }
    }

    public void deleteUser(final int position) {
        if (position < 0 || position >= userList.size()) {
            Log.e("Delete User", "Invalid position: " + position);
            return;
        }

        User userToDelete = userList.get(position);
        String deleteUrl = "https://invoicemaster.top/delete_user.php";
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteUrl,
                response -> {
                    Log.d("Delete User", "Response: " + response);
                    userList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, userList.size());
                },
                error -> Log.d("Delete User", "Error: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userToDelete.getUserId()));
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void showPopupMenu(View view, int position) {
        if (position < 0 || position >= userList.size()) {
            Log.e("PopupMenu", "Invalid position: " + position);
            return;
        }

        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.users_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete_option) {
                showDeleteConfirmationDialog(position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("êtes vous sûre de vouloir supprimer cet utilisateur?");
        builder.setPositiveButton("Supprimer", (dialog, which) -> deleteUser(position));
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, roleTextView;
        ImageView imageViewOptions;

        UserViewHolder(View itemView, UserAdapter adapter) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            imageViewOptions = itemView.findViewById(R.id.imageViewOptions);

            imageViewOptions.setOnClickListener(view -> adapter.showPopupMenu(view, getAdapterPosition()));
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < adapter.userList.size()) {
                    adapter.listener.onItemClick(adapter.userList.get(position).getUserId());
                }
            });
        }

        void bind(User user) {
            emailTextView.setText(user.getEmail());
            roleTextView.setText(user.getRole());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int userId);
    }
}
