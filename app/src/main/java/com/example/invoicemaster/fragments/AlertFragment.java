package com.example.invoicemaster.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.Item;
import com.example.invoicemaster.R;
import com.example.invoicemaster.itemstockadapter;
import com.example.invoicemaster.mangestockdetails;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertFragment extends Fragment implements itemstockadapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private itemstockadapter adapter;
    TextView tvConnectionError;
    Button btnRefresh;
    ShimmerFrameLayout shimmerFrameLayout;
    private List<Item> itemList;
    private TextView noItemsView;

    SearchView Items_searchView;
    LinearLayout Items_searchcontainer;
    private ImageView noItemsImage;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rview = inflater.inflate(R.layout.alertlayout, container, false);
        shimmerFrameLayout=rview.findViewById(R.id.shimmer);
        noItemsView = rview.findViewById(R.id.text_no_items);
        noItemsImage = rview.findViewById(R.id.image_no_items);
        Items_searchView = rview.findViewById(R.id.items_searchView);
        Items_searchcontainer = rview.findViewById(R.id.items_searchcontainer);
        recyclerView = rview.findViewById(R.id.items_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        adapter = new itemstockadapter( itemList, this); // Pass 'this' as the listener
        recyclerView.setAdapter(adapter);
        updateEmptyViewVisibility(itemList.isEmpty()); // Update the empty view visibility initially

        updateEmptyViewVisibility(false);
        setupSearchView();
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        shimmerFrameLayout=rview.findViewById(R.id.shimmer);
        tvConnectionError = rview.findViewById(R.id.tv_connection_error);
        btnRefresh = rview.findViewById(R.id.btn_refresh);
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        // Show the ShimmerFrameLayout
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        btnRefresh.setOnClickListener(v -> {
            // Hide the TextView and Button
            tvConnectionError.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.GONE);

            // Show the ShimmerFrameLayout
            shimmerFrameLayout.startShimmer();
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            // Fetch data again
            fetchData();
        });


        fetchData();

        return rview;
    }
    private void handleError(VolleyError error) {
        handleErrorFetchingData(error);
    }
    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();

        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            tvConnectionError.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.VISIBLE);
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }, 2000);

    }

    private void setupSearchView() {
        Items_searchView.clearFocus();
        EditText searchEditText = Items_searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.parseColor("#97989a"));

        int searchPlateId = androidx.appcompat.R.id.search_plate;
        View searchPlate = Items_searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT); // Set to transparent to remove underline
        }

        Items_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
    }

    private void fetchData() {
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        updateEmptyViewVisibility(false);
        String url = "https://invoicemaster.top/getalert.php                                                    ";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                this::parseItems,
                this::handleError);

        queue.add(jsonArrayRequest);
    }

    private void parseItems(JSONArray response) {
        try {
            List<Item> items = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                Item item = new Item(
                        obj.getInt("id"),
                        obj.getString("name"),
                        obj.getString("code"),
                        obj.getInt("carton_number"),
                        obj.getInt("packet_number"),
                        obj.getInt("cartonQty"),
                        obj.getDouble("cost_price"),
                        obj.getInt("item_price"),
                        obj.getDouble("carton_price"),
                        obj.getDouble("packet_price")
                );
                items.add(item);
            }

            Collections.sort(items, (o1, o2) -> Integer.compare(o2.getId(), o1.getId()));
            shimmerFrameLayout.stopShimmer();;
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateItemList(items);
            updateEmptyViewVisibility(items.isEmpty());
            if (items.isEmpty()) {
                updateEmptyViewVisibility(true);
            } else {
                adapter.updateItemList(items);
                updateEmptyViewVisibility(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorFetchingData(new VolleyError(e));
        }
    }



    public void updateEmptyViewVisibility(boolean isEmpty) {
        noItemsView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        noItemsImage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(requireContext(), mangestockdetails.class);
        intent.putExtra("item_id", String.valueOf(item.getId()));
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Item item) {
        // Method for handling delete click. Implement if necessary.
    }
}