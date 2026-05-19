package com.example.invoicemaster.fragments;

import static java.lang.Double.parseDouble;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.BottomSheetFragment;
import com.example.invoicemaster.EditInvoiceItemActivity;
import com.example.invoicemaster.InvoiceItem;
import com.example.invoicemaster.InvoiceItemAdapter;
import com.example.invoicemaster.ItemsBottomSheetFragment;
import com.example.invoicemaster.R;
import com.example.invoicemaster.SharedViewModel;
import com.example.invoicemaster.SharedViewModelPreview;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditInvoiceFragment extends Fragment {
    private String newInvoiceID1;
    private String invoicedate;
    TextView tvConnectionError;
    Button btnRefresh;
    ShimmerFrameLayout shimmerFrameLayout;
    RelativeLayout icontent;
    private SharedViewModelPreview sharedViewModel;
    private double taxPercentage = 0.0; // Default to 0.0
    private double discountAmount = 0.0;
    private boolean dataFetched = false;
    private String clientId1;
    RelativeLayout mc;

    private RecyclerView recyclerView;
    private InvoiceItemAdapter adapter;
    private List<InvoiceItem> invoiceItems;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_invoice, container, false);
        recyclerView = view.findViewById(R.id.invoiceitems_recycler);
        shimmerFrameLayout = view.findViewById(R.id.shimmer);
        mc = view.findViewById(R.id.mmc);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        Bundle args = getArguments();
        if (args != null) {
            newInvoiceID1 = args.getString("invoice_id");
            fetchLastInvoiceID(newInvoiceID1);
        }
        Button addInvoiceButton = view.findViewById(R.id.btsavedata);
        addInvoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToDatabase();
            }
        });

        Button addClientButton = view.findViewById(R.id.add_client);
        addClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetFragment();
            }
        });

        Button addItemsButton = view.findViewById(R.id.add_items);
        Button addItemsButton2 = view.findViewById(R.id.add_item_button);
        addItemsButton.setOnClickListener(v -> {
            if (!dataFetched) {
                Toast.makeText(getContext(), "Please wait until the data is loaded.", Toast.LENGTH_SHORT).show();
            } else {
                openPreviewFragment();
            }
        });
        addItemsButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreviewFragment();
            }
        });

        View billToLayout = view.findViewById(R.id.billto);
        billToLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dataFetched) {
                    Toast.makeText(requireContext(), "Data not fetched yet", Toast.LENGTH_SHORT).show();
                } else {
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
                                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
            }
        });

        TextView etTax = view.findViewById(R.id.etTax);
        TextView tvTax = view.findViewById(R.id.tvTax);
        TextView tvTotal = view.findViewById(R.id.tvTotal);

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
                                    taxPercentage = Double.parseDouble(taxStr);
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

    private void fetchLastInvoiceID(String invoiceId) {
        String url = "https://invoicemaster.top/getInvoicedetails.php?invoice_id=" + invoiceId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) return;
                        try {
                            int invoiceid = Integer.parseInt(response.getString("invoice_id"));
                            TextView invoiceIDTextView = getView().findViewById(R.id.invoiceid);
                            String clientid = response.getString("client_id");
                            String userId = response.getString("user_id");
                            String date = response.getString("date");
                            String status = response.getString("status");
                            String totalAmount = response.getString("total_amount");
                            String discount = response.getString("discount");
                            TextView tvDiscount = getView().findViewById(R.id.etDiscount);
                            double discount1 = Double.parseDouble(discount);
                            if (discount1 == (long) discount1) {
                                tvDiscount.setText(String.format(Locale.US, "-%d CFA", (long) discount1));
                            } else {
                                tvDiscount.setText(String.format(Locale.US, "-%.2f CFA", discount1));
                            }
                            invoicedate = date;
                            String tax = response.getString("tax");
                            taxPercentage = Double.parseDouble(tax);
                            String formattedTaxPercentage = taxPercentage == Math.floor(taxPercentage) ?
                                    String.format(Locale.getDefault(), "Tax(%d%%)", (int) taxPercentage) :
                                    String.format(Locale.getDefault(), "Tax(%.2f%%)", taxPercentage);
                            TextView tvTax = getView().findViewById(R.id.tvTax);
                            tvTax.setText(formattedTaxPercentage);
                            String newInvoiceID = generateInvoiceID(invoiceid);
                            invoiceIDTextView.setText(newInvoiceID);
                            SharedViewModel sharedViewModel1 = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                            sharedViewModel1.selectClientId(clientid);
                            SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
                            sharedViewModel.updateInvoiceId(newInvoiceID);

                            TextView invoiceDateTextView = getView().findViewById(R.id.invoicedate);
                            invoiceDateTextView.setText("Date: " + date);
                            sharedViewModel1.selectDate("Date: " + date);
                            fetchInvoiceItems();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error parsing last invoice ID", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        fetchInvoiceItems();
    }

    private void updateTotal() {
        try {
            View view = getView();
            if (view == null) return;
            TextView tvSubtotal = view.findViewById(R.id.tvSubtotal);
            TextView tvDiscount = view.findViewById(R.id.etDiscount);
            TextView etTax = view.findViewById(R.id.etTax);
            TextView tvTotal = view.findViewById(R.id.tvTotal);

            double subtotal = Double.parseDouble(tvSubtotal.getText().toString().replaceAll("[^\\d.]", ""));
            double discount = Double.parseDouble(tvDiscount.getText().toString().replaceAll("[^\\d.]", ""));
            double taxAmount = ((subtotal - discount) * taxPercentage) / 100.0;
            double total = subtotal - discount + taxAmount;

            DecimalFormat formatter = new DecimalFormat("#,##0.00");

            etTax.setText(taxAmount == (long) taxAmount ? String.format(Locale.US, "%,d CFA", (long) taxAmount) : formatter.format(taxAmount) + " CFA");
            tvTotal.setText(total == (long) total ? String.format(Locale.US, "%,d CFA", (long) total) : formatter.format(total) + " CFA");

            SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
            sharedViewModel.updateSubtotal(subtotal);
            sharedViewModel.updateTax(taxAmount);
            sharedViewModel.updateDiscount(discount);
            sharedViewModel.updateTotal(total);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error calculating total", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateInvoiceID(int id) {
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

                        TextView tvClientName = view.findViewById(R.id.tvclientname);
                        TextView tvClientPhone = view.findViewById(R.id.tvclientphone);
                        TextView tvClientAddress = view.findViewById(R.id.tvclientaddress);

                        tvClientName.setText(clientName);
                        tvClientPhone.setText(phoneNumber);
                        tvClientAddress.setText(clientAddress);

                        View billToLayout = view.findViewById(R.id.billto);
                        billToLayout.setVisibility(View.VISIBLE);

                        Button addClientButton = view.findViewById(R.id.add_client);
                        addClientButton.setVisibility(View.GONE);
                        dataFetched = true;

                        SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
                        sharedViewModel.updateClientName(clientName);
                        sharedViewModel.updateClientAddress(clientAddress);
                        sharedViewModel.updateClientPhone(phoneNumber);
                        mc.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing client data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Failed to fetch client data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void fetchInvoiceItems() {
        String url = "https://invoicemaster.top/get_invoice_items3.php?invoiceId=" + newInvoiceID1;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (!isAdded()) return;
                    try {
                        List<InvoiceItem> items = new ArrayList<>();
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
                            InvoiceItem item = new InvoiceItem(invoiceItemId, item_Id, itemName, quantity, unit, itemPrice, code, invoice_item_total, item_packet_price, item_carton_price, nu);
                            items.add(item);
                        }

                        // Update the invoice items
                        updateInvoiceItems(items);

                        // Hide the Add Items button and close the bottom sheet if the number of items is 30 or more
                        Button addItemsButton2 = getView().findViewById(R.id.add_item_button);
                        if (items.size() >= 30) {
                            addItemsButton2.setVisibility(View.GONE);
                            if (itemsBottomSheetFragment != null && itemsBottomSheetFragment.isVisible()) {
                                itemsBottomSheetFragment.dismiss();
                            }
                        } else {
                            addItemsButton2.setVisibility(View.VISIBLE);
                        }

                        // Fetch the total
                        fetchTotal();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Failed to fetch invoice items", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }


    private void updateInvoiceItems(List<InvoiceItem> items) {
        if (!isAdded()) return;
        SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);
        sharedViewModel.updateInvoiceItems(items);
        updateRecyclerView(items);
    }

    private void fetchTotal() {
        String url = "https://invoicemaster.top/get_invoice_total.php";

        if (newInvoiceID1 == null) {
            return;
        }

        url += "?invoices_id=" + newInvoiceID1;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) return;
                        try {
                            double total = response.getDouble("total");
                            TextView tvSubtotal = getView().findViewById(R.id.tvSubtotal);
                            String subtotalText;
                            if (total == (long) total) {
                                subtotalText = String.format(Locale.US, "%,d CFA", (long) total);
                            } else {
                                DecimalFormat formatter = new DecimalFormat("#,##0.00");
                                subtotalText = formatter.format(total) + " CFA";
                            }
                            tvSubtotal.setText(subtotalText);
                            updateTotal();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Error parsing the total", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to fetch the total", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(request);
    }

    private void updateRecyclerView(List<InvoiceItem> items) {
        if (adapter == null) {
            adapter = new InvoiceItemAdapter(items, new InvoiceItemAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int invoiceItemId) {
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
                    updateUIBasedOnItems();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }

        updateUIBasedOnItems();
    }


    private void deleteInvoiceItem(int invoiceItemId, int position) {
        String url = "https://invoicemaster.top/delete_invoice_item.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    fetchInvoiceItems();
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

    private void saveDataToDatabase() {
        String newInvoiceID = newInvoiceID1;
        String currentDate = invoicedate;
        String discount = ((TextView) getView().findViewById(R.id.etDiscount)).getText().toString();
        String status = "PAID";
        String total = ((TextView) getView().findViewById(R.id.tvTotal)).getText().toString();

        Map<String, String> params = new HashMap<>();
        params.put("invoice_id", newInvoiceID);
        params.put("date", currentDate);
        params.put("status", status);
        params.put("client_id", clientId1);
        params.put("discount", discount.replaceAll("[^\\d.]", ""));
        params.put("tax", String.valueOf(taxPercentage));
        params.put("total", total.replaceAll("[^\\d.]", ""));

        String url = "https://invoicemaster.top/updateInvoiceData.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!isAdded()) return;
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error parsing JSON response: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Failed to save data. Error: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        queue.add(request);
    }

    private void updateUIBasedOnItems() {
        View view = getView();
        if (view == null) return;

        if (adapter.getItemCount() == 0) {
            view.findViewById(R.id.line4).setVisibility(View.GONE);
            view.findViewById(R.id.line5).setVisibility(View.GONE);
            view.findViewById(R.id.linear3).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.line4).setVisibility(View.VISIBLE);
            view.findViewById(R.id.line5).setVisibility(View.VISIBLE);
            view.findViewById(R.id.linear3).setVisibility(View.GONE);
        }
    }

    private void showBottomSheetFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getParentFragmentManager(), bottomSheetFragment.getTag());
    }
    private ItemsBottomSheetFragment itemsBottomSheetFragment;
    private void openPreviewFragment() {
        itemsBottomSheetFragment = new ItemsBottomSheetFragment();

        Bundle args = new Bundle();
        args.putString("newInvoiceID", newInvoiceID1);
        itemsBottomSheetFragment.setArguments(args);

        itemsBottomSheetFragment.show(getParentFragmentManager(), itemsBottomSheetFragment.getTag());
    }
}
