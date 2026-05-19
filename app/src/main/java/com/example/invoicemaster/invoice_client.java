package com.example.invoicemaster;

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

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class invoice_client extends Fragment {

    private static final int REQUEST_CODE_EDIT_CLIENT = 1;
    private RecyclerView recyclerView;
    private ClientEditAdapter clientAdapter;
    private BottomSheetFragment bottomSheetFragment;
    private LinearLayout billtoLayout;

    public invoice_client() {
        // Required empty public constructor
    }
    TextView tvConnectionError;
    Button btnRefresh;
    ShimmerFrameLayout shimmerFrameLayout;
    LinearLayout Clients_searchcontainer;
    private ImageView noClientsImage;
    private TextView noClientsView;
    SearchView Clients_searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);
        recyclerView = view.findViewById(R.id.clients_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        billtoLayout = getActivity().findViewById(R.id.billto); // Update this line

        bottomSheetFragment = (BottomSheetFragment) getParentFragment();
        noClientsImage = view.findViewById(R.id.image_no_clients);
        noClientsView = view.findViewById(R.id.text_no_clients);

        Clients_searchView=view.findViewById(R.id.clients_searchView);
        Clients_searchcontainer=view.findViewById(R.id.clients_searchcontainer);
        shimmerFrameLayout=view.findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();
        tvConnectionError = view.findViewById(R.id.tv_connection_error);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        btnRefresh = view.findViewById(R.id.btn_refresh);
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
                recyclerView.setVisibility(View.GONE);

                // Fetch data again
                fetchDataFromServer();
            }
        });
        updateEmptyClientsView(false);
        Clients_searchView.clearFocus();
        EditText searchEditText = Clients_searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.parseColor("#97989a"));
        if (Clients_searchView != null) {
            int searchPlateId = androidx.appcompat.R.id.search_plate;
            View searchPlate = Clients_searchView.findViewById(searchPlateId);
            if (searchPlate != null) {
                searchPlate.setBackgroundColor(Color.TRANSPARENT); // Set to transparent to remove underline
            }
        }
        clientAdapter = new ClientEditAdapter(new ArrayList<>(), new ClientEditAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Client client) {
                SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                sharedViewModel.selectClientId(client.getClientId());

                // Dismiss the BottomSheetFragment
                bottomSheetFragment.dismiss();

                // Make billtoLayout visible
                if (billtoLayout != null) {
                    billtoLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onEditClicked(Client client) {
                // Handle edit action here
                Intent intent = new Intent(getActivity(), Editclients.class);
                intent.putExtra("client_id", client.getClientId());
                startActivityForResult(intent, REQUEST_CODE_EDIT_CLIENT);
            }

            @Override
            public void onFilterResult(boolean isEmpty) {
                updateEmptyClientsView(isEmpty);
            }
        });
        recyclerView.setAdapter(clientAdapter);

        fetchDataFromServer();
        Clients_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter clients based on the entered text
                clientAdapter.filter(newText);
                return true;
            }



        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_CLIENT && resultCode == getActivity().RESULT_OK && data != null) {
            String updatedClientId = data.getStringExtra("updated_client_id");
            String updatedClientName = data.getStringExtra("updated_client_name");
            String updatedPhoneNumber = data.getStringExtra("updated_phone_number");
            String updatedClientAddress = data.getStringExtra("updated_client_address");

            Client updatedClient = new Client(updatedClientId, updatedClientName, updatedPhoneNumber, updatedClientAddress);
            updateClientInList(updatedClient);
        }
    }

    private void updateClientInList(Client updatedClient) {
        clientAdapter.updateClientInAdapter(updatedClient);
    }

    private void fetchDataFromServer() {
        updateEmptyClientsView(false);
        String url = "https://invoicemaster.top/get_clients.php";
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<Client> clients = parseJsonResponse(response);
                    clientAdapter.addClientsAtTop(clients);// Inside fetchDataFromServer() after fetching data:
                    clientAdapter.updateClientsList(clients);

                },
                error ->    handleErrorFetchingData(error)  );

        queue.add(jsonArrayRequest);
    }

    private List<Client> parseJsonResponse(JSONArray jsonArray) {
        List<Client> clients = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Client client = new Client(
                        jsonObject.getString("client_id"),
                        jsonObject.getString("client_name"),
                        jsonObject.getString("phone_number"),
                        jsonObject.getString("client_address"));
                clients.add(client);
            }

            if (clients.isEmpty()) {
                updateEmptyClientsView(true); // Show "No clients" view
            } else {
                clientAdapter.updateClientsList(clients); // Update the adapter's list
                updateEmptyClientsView(false); // Hide "No clients" view
            }
            Collections.sort(clients, new Comparator<Client>() {

                public int compare(Client c1, Client c2) {
                    return Integer.compare(Integer.parseInt(c2.getClientId()), Integer.parseInt(c1.getClientId()));
                }
            });
            shimmerFrameLayout.stopShimmer();;
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            handleErrorFetchingData(new VolleyError(e));
        }
        return clients;
    }
    public void updateEmptyClientsView(boolean show) {
        noClientsImage.setVisibility(show ? View.VISIBLE : View.GONE);
        noClientsView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onResume() {
        super.onResume();
        fetchDataFromServer();
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
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

        //.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
