package com.example.invoicemaster.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.EditInvoiceActivity;
import com.example.invoicemaster.Invoice;
import com.example.invoicemaster.InvoiceAdapter;
import com.example.invoicemaster.MainActivity;
import com.example.invoicemaster.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Allfragment extends Fragment implements InvoiceAdapter.OnInvoiceClickListener, TabLayout.OnTabSelectedListener, MainActivity.OnSearchQueryChangedListener {

    TabLayout tabLayout;
    private List<Invoice> originalInvoiceList = new ArrayList<>();
    private RecyclerView recyclerView;
    private InvoiceAdapter adapter;
    TextView tvConnectionError;
    Button btnRefresh;
    private List<Invoice> invoiceList = new ArrayList<>();
    ShimmerFrameLayout shimmerFrameLayout;

    private static final String JSON_URL = "https://invoicemaster.top/get_invoices.php";
    private Timer timer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSearching = false; // Flag to track if the user is searching
    private int currentTabPosition = 0; // Track the current tab

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fetchData();
            handler.postDelayed(this, 10000); // Repeat every 10 seconds (10000 milliseconds)
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allfragment, container, false);
        recyclerView = view.findViewById(R.id.invoices_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InvoiceAdapter(invoiceList, this); // Pass this as the listener
        recyclerView.setAdapter(adapter);
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(this);
        shimmerFrameLayout = view.findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();

        tvConnectionError = view.findViewById(R.id.tv_connection_error);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the TextView and Button
                tvConnectionError.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.GONE);

                // Show the ShimmerFrameLayout
                shimmerFrameLayout.startShimmer();
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                // Fetch data again
                fetchData();
            }
        });
        return view;
    }

    @Override
    public void onInvoiceClick(String invoiceId, String status) {
        Intent intent = new Intent(getContext(), EditInvoiceActivity.class);
        intent.putExtra("invoice_id", invoiceId);
        intent.putExtra("status", status);
        startActivity(intent);
    }

    private void fetchData() {
        // If user is searching, don't refresh the list
        if (isSearching) {
            return;
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, JSON_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Invoice> newInvoiceList = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String invoiceId = jsonObject.getString("invoice_id");
                                String clientId = jsonObject.getString("client_id");
                                String userId = jsonObject.getString("user_id");
                                String date = jsonObject.getString("date");
                                String status = jsonObject.getString("status");
                                String totalAmount = jsonObject.getString("total_amount");
                                String discount = jsonObject.getString("discount");
                                String tax = jsonObject.getString("tax");
                                String clientName = jsonObject.getString("client_name");
                                Invoice invoice = new Invoice(invoiceId, clientId, userId, date, status, totalAmount, discount, tax, clientName);
                                newInvoiceList.add(invoice);
                            }

                            originalInvoiceList.clear();
                            originalInvoiceList.addAll(newInvoiceList);

                            // Check if the data has changed
                            if (!newInvoiceList.equals(invoiceList)) {
                                // Data has changed, update the RecyclerView
                                invoiceList.clear();
                                invoiceList.addAll(newInvoiceList);
                                adapter.notifyDataSetChanged();
                            }

                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            // Reapply the current tab filter
                            filterInvoices(currentTabPosition);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            handleErrorFetchingData(new VolleyError(e));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleErrorFetchingData(error);
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setOnSearchQueryChangedListener(this);
        // Start the periodic data fetch
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 0, 10000); // Start immediately and repeat every 10 seconds (10000 milliseconds)
    }

    @Override
    public void onPause() {
        super.onPause();
        // Cancel the periodic data fetch
        timer.cancel();
        handler.removeCallbacks(runnable);
    }

    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();
        // Hide the ShimmerFrameLayout and RecyclerView
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        }, 2000); // Delay time for showing error message
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        currentTabPosition = tab.getPosition(); // Update the current tab position
        filterInvoices(currentTabPosition);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // No implementation needed
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // No implementation needed
    }

    private void filterInvoices(int tabPosition) {
        List<Invoice> filteredList = new ArrayList<>();

        switch (tabPosition) {
            case 0: // All tab
                filteredList.addAll(originalInvoiceList); // Display all invoices
                break;
            case 1: // Pending tab
                for (Invoice invoice : originalInvoiceList) {
                    if (invoice.getStatus().equalsIgnoreCase("Unpaid")) {
                        filteredList.add(invoice);
                    }
                }
                break;
            case 2: // Paid tab
                for (Invoice invoice : originalInvoiceList) {
                    if (invoice.getStatus().equalsIgnoreCase("Paid")) {
                        filteredList.add(invoice);
                    }
                }
                break;
        }

        // Sort the filtered list in descending order based on invoice ID
        Collections.sort(filteredList, new Comparator<Invoice>() {
            @Override
            public int compare(Invoice invoice1, Invoice invoice2) {
                return Integer.parseInt(invoice2.getInvoiceId()) - Integer.parseInt(invoice1.getInvoiceId());
            }
        });

        adapter.updateList(filteredList); // Update RecyclerView with filtered and sorted list
    }

    @Override
    public void onSearchQueryChanged(String query) {
        if (query.isEmpty()) {
            isSearching = false;
            filterInvoices(currentTabPosition); // Show all or filtered invoices based on the current tab
        } else {
            isSearching = true;
            filterInvoicesByClientName(query); // Filter invoices based on the search query
        }
    }

    private void filterInvoicesByClientName(String query) {
        List<Invoice> filteredList = new ArrayList<>();

        for (Invoice invoice : originalInvoiceList) {
            if (invoice.getClientName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(invoice);
            }
        }

        // Sort the filtered list in descending order based on invoice ID
        Collections.sort(filteredList, new Comparator<Invoice>() {
            @Override
            public int compare(Invoice invoice1, Invoice invoice2) {
                return Integer.parseInt(invoice2.getInvoiceId()) - Integer.parseInt(invoice1.getInvoiceId());
            }
        });

        adapter.updateList(filteredList); // Update RecyclerView with filtered and sorted list
    }
}
