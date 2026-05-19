package com.example.invoicemaster.fragments;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.EditItemActivity;
import com.example.invoicemaster.Item;
import com.example.invoicemaster.ItemAdapter;
import com.example.invoicemaster.R;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsFragment extends Fragment implements ItemAdapter.OnItemClickListener {

    private RecyclerView recyclerView;

    private ItemAdapter adapter;
    TextView tvConnectionError;
    Button btnRefresh;
    private List<Item> itemList;
    private TextView noItemsView;
    ShimmerFrameLayout shimmerFrameLayout;
    SearchView Items_searchView;
    LinearLayout Items_searchcontainer;
    private ImageView noItemsImage;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_items, container, false);

        noItemsView = view.findViewById(R.id.text_no_items);
        noItemsImage = view.findViewById(R.id.image_no_items);
        Items_searchView = view.findViewById(R.id.items_searchView);
        Items_searchcontainer = view.findViewById(R.id.items_searchcontainer);
        recyclerView = view.findViewById(R.id.items_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList, this); // Pass 'this' as the listener
        recyclerView.setAdapter(adapter);
        shimmerFrameLayout=view.findViewById(R.id.shimmer);
        tvConnectionError = view.findViewById(R.id.tv_connection_error);
        btnRefresh = view.findViewById(R.id.btn_refresh);
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

        updateEmptyViewVisibility(itemList.isEmpty()); // Update the empty view visibility initially

        updateEmptyViewVisibility(false);
        setupSearchView();

        fetchData();

        return view;
    }

    private void hideErrorViews() {
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
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
        updateEmptyViewVisibility(false);
        String url = "https://invoicemaster.top/get_items.php";
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

            adapter.updateItemList(items);
            shimmerFrameLayout.stopShimmer();;
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
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

       // Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
        // You can also show a Toast message if needed
        // Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();


    public void updateEmptyViewVisibility(boolean isEmpty) {
        noItemsView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        noItemsImage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(requireContext(), EditItemActivity.class);
        intent.putExtra("item_id", String.valueOf(item.getId()));
        startActivity(intent);
    }


}
