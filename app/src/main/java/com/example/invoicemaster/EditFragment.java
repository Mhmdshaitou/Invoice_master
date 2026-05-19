package com.example.invoicemaster;

import static java.lang.Double.parseDouble;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditFragment extends Fragment {
    private String newInvoiceID1;
    private double taxPercentage = 0.0; // Default to 0.0
    private List<InvoiceItem> invoiceItems = new ArrayList<>();
    TextView tvConnectionError;
    Button btnRefresh;

    ShimmerFrameLayout shimmerFrameLayout;
    RelativeLayout mc, error_layout;
    private String userid;
    private boolean dataFetched = false;
    private String clientId1;

    public interface DataLoadListener {
        void onDataLoaded();
    }

    private DataLoadListener dataLoadListener;

    public void setDataLoadListener(DataLoadListener listener) {
        this.dataLoadListener = listener;
    }

    public EditFragment() {
    }

    private RecyclerView recyclerView;
    private InvoiceItemAdapter adapter;

    @Override
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        recyclerView = view.findViewById(R.id.invoiceitems_recycler);
        shimmerFrameLayout = view.findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();
        mc = view.findViewById(R.id.ic);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Bundle args = getArguments();
        if (args != null) {
            userid = args.getString("user_id");
            fetchLastInvoiceID(userid);
        }

        Button addInvoiceButton = view.findViewById(R.id.btsavedata);
        addInvoiceButton.setOnClickListener(v -> saveDataToDatabase());

        // Find the "Add Client" button in the layout
        Button addClientButton = view.findViewById(R.id.add_client);
        // Set a click listener for the button
        addClientButton.setOnClickListener(v -> showBottomSheetFragment());

        // Find the "Add Items" button in the layout
        Button addItemsButton = view.findViewById(R.id.add_items);
        Button addItemsButton2 = view.findViewById(R.id.add_item_button);

        // Set a click listener for the button
        addItemsButton.setOnClickListener(v -> openitemBottomSheet());
        addItemsButton2.setOnClickListener(v -> openitemBottomSheet());

        // Find the "billto" layout
        View billToLayout = view.findViewById(R.id.billto);

        // Set a click listener for the billto layout
        billToLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dataFetched) {
                    // Handle click event when data is not fetched (if needed)
                    // Toast.makeText(requireContext(), "Data not fetched yet", Toast.LENGTH_SHORT).show();
                } else {
                    // Show the BottomSheetFragment to change the data
                    showBottomSheetFragment();
                }
            }
        });

        TextView tvDiscount = view.findViewById(R.id.etDiscount);

        tvDiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_discount_input, null);
                final EditText dialogEtDiscount = dialogView.findViewById(R.id.dialog_etDiscount);

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Entrez le montant de la remise")
                        .setView(dialogView)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String discountStr = dialogEtDiscount.getText().toString();
                                String discountText;
                                try {
                                    double discountAmount = parseDouble(discountStr);
                                    if (discountAmount == (long) discountAmount) {
                                        discountText = String.format("%,d CFA", (long) discountAmount);
                                    } else {
                                        DecimalFormat formatter = new DecimalFormat("#,##0.00");
                                        discountText = formatter.format(discountAmount) + " CFA";
                                    }
                                    tvDiscount.setText(discountText);

                                    updateTotal();
                                } catch (NumberFormatException e) {
                                    // Handle exception
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null) // Dismiss the dialog without action
                        .create();

                dialog.show();
            }
        });

        TextView etTax = view.findViewById(R.id.etTax);
        TextView tvTax = view.findViewById(R.id.tvTax); // TextView to display "Tax(x%)
        TextView tvTotal = view.findViewById(R.id.tvTotal); // Assuming this TextView exists and contains the total amount

        etTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_tax_input, null);
                final EditText dialogEtTax = dialogView.findViewById(R.id.dialog_etTax);

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Entrez le pourcentage de taxe")
                        .setView(dialogView)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String taxStr = dialogEtTax.getText().toString();
                                try {
                                    taxPercentage = Double.parseDouble(taxStr); // Update global variable
                                    String formattedTaxPercentage = taxPercentage == Math.floor(taxPercentage) ?
                                            String.format(Locale.getDefault(), "Tax(%d%%)", (int) taxPercentage) :
                                            String.format(Locale.getDefault(), "Tax(%.2f%%)", taxPercentage);
                                    tvTax.setText(formattedTaxPercentage);
                                    updateTotal();
                                } catch (NumberFormatException e) {
                                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
            }
        });

        return view;
    }

    private void updateTotal() {
        View rootView = getView();
        if (rootView == null || !isAdded()) {
            return;
        }
        TextView tvSubtotal = rootView.findViewById(R.id.tvSubtotal);
        TextView tvDiscount = rootView.findViewById(R.id.etDiscount);
        TextView etTax = rootView.findViewById(R.id.etTax);
        TextView tvTotal = rootView.findViewById(R.id.tvTotal);
        try {
            double subtotal = Double.parseDouble(tvSubtotal.getText().toString().replaceAll("[^\\d.]", ""));
            double discount = Double.parseDouble(tvDiscount.getText().toString().replaceAll("[^\\d.]", ""));
            double taxAmount = ((subtotal - discount) * taxPercentage) / 100.0;
            double total = subtotal - discount + taxAmount;

            String taxText;
            String totalText;
            if (taxAmount == (long) taxAmount) {
                taxText = String.format(Locale.US, "%,d CFA", (long) taxAmount);
            } else {
                DecimalFormat formatter = new DecimalFormat("#,##0.00");
                taxText = formatter.format(taxAmount) + " CFA";
            }
            etTax.setText(taxText);

            if (total == (long) total) {
                totalText = String.format(Locale.US, "%,d CFA", (long) total);
            } else {
                DecimalFormat formatter = new DecimalFormat("#,##0.00");
                totalText = formatter.format(total) + " CFA";
            }
            tvTotal.setText(totalText);

            SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
            sharedViewModel.updateSubtotal(subtotal);
            sharedViewModel.updateTax(taxAmount);
            sharedViewModel.updateDiscount(discount);
            sharedViewModel.updateTotal(total);
        } catch (NumberFormatException e) {
            // Handle exception
        }
    }

    private String generateInvoiceID(int id) {
        // Format the invoice ID as "INV00" + id
        return "#INV00" + id;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getClientId().observe(getViewLifecycleOwner(), clientId -> {
            if (clientId != null) {
                fetchClientData(clientId, view);
                clientId1 = clientId;
            }
        });
    }

    private void fetchLastInvoiceID(String userId) {
        String url = "https://invoicemaster.top/getLastInvoiceID.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Create a StringRequest for POST method
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) return;
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.has("invoice_id")) {
                                int lastInvoiceID = jsonResponse.getInt("invoice_id");
                                // Generate new invoice ID
                                String newInvoiceID = generateInvoiceID(lastInvoiceID);
                                EditFragment.this.newInvoiceID1 = String.valueOf(lastInvoiceID);

                                // Set the new invoice ID to the TextView
                                View rootView = getView();
                                if (rootView != null) {
                                    TextView invoiceIDTextView = rootView.findViewById(R.id.invoiceid);
                                    invoiceIDTextView.setText(newInvoiceID);

                                    // Update SharedViewModel with the new Invoice ID
                                    SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
                                    sharedViewModel.updateInvoiceId(newInvoiceID);

                                    // Set current date
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    String currentDate = dateFormat.format(new Date());
                                    TextView invoiceDateTextView = rootView.findViewById(R.id.invoicedate);
                                    invoiceDateTextView.setText("Date: " + currentDate);

                                    // Fetch additional invoice items
                                    fetchInvoiceItems();
                                }
                            } else {
                                // Handle error
                                //Log.e("InvoiceError", "Error: " + jsonResponse.getString("error"));
                            }
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            mc.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Create a Map to hold the parameters
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId); // Add the user_id to the parameters
                return params;
            }
        };

        queue.add(request);
    }

    private void fetchClientData(String clientId, View view) {
        String url = "https://invoicemaster.top/getClientDetails.php?id=" + clientId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (!isAdded()) return;
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String clientName = jsonObject.getString("client_name");
                        String phoneNumber = jsonObject.getString("phone_number");
                        String clientAddress = jsonObject.getString("client_address");

                        // Find TextViews and set their texts
                        TextView tvClientName = view.findViewById(R.id.tvclientname);
                        TextView tvClientPhone = view.findViewById(R.id.tvclientphone);
                        TextView tvClientAddress = view.findViewById(R.id.tvclientaddress);

                        tvClientName.setText(clientName);
                        tvClientPhone.setText(phoneNumber);
                        tvClientAddress.setText(clientAddress);

                        // Show the layout with id billto
                        View billToLayout = view.findViewById(R.id.billto);
                        billToLayout.setVisibility(View.VISIBLE);

                        // Hide the "Add Client" button
                        Button addClientButton = view.findViewById(R.id.add_client);
                        addClientButton.setVisibility(View.GONE);
                        dataFetched = true;

                        // Update SharedViewModel with client details
                        SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
                        sharedViewModel.updateClientName(clientName);
                        sharedViewModel.updateClientAddress(clientAddress);
                        sharedViewModel.updateClientPhone(phoneNumber);

                        // Notify that data has been loaded
                        if (dataLoadListener != null) {
                            dataLoadListener.onDataLoaded();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(requireContext(), "Failed to fetch client data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void fetchInvoiceItems() {
        if (newInvoiceID1 == null) {
            // If there is no invoice ID, stop the shimmer effect and show the "Add Items" button
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            mc.setVisibility(View.VISIBLE);
            return;
        }

        String url = "https://invoicemaster.top/get_invoice_items3.php?invoiceId=" + newInvoiceID1;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (!isAdded()) return;
                    try {
                        if (response.length() == 0) {
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            mc.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<InvoiceItem> invoiceItems = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            int invoiceItemId = jsonObject.getInt("invoice_item_id");
                            int item_Id = jsonObject.getInt("items_id");
                            String itemName = jsonObject.getString("item_name");
                            int quantity = jsonObject.getInt("quantity");
                            int nu = jsonObject.getInt("nu");
                            String unit = jsonObject.getString("unit");
                            double itemPrice = jsonObject.getDouble("item_price");
                            String code = jsonObject.getString("code");
                            double invoice_item_total = jsonObject.getDouble("invoice_item_total");
                            double item_packet_price = jsonObject.getDouble("item_packet_price");
                            double item_carton_price = jsonObject.getDouble("item_carton_price");
                            InvoiceItem invoiceItem = new InvoiceItem(invoiceItemId, item_Id, itemName, quantity, unit, itemPrice, code, invoice_item_total, item_packet_price, item_carton_price, nu);
                            invoiceItems.add(invoiceItem);
                        }

                        // Update SharedViewModelPreview with the fetched invoice items
                        SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
                        sharedViewModel.updateInvoiceItems(invoiceItems);

                        // Hide the Add Items button and close the bottom sheet if the number of items is 30 or more
                        Button addItemsButton2 = getView().findViewById(R.id.add_item_button);
                        if (invoiceItems.size() >= 30) {
                            addItemsButton2.setVisibility(View.GONE);
                            if (itemsBottomSheetFragment != null && itemsBottomSheetFragment.isVisible()) {
                                itemsBottomSheetFragment.dismiss();
                            }
                        } else {
                            addItemsButton2.setVisibility(View.VISIBLE);
                        }

                        if (newInvoiceID1 == null) {
                            recyclerView.setVisibility(View.GONE);
                            View rootView = getView();
                            if (rootView != null) {
                                rootView.findViewById(R.id.add_items).setVisibility(View.VISIBLE);
                            }
                        } else {
                            updateRecyclerView(invoiceItems); // Pass the items you fetched to this method.
                        }
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        mc.setVisibility(View.VISIBLE);
                        fetchTotal();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                }

        );

        queue.add(request);
    }

    private void fetchTotal() {
        String url = "https://invoicemaster.top/get_invoice_total.php";

        // Assuming newInvoiceID1 is the ID of the invoice for which you want to fetch the total
        if (newInvoiceID1 == null || !isAdded()) {
            return;
        }

        // Append the invoice ID to the URL as a query parameter
        url += "?invoices_id=" + newInvoiceID1;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) return;
                        try {
                            double total = response.getDouble("total");
                            View rootView = getView();
                            if (rootView != null) {
                                TextView tvSubtotal = rootView.findViewById(R.id.tvSubtotal);

                                String subtotalText;
                                if (total == (long) total) {
                                    subtotalText = String.format(Locale.US, "%,d CFA", (long) total);
                                } else {
                                    DecimalFormat formatter = new DecimalFormat("#,##0.00");
                                    subtotalText = formatter.format(total) + " CFA";
                                }
                                tvSubtotal.setText(subtotalText);
                                updateTotal();

                                // Notify that data has been loaded
                                if (dataLoadListener != null) {
                                    dataLoadListener.onDataLoaded();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(request);
    }

    private void updateRecyclerView(List<InvoiceItem> items) {
        View rootView = getView();
        if (rootView == null) {
            return;
        }
        LinearLayout line4 = rootView.findViewById(R.id.line4);
        Button addItemsButton = rootView.findViewById(R.id.add_items);
        Button saveButton = rootView.findViewById(R.id.btsavedata);
        LinearLayout line5 = rootView.findViewById(R.id.line5);
        LinearLayout linear3 = rootView.findViewById(R.id.linear3); // Ensure this is the correct ID for "Add Items" button container

        if (adapter == null) {
            adapter = new InvoiceItemAdapter(items, new InvoiceItemAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int invoiceItemId) {
                    // Start the EditInvoiceItemActivity and pass both IDs
                    Intent intent = new Intent(requireContext(), EditInvoiceItemActivity.class);
                    intent.putExtra("invoice_item_id", invoiceItemId);
                    startActivity(intent);
                }

                @Override
                public void onDeleteClick(int invoiceItemId, int position) {
                    deleteInvoiceItem(invoiceItemId, position);
                }
            }, new InvoiceItemAdapter.DataChangeListener() {
                @Override
                public void onDataChanged() {
                    updateUIBasedOnItems(); // This method should exist in EditFragment to update UI based on the adapter's item count
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }

        // Adjusting visibility based on items count
        if (items.isEmpty()) {
            line4.setVisibility(View.GONE); // Hide RecyclerView container
            line5.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            linear3.setVisibility(View.VISIBLE); // Show "Add Items" button container
        } else {
            line5.setVisibility(View.VISIBLE);
            line4.setVisibility(View.VISIBLE); // Show RecyclerView container
            linear3.setVisibility(View.GONE); // Hide "Add Items" button container
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    private void deleteInvoiceItem(int invoiceItemId, int position) {
        String url = "https://invoicemaster.top/delete_invoice_item.php"; // Adjust with your actual deletion endpoint
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Assuming your API expects a POST request with the invoice item ID
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!isAdded()) return;
                    fetchInvoiceItems();
                    // Remove the item from the adapter and notify the change
                    adapter.removeAt(position);

                },
                error -> Toast.makeText(requireContext(), "Failed to delete invoice item", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("invoice_item_id", String.valueOf(invoiceItemId));
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmer();

        // Always fetch the items when resuming, which will update the RecyclerView's adapter.
        fetchInvoiceItems(); // This method should also manage the visibility of RecyclerView and Add Item button.
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
    }

    private void saveDataToDatabase() {
        View rootView = getView();
        if (rootView == null || !isAdded()) {
            return;
        }

        // Collect data
        String newInvoiceID = newInvoiceID1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String status = "Unpaid";
        String discount = ((TextView) rootView.findViewById(R.id.etDiscount)).getText().toString();
        String total = ((TextView) rootView.findViewById(R.id.tvTotal)).getText().toString();
        // Replace placeholders with actual data
        Map<String, String> params = new HashMap<>();
        params.put("invoice_id", newInvoiceID);
        params.put("date", currentDate);
        params.put("status", status);
        params.put("user_id", userid);
        params.put("client_id", clientId1);
        params.put("discount", discount.replaceAll("[^\\d.]", "")); // remove non-numeric characters
        params.put("tax", String.valueOf(taxPercentage));
        params.put("total", total.replaceAll("[^\\d.]", ""));

        String url = "https://invoicemaster.top/saveInvoiceData.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Use StringRequest to get the raw response
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!isAdded()) return;
                    try {
                        // Attempt to convert the raw response to a JSONObject
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        // Handle exception
                    }
                },
                error -> {
                    // Handle error
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        queue.add(request);
    }

    private void updateUIBasedOnItems() {
        View rootView = getView();
        if (rootView == null) {
            return;
        }
        if (adapter.getItemCount() == 0) {
            rootView.findViewById(R.id.line4).setVisibility(View.GONE);
            rootView.findViewById(R.id.line5).setVisibility(View.GONE);
            rootView.findViewById(R.id.linear3).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.line4).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.line5).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.linear3).setVisibility(View.GONE);
        }
    }

    private void showBottomSheetFragment() {
        // Create an instance of the BottomSheetFragment
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        // Show the BottomSheetFragment
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
    }

    private ItemsBottomSheetFragment itemsBottomSheetFragment;

    private void openitemBottomSheet() {
        itemsBottomSheetFragment = new ItemsBottomSheetFragment();

        Bundle args = new Bundle();
        args.putString("newInvoiceID", newInvoiceID1);
        itemsBottomSheetFragment.setArguments(args);

        itemsBottomSheetFragment.show(getParentFragmentManager(), itemsBottomSheetFragment.getTag());
    }

}
