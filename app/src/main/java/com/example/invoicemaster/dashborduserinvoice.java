package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class dashborduserinvoice extends AppCompatActivity implements UserInvoiceAdapter.OnItemClickListener {
    private RecyclerView userInvoicesRecyclerView;
    private UserInvoiceAdapter userInvoiceAdapter;
    private List<UserInvoice1> userInvoiceList;
    private TextView tvConnectionError;
    private Button btnRefresh;
    private ShimmerFrameLayout shimmerFrameLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashborduserinvoice);

        userInvoicesRecyclerView = findViewById(R.id.user_invoices_recycler);
        tvConnectionError = findViewById(R.id.tv_connection_error);
        btnRefresh = findViewById(R.id.btn_refresh);
        shimmerFrameLayout = findViewById(R.id.shimmer);

        userInvoicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userInvoiceList = new ArrayList<>();
        userInvoiceAdapter = new UserInvoiceAdapter(userInvoiceList, this); // Pass 'this' as the OnItemClickListener
        userInvoicesRecyclerView.setAdapter(userInvoiceAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar_user_invoices);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String startDate = getIntent().getStringExtra("selectedStartDate");
        String endDate = getIntent().getStringExtra("selectedEndDate");

        userInvoiceList.clear();
        fetchUserInvoices(startDate, endDate);
    }

    private void fetchUserInvoices(String startDate, String endDate) {
        String url = "https://invoicemaster.top/userinvoice.php" +
                "?start_date=" + startDate +
                "&end_date=" + endDate;
        Log.d("FetchInvoiceDetails", "User ID clicked: " + url);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                this::parseUserInvoices,
                this::handleErrorFetchingData);

        queue.add(jsonArrayRequest);
    }

    private void parseUserInvoices(JSONArray response) {
        try {
            List<UserInvoice1> userInvoices = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                UserInvoice1 userInvoice = new UserInvoice1(
                        obj.getInt("user_id"),
                        obj.getString("email"),
                        obj.getInt("invoice_count")

                );
                userInvoices.add(userInvoice);
            }
            userInvoiceAdapter.updateUserInvoiceList(userInvoices);
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            userInvoicesRecyclerView.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorFetchingData(new VolleyError(e));
        }
    }

    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();
        //Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int userId) {

        Intent intent = new Intent(this, dashbordi.class);
        String startDate = getIntent().getStringExtra("selectedStartDate");
        String endDate = getIntent().getStringExtra("selectedEndDate");
        intent.putExtra("selectedStartDate", startDate);
        intent.putExtra("selectedEndDate", endDate);

        intent.putExtra("userId", String.valueOf(userId));



        startActivity(intent);
    }

}