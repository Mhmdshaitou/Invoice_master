package com.example.invoicemaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.user.UserMainActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserAllfragment extends Fragment implements InvoiceAdapter.OnInvoiceClickListener, TabLayout.OnTabSelectedListener, UserMainActivity.OnSearchQueryChangedListener {

    TabLayout tabLayout;
    private List<Invoice> originalInvoiceList = new ArrayList<>();

    private RecyclerView recyclerView;
    private InvoiceAdapter adapter;
    TextView tvConnectionError;
    Button btnRefresh;
    private List<Invoice> invoiceList = new ArrayList<>();
    ShimmerFrameLayout shimmerFrameLayout;
    String userId;

    private static final String JSON_URL = "https://invoicemaster.top/get_invoices.php";

    public static UserAllfragment newInstance(String userId) {
        UserAllfragment fragment = new UserAllfragment();
        Bundle args = new Bundle();
        args.putString("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("user_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userallfragment, container, false);
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
        Intent intent = new Intent(getContext(), UserEditInvoice.class);
        intent.putExtra("invoice_id", invoiceId);
        intent.putExtra("status", status);
        startActivity(intent);
    }

    private void fetchData() {
        String url = "https://invoicemaster.top/get_invoicesbyid.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            List<Invoice> sortedInvoiceList = new ArrayList<>(); // Create a new list to store sorted invoices
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
                                sortedInvoiceList.add(invoice);
                                originalInvoiceList.clear();
                                originalInvoiceList.addAll(sortedInvoiceList);
                            }

                            // Sort the list in descending order based on invoice ID
                            Collections.sort(sortedInvoiceList, new Comparator<Invoice>() {
                                @Override
                                public int compare(Invoice invoice1, Invoice invoice2) {
                                    return Integer.parseInt(invoice2.getInvoiceId()) - Integer.parseInt(invoice1.getInvoiceId());
                                }
                            });

                            // Clear existing list and add sorted invoices
                            invoiceList.clear();
                            invoiceList.addAll(sortedInvoiceList);

                            adapter.notifyDataSetChanged();

                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
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
        ((UserMainActivity) requireActivity()).setOnSearchQueryChangedListener(this);
        fetchData();
    }

    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();
        // Hide the ShimmerFrameLayout and RecyclerView

        // Get the TextView and Button instances

        // Initially hide the TextView and Button
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        // Delay showing the TextView and Button for 2 seconds (2000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show the TextView and Button after the delay
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

       // Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPosition = tab.getPosition();
        filterInvoices(tabPosition);
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
        filterInvoicesByClientName(query);
    }

    private void filterInvoicesByClientName(String query) {
        List<Invoice> filteredList = new ArrayList<>();

        // Filter the originalInvoiceList based on the client name
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