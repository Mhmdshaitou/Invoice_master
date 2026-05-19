package com.example.invoicemaster.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.invoicemaster.Managestock;
import com.example.invoicemaster.Manageuser;
import com.example.invoicemaster.PreferenceManager;
import com.example.invoicemaster.R;
import com.example.invoicemaster.Report;
import com.example.invoicemaster.login;
import com.example.invoicemaster.userreport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DashboardFragment extends Fragment {

    private TextView userCountTextView;
    private TextView clientCountTextView;
    private TextView itemCountTextView;
    private Button manageUsersButton;
    private Button reportButton;
    private Button manageStockButton;
    private Button userInvoiceButton;
    private Button logoutButton;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        userCountTextView = rootView.findViewById(R.id.userCounttextView);
        clientCountTextView = rootView.findViewById(R.id.clientCounttextView);
        itemCountTextView = rootView.findViewById(R.id.itemCounttextView);

        manageUsersButton = rootView.findViewById(R.id.manageUsersButton);
        reportButton = rootView.findViewById(R.id.reportButton);
        manageStockButton = rootView.findViewById(R.id.manageStockButton);
        userInvoiceButton = rootView.findViewById(R.id.userInvoiceButton);
        logoutButton = rootView.findViewById(R.id.logout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        manageUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Manageuser.class);
                startActivity(intent);
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Report.class);
                startActivity(intent);
            }
        });

        manageStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Managestock.class);
                startActivity(intent);
            }
        });

        userInvoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), userreport.class);
                startActivity(intent);
            }
        });

        new FetchCountsTask().execute();

        return rootView;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Quitter l'application");
        builder.setMessage("êtes-vous sûr de vouloir quitter?");
        builder.setPositiveButton("Oui", (dialog, which) -> {
            PreferenceManager preferenceManager = new PreferenceManager(getContext());
            preferenceManager.clearUserData();
            // Redirect to login activity
            Intent intent = new Intent(getActivity(), login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
        });
        builder.setNegativeButton("Annuler", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class FetchCountsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Specify the URL of your PHP script
                URL url = new URL("https://invoicemaster.top/count%20data.php");

                // Open a connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set request method
                connection.setRequestMethod("GET");

                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Close connection
                reader.close();
                connection.disconnect();

                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    // Parse JSON response
                    JSONObject jsonObject = new JSONObject(response);

                    // Update TextViews with counts
                    userCountTextView.setText(jsonObject.getString("user_count"));
                    clientCountTextView.setText(jsonObject.getString("client_count"));
                    itemCountTextView.setText(jsonObject.getString("item_count"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}