package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.fragments.AlertFragment;
import com.example.invoicemaster.fragments.Allfragment;
import com.example.invoicemaster.fragments.ManagestockFragment;
import com.example.invoicemaster.fragments.PaidFragment;
import com.example.invoicemaster.fragments.PendingFragment;
import com.example.invoicemaster.fragments.VPAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Managestock extends AppCompatActivity {

    TabLayout tabLayout;
    FragmentManager fragmentManager;
    ViewPager viewPagerTab;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_managestock);
        fragmentManager = getSupportFragmentManager();

        tabLayout = findViewById(R.id.tab_layout);
        viewPagerTab = findViewById(R.id.view_pager_tab);
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);

        // Set up ViewPager with initial fragments
        VPAdapter vpAdapter = new VPAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new ManagestockFragment(), "MANAGE");
        vpAdapter.addFragment(new AlertFragment(), "ALERT");

        viewPagerTab.setAdapter(vpAdapter);

        // Disable tooltip for each tab by setting a custom OnLongClickListener
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View tabView = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                tabView.setOnLongClickListener(v -> true);
            }
        }

        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPagerTab);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
