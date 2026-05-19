package com.example.invoicemaster.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.invoicemaster.Addnewclient;
import com.example.invoicemaster.InvoiceActivity;
import com.example.invoicemaster.MainActivity;
import com.example.invoicemaster.PreferenceManager;
import com.example.invoicemaster.R;
import com.example.invoicemaster.UserInvoice;
import com.example.invoicemaster.fragments.ClientsFragment;
import com.example.invoicemaster.UserAllfragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class UserMainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public interface OnSearchQueryChangedListener {
        void onSearchQueryChanged(String query);
    }
    private OnSearchQueryChangedListener searchQueryChangedListener;
    private MenuItem searchItem;
    ChipNavigationBar bottomNav;
    FragmentManager fragmentManager;
    FloatingActionButton fabAdd;

    @SuppressLint({"NonConstantResourceId", "MissingInflatedId"})
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        fragmentManager = getSupportFragmentManager();
        bottomNav = findViewById(R.id.user_bottom_nav);
        fabAdd = findViewById(R.id.fab_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String userId = getIntent().getStringExtra("user_id");
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(UserMainActivity.this, InvoiceActivity.class);
            intent.putExtra("user_id", userId); // Pass the user ID to the InvoiceActivity
            startActivity(intent);
        });

        bottomNav.setOnItemSelectedListener(id -> {
            Fragment selectedFragment = null;
            String title = "";

            if (id == R.id.home) {
                title = "Factures";
                selectedFragment = UserAllfragment.newInstance(userId);

                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(view -> {
                    Intent intent = new Intent(UserMainActivity.this, UserInvoice.class);
                    intent.putExtra("user_id", userId); // Pass the user ID to the InvoiceActivity
                    startActivity(intent);
                });
            } else if (id == R.id.clients) {
                title = "Clients";

                selectedFragment = new ClientsFragment();
                fabAdd.setVisibility(View.VISIBLE);
                fabAdd.setOnClickListener(view -> startActivity(new Intent(UserMainActivity.this, Addnewclient.class)));
            } else {
                Log.e(TAG, "Unhandled navigation item.");
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            toolbar.setTitle(title);
            invalidateOptionsMenu(); // Refresh the menu
        });

        // Set initial selected item to "Home"
        bottomNav.setItemSelected(R.id.home, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("recherche");

        searchItem.setVisible(bottomNav.getSelectedItemId() == R.id.home);

        searchView.setBackgroundResource(R.drawable.search_view_background);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                item.getActionView().requestFocus();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Handle SearchView collapsing if needed
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchQueryChangedListener != null) {
                    searchQueryChangedListener.onSearchQueryChanged(newText);
                }
                return false;
            }
        });

        return true;
    }
    public void setOnSearchQueryChangedListener(OnSearchQueryChangedListener listener) {
        this.searchQueryChangedListener = listener;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            item.expandActionView(); // Expand the SearchView
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quitter l'application");
        builder.setMessage("êtes-vous sûr de vouloir quitter?");
        builder.setPositiveButton("Oui", (dialog, which) -> {
            PreferenceManager preferenceManager = new PreferenceManager(this);
            preferenceManager.clearUserData();
            finish();
        });
        builder.setNegativeButton("Annuler", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

