package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class viewreport extends AppCompatActivity {
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout paidLayout,unpaidLayout,errorlayout,profitLayout;
    private RecyclerView recyclerView;
    TextView tvConnectionError;
    Button btnRefresh;
    private InvoiceActivityAdapter adapter;

    private TextView totalUnpaidAmountTextView;
    private TextView totalPaidAmountTextView,totalProfitAmountTextView;

    private List<Invoiceforreport> invoiceList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewreport);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        invoiceList = new ArrayList<>();
        adapter = new InvoiceActivityAdapter(invoiceList);
        recyclerView.setAdapter(adapter);
        totalUnpaidAmountTextView = findViewById(R.id.totalUnpaidAmountTextView);
        shimmerFrameLayout=findViewById(R.id.shimmer);
        paidLayout = findViewById(R.id.totalpaid);
        profitLayout=findViewById(R.id.totalprofit);
        unpaidLayout = findViewById(R.id.totalunpaid);
        errorlayout=findViewById(R.id.error_layout);
        shimmerFrameLayout.startShimmer();
        totalPaidAmountTextView = findViewById(R.id.totalPaidAmountTextView);
        totalProfitAmountTextView=findViewById(R.id.totalProfitAmountTextView);
        Intent intent = getIntent();
        String selectedOption = intent.getStringExtra("selectedOption1");
        String selectedClientId = intent.getStringExtra("selectedClientId");
        String selectedStartDate = intent.getStringExtra("selectedStartDate");
        String selectedEndDate = intent.getStringExtra("selectedEndDate");
        tvConnectionError =findViewById(R.id.tv_connection_error);
        btnRefresh = findViewById(R.id.btn_refresh);
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
                unpaidLayout.setVisibility(View.GONE);
                paidLayout.setVisibility(View.GONE);
                profitLayout.setVisibility(View.GONE);
                // Fetch data again
                fetchData(selectedOption, selectedClientId, selectedStartDate, selectedEndDate);
            }
        });
        fetchData(selectedOption, selectedClientId, selectedStartDate, selectedEndDate);
    }

    private void setSupportActionBar(Toolbar toolbar) {

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
                errorlayout.setVisibility(View.VISIBLE);
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                unpaidLayout.setVisibility(View.GONE);
                paidLayout.setVisibility(View.GONE);
                profitLayout.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

        //Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void fetchData(String selectedOption, String selectedClientId, String selectedStartDate, String selectedEndDate) {
        // Construct the API endpoint URL with parameters
        String url = "https://invoicemaster.top/getinvocefilterd1.php?";
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String formattedStartDate = "";
        String formattedEndDate = "";
        try{
            java.util.Date startDate = inputFormat.parse(selectedStartDate);
            formattedStartDate = outputFormat.format(startDate);

            java.util.Date endDate = inputFormat.parse(selectedEndDate);
            formattedEndDate = outputFormat.format(endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Append the selected options as query parameters
        if (selectedOption.equals("Applytwodate")) {
            url += "startDate=" + formattedStartDate + "&endDate=" + formattedEndDate;
        } else {
            url += "option=" + selectedOption;
        }

        // Append the selected client ID as a query parameter
        url += "&clientId=" + selectedClientId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Invoiceforreport> sortedInvoiceList = new ArrayList<>();

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
                                String profit =jsonObject.getString("profit");
                                Invoiceforreport invoice = new Invoiceforreport(invoiceId, clientId, userId, date, status, totalAmount, discount, tax, clientName,profit);
                                sortedInvoiceList.add(invoice);
                            }

                            // Sort the list in descending order based on invoice ID
                            Collections.sort(sortedInvoiceList, new Comparator<Invoiceforreport>() {
                                @Override
                                public int compare(Invoiceforreport invoice1,Invoiceforreport invoice2) {
                                    return Integer.parseInt(invoice2.getInvoiceId()) - Integer.parseInt(invoice1.getInvoiceId());
                                }
                            });

                            // Clear existing list and add sorted invoices
                            invoiceList.clear();
                            invoiceList.addAll(sortedInvoiceList);
                            adapter.notifyDataSetChanged();
                            double totalUnpaidAmount = adapter.getTotalUnpaidAmount();
                            totalUnpaidAmountTextView.setText(formatAmount(totalUnpaidAmount) + " CFA");

                            double totalPaidAmount = adapter.getTotalPaidAmount();
                            totalPaidAmountTextView.setText(formatAmount(totalPaidAmount) + " CFA");

                            double totalprofitL = adapter.getTotalProfit();
                            totalProfitAmountTextView.setText(formatAmount(totalprofitL) + " CFA");
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            paidLayout.setVisibility(View.VISIBLE);
                            unpaidLayout.setVisibility(View.VISIBLE);
                            profitLayout.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handleErrorFetchingData(new VolleyError(e));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                handleErrorFetchingData(error);     }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String formatAmount(double amount) {
        if (amount == (long) amount) {
            return String.format(Locale.US, "%,d", (long) amount);
        } else {
            return String.format(Locale.US, "%,.2f", amount);
        }
    }


}