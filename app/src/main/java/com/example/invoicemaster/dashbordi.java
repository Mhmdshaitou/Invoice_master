package com.example.invoicemaster;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.Invoice;
import com.example.invoicemaster.InvoiceAdapter;
import com.example.invoicemaster.R;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class dashbordi extends AppCompatActivity implements InvoiceAdapter2.OnInvoiceClickListener {

    private RecyclerView recyclerView;
    private InvoiceAdapter2 adapter;
    private TextView tvConnectionError;
    private Button btnRefresh;
    private List<Invoice> invoiceList = new ArrayList<>();
    private ShimmerFrameLayout shimmerFrameLayout;


    private static final String JSON_URL = "https://invoicemaster.top/invoicesbetweentwodates.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashbordi);

        // Find views by their IDs
        recyclerView = findViewById(R.id.invoices_recycler);
        tvConnectionError = findViewById(R.id.tv_connection_error);
        btnRefresh = findViewById(R.id.btn_refresh);
        shimmerFrameLayout = findViewById(R.id.shimmer);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvoiceAdapter2(invoiceList, this);
        recyclerView.setAdapter(adapter);

        String selectedStartDate = getIntent().getStringExtra("selectedStartDate");
        String selectedEndDate = getIntent().getStringExtra("selectedEndDate");
        String userId = getIntent().getStringExtra("userId");
        // Set up click listener for refresh button
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
                fetchData(selectedStartDate, selectedEndDate, userId);
            }
        });

        // Initially hide the TextView, Button, and ShimmerFrameLayout
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.GONE);

        // Fetch data from the API
        fetchData(selectedStartDate, selectedEndDate, userId);
    }

    @Override
    public void onInvoiceClick(String invoiceId, String status) {
        Intent intent = new Intent(this, EditInvoiceActivity.class);
        intent.putExtra("invoice_id", invoiceId);
        intent.putExtra("status", status);
        startActivity(intent);
    }

    private void fetchData(String startDate, String endDate, String userId) {
        String url = JSON_URL + "?start_date=" + startDate + "&end_date=" + endDate + "&user_id=" + userId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Invoice> sortedInvoiceList = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                // Extract the required fields from the JSON object
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

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();

        // Initially hide the TextView and Button
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        // Delay showing the TextView and Button for 2 seconds (2000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show the TextView and Button after the delay
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

        //Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
}