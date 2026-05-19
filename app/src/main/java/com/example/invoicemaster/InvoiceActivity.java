package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class InvoiceActivity extends AppCompatActivity implements View.OnClickListener, EditFragment.DataLoadListener {

    private ColorStateList def;
    private TextView edit, preview, select;
    private ImageView backArrow;
    private boolean isDataLoaded = false; // Flag to check if data is loaded
    private boolean canExit = false; // Flag to control the onBackPressed behavior
    private ViewPager2 viewPager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        String userId = getIntent().getStringExtra("user_id");

        // Set up ViewPager2 with the adapter
        viewPager = findViewById(R.id.view_pager);
        InvoicePagerAdapter2 adapter = new InvoicePagerAdapter2(this, userId);
        viewPager.setAdapter(adapter);

        // Initialize views
        edit = findViewById(R.id.edit);
        preview = findViewById(R.id.preview);
        select = findViewById(R.id.select);
        backArrow = findViewById(R.id.backArrow);

        // Set click listeners
        edit.setOnClickListener(this);
        preview.setOnClickListener(this);
        backArrow.setOnClickListener(v -> onBackPressed());

        // Save the default text color
        def = preview.getTextColors();

        // Initially disable the preview and back arrow
        preview.setEnabled(false);
        backArrow.setEnabled(false);

        // Enable them after a 2-second delay
        new Handler().postDelayed(() -> {
            preview.setEnabled(true);
            backArrow.setEnabled(true);
            canExit = true; // Allow onBackPressed to function normally
        }, 2000); // 2000 milliseconds = 2 seconds

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
        if (!isDataLoaded) {
            Toast.makeText(this, "Add Items first!", Toast.LENGTH_SHORT).show();
            return; // Early return if data is not loaded
        }

        if (v.getId() == R.id.edit) {
            viewPager.setCurrentItem(0);
        } else if (v.getId() == R.id.preview) {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onDataLoaded() {
        isDataLoaded = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up references for garbage collection
    }
}
