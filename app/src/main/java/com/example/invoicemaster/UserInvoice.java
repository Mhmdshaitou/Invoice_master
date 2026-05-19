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

public class UserInvoice extends AppCompatActivity implements View.OnClickListener, UserEditFragment.DataLoadListener {

    private ViewPager2 viewPager;
    private ColorStateList def;
    private TextView edit, preview, select;
    private boolean isDataLoaded = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_inoice);

        String userId = getIntent().getStringExtra("user_id");

        // Initialize ViewPager2 and Adapter
        viewPager = findViewById(R.id.view_pager);
        UserInvoicePagerAdapter adapter = new UserInvoicePagerAdapter(this, userId);
        viewPager.setAdapter(adapter);

        // Initialize views
        edit = findViewById(R.id.edit);
        preview = findViewById(R.id.preview);
        select = findViewById(R.id.select);

        // Set click listeners
        edit.setOnClickListener(this);
        preview.setOnClickListener(this);

        // Save the default text color
        def = preview.getTextColors();

        // Set back arrow click listener
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> onBackPressed());

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
            // Early return if data is not loaded
            Toast.makeText(this, "Wait till the data is loaded", Toast.LENGTH_SHORT).show();
            return;
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
}
