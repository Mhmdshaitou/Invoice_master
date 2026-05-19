package com.example.invoicemaster;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Manageuser extends AppCompatActivity {
    SearchView Users_searchView;

    TextView tvConnectionError;
    Button btnRefresh;
    ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    LinearLayout errorLayout;
    FloatingActionButton fabAdd;
    private ImageView noItemsImage;
    private TextView noItemsView;
    LinearLayout Userss_searchcontainer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manageuser);
        noItemsView = findViewById(R.id.text_no_items);
        noItemsImage =findViewById(R.id.image_no_items);
        Users_searchView = findViewById(R.id.users_searchView);
        fabAdd = findViewById(R.id.fab_add);
        fabAdd.bringToFront();
        usersRecyclerView = findViewById(R.id.users_recycler);
        shimmerFrameLayout=findViewById(R.id.shimmer);
        errorLayout = findViewById(R.id.error_layout);
        shimmerFrameLayout.startShimmer();

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
                usersRecyclerView.setVisibility(View.GONE);

                // Fetch data again
                fetchUsers();
            }
        });
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, userId -> {
            Intent intent = new Intent(Manageuser.this, Edituser.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        }, isEmpty -> {
            // Implement the interface method to update visibility
            updateEmptyViewVisibility(isEmpty);
        });

        updateEmptyViewVisibility(userList.isEmpty()); // Update the empty view visibility initially

        updateEmptyViewVisibility(false);
        usersRecyclerView.setAdapter(userAdapter);

        usersRecyclerView.setAdapter(userAdapter);
        Userss_searchcontainer = findViewById(R.id.users_searchcontainer);
        Users_searchView.clearFocus();
        EditText searchEditText = Users_searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.parseColor("#97989a"));
        int searchPlateId = androidx.appcompat.R.id.search_plate;
        View searchPlate = Users_searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT); // Set to transparent to remove underline
        }
        Users_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.filter(newText);
                return true;
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When FloatingActionButton is clicked, open another activity
                Intent intent = new Intent(Manageuser.this,Addnewuser.class);
                startActivity(intent);
            }
        });



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
        userList.clear();
        fetchUsers();
    }


    private void fetchUsers() {
        updateEmptyViewVisibility(false);
        String url = "https://invoicemaster.top/get_users.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                this::parseUsers,
                error -> {
                    handleErrorFetchingData(error);     });

        queue.add(jsonArrayRequest);
    }

    private void parseUsers(JSONArray response) {
        try {
            List<User> users = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                User user = new User(
                        obj.getInt("user_id"),
                        obj.getString("email"),
                        obj.getString("password"),
                        obj.getString("role")
                );
                users.add(user);
            }
            userAdapter.updateUserList(users);
            // Optionally sort the users by ID or another attribute
            Collections.sort(users, (o1, o2) -> Integer.compare(o1.getUserId(), o2.getUserId()));
            updateEmptyViewVisibility(users.isEmpty());
            if (users.isEmpty()) {
                updateEmptyViewVisibility(true);
            } else {
                userAdapter.updateUserList(users);
                updateEmptyViewVisibility(false);
            }
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
            fabAdd.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorFetchingData(new VolleyError(e));

        }
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
                errorLayout.setVisibility(View.VISIBLE);
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                usersRecyclerView.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

        //Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
    public void updateEmptyViewVisibility(boolean isEmpty) {
        noItemsView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        noItemsImage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
