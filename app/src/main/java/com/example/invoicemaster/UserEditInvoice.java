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
import androidx.fragment.app.FragmentActivity;
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

public class UserEditInvoice extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewPager;
    private ColorStateList def;
    private FloatingActionButton fabMenu;
    private MaterialCardView menuCard;
    private TextView edit, preview, select;
    private String invoiceId;
    private boolean isLoadingData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_invoice);
        invoiceId = getIntent().getStringExtra("invoice_id");

        viewPager = findViewById(R.id.view_pager);
        InvoicePagerAdapter adapter = new InvoicePagerAdapter(this, invoiceId);
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
                Toast.makeText(UserEditInvoice.this, "Wait till the data loads", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });

        preview.setOnClickListener(v -> {
            if (isLoadingData) {
                Toast.makeText(UserEditInvoice.this, "Wait till the data loads", Toast.LENGTH_SHORT).show();
            } else {
                onClick(v);
            }
        });

        new android.os.Handler().postDelayed(() -> isLoadingData = false, 4000);

        // Set a listener to update the UI when the page is swiped
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabSelection(position);
            }
        });
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
        if (!isLoadingData) {
            if (v.getId() == R.id.edit) {
                viewPager.setCurrentItem(0);
            } else if (v.getId() == R.id.preview) {
                viewPager.setCurrentItem(1);
            }
        }
    }

    public void onOption2Clicked(View view) {
        deleteInvoice(invoiceId);
    }

    private void deleteInvoice(String invoiceId) {
        String url = "https://invoicemaster.top/deleteInvoice.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Toast.makeText(UserEditInvoice.this, "Invoice deleted successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close this activity, or you might want to refresh the list of invoices
                },
                error -> {
                    // Toast.makeText(UserEditInvoice.this, "Failed to delete invoice", Toast.LENGTH_SHORT).show();
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
}
