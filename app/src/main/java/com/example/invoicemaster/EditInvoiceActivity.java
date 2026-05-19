package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditInvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    private ColorStateList def;
    private FloatingActionButton fabMenu;
    private MaterialCardView menuCard;
    private TextView edit, preview, select;
    private String invoiceId;
    private boolean isLoadingData = true; // Flag to check if data is loading
    private ViewPager2 viewPager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_invoice);
        invoiceId = getIntent().getStringExtra("invoice_id");

        String status = getIntent().getStringExtra("status");
        if (status != null) {
            updateMenuOptions(status);
        }

        // Set up ViewPager2 with the adapter
        viewPager = findViewById(R.id.view_pager);
        EditInvoicePagerAdapter adapter = new EditInvoicePagerAdapter(this, invoiceId);
        viewPager.setAdapter(adapter);

        edit = findViewById(R.id.edit);
        preview = findViewById(R.id.preview);
        select = findViewById(R.id.select);

        edit.setOnClickListener(this);
        preview.setOnClickListener(this);
        def = preview.getTextColors();

        fabMenu = findViewById(R.id.fab_menu);
        menuCard = findViewById(R.id.menu_card);

        fabMenu.setOnClickListener(v -> {
            if (menuCard.getVisibility() == View.VISIBLE) {
                menuCard.setVisibility(View.GONE);
            } else {
                menuCard.setVisibility(View.VISIBLE);
            }
        });

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> {
            if (isLoadingData) {
                Toast.makeText(EditInvoiceActivity.this, "Wait till the data loads", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });

        // Listen for page changes to update tab selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabSelection(position);
            }
        });

        // After a 2-second delay, indicate that data loading is complete
        new android.os.Handler().postDelayed(() -> isLoadingData = false, 2000);
    }

    private void updateTabSelection(int position) {
        int targetX = 0;

        if (position == 0) {
            edit.setTextColor(Color.WHITE);
            preview.setTextColor(def);
        } else if (position == 1) {
            preview.setTextColor(Color.WHITE);
            edit.setTextColor(def);
            targetX = preview.getLeft();
        }

        select.animate().translationX(targetX).setDuration(100).start();
    }

    @Override
    public void onClick(View v) {
        if (!isLoadingData) { // Check if data has finished loading before allowing interaction
            if (v.getId() == R.id.edit) {
                viewPager.setCurrentItem(0);
            } else if (v.getId() == R.id.preview) {
                viewPager.setCurrentItem(1);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateMenuOptions(String status) {
        TextView option1TextView = findViewById(R.id.option1);
        TextView option2TextView = findViewById(R.id.option2);

        if (status.equalsIgnoreCase("Paid")) {
            option1TextView.setText("non validé");
            option2TextView.setText("supprimer");
        } else {
            option1TextView.setText("valide");
            option2TextView.setText("supprimer");
        }
    }

    public void onOption1Clicked(View view) {
        String newStatus;
        String option1Text = ((TextView) view).getText().toString();
        if (option1Text.equalsIgnoreCase("valide")) {
            newStatus = "Paid";
        } else {
            newStatus = "Unpaid";
        }

        // Update the invoice status on the server
        updateInvoiceStatus(invoiceId, newStatus);

        // Update the UI based on the new status
        updateMenuOptions(newStatus);

        // Close the menu
        menuCard.setVisibility(View.GONE);
    }

    public void onOption2Clicked(View view) {
        deleteInvoice(invoiceId);
    }

    private void deleteInvoice(String invoiceId) {
        String url = "https://invoicemaster.top/deleteInvoice.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Handle the successful response here
                    finish(); // Close this activity, or you might want to refresh the list of invoices
                },
                error -> {
                    // Handle error here
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("invoice_id", invoiceId);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void updateInvoiceStatus(String invoiceId, String newStatus) {
        String url = "https://invoicemaster.top/updateInvoiceStatus.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("invoice_id", invoiceId);
                params.put("status", newStatus);
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
