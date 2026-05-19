package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddNewinvoiceitem extends AppCompatActivity {

    TextInputLayout layoutItemName, layoutCode, layoutPiecePrice,layoutUnit,layoutQuantity;
    String itemId;
    String InvoiceID;
    Button submitbutton;
    JSONObject jsonObject;
    @SuppressLint("MissingInflatedId")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_newinvoiceitem);

        layoutItemName = findViewById(R.id.layoutitemname);
        layoutCode = findViewById(R.id.layoutcode);
        layoutQuantity = findViewById(R.id.layoutquantity);
        layoutUnit = findViewById(R.id.layoutunit);
        layoutPiecePrice = findViewById(R.id.layoutpiecep);
        submitbutton = findViewById(R.id.submitbutton);
        String[] unitItems = {"PCS", "PQT", "CTS"};


        AutoCompleteTextView autoCompleteTextViewUnit = findViewById(R.id.autoCompleteTextViewRole);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, unitItems);
        autoCompleteTextViewUnit.setAdapter(adapter);
        autoCompleteTextViewUnit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteTextViewUnit.showDropDown();
                }
            }
        });
        final ViewTreeObserver observer = layoutUnit.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you only call this once by removing the listener after getting the width
                layoutUnit.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int width = layoutUnit.getWidth(); // Get the TextInputLayout width
                autoCompleteTextViewUnit.setDropDownWidth(width); // Set the dropdown width
            }
        });

        // Set default selection to "pc"
        autoCompleteTextViewUnit.setText("PCS", false); // false to prevent triggering the listener

        autoCompleteTextViewUnit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = unitItems[position];
                updateItemPrice(selectedUnit);
                autoCompleteTextViewUnit.clearFocus();
            }
        });

        Intent intent = getIntent();
        InvoiceID = intent.getStringExtra("newInvoiceID");

        // Show the newInvoiceID in a Toast message
        //Toast.makeText(this, "New Invoice ID: " + InvoiceID, Toast.LENGTH_LONG).show();
        // Display the invoice ID in a Toast

        itemId = getIntent().getStringExtra("item_id");

        if (itemId != null) {
            fetchData(itemId);
        }
        //Toast.makeText(this, "id=" + itemId, Toast.LENGTH_SHORT).show();
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }

        findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    submitInvoiceItemData();
                }
            }
        });
        layoutQuantity.getEditText().requestFocus();
    }

    private void fetchData(String itemId) {
        String url = "https://invoicemaster.top/getitemdetails.php?id=" + itemId;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    jsonObject = new JSONObject(response);
                    String itemName = jsonObject.getString("name");
                    String code = jsonObject.getString("code");
                    double piecePrice = jsonObject.getDouble("item_price");

                    layoutItemName.getEditText().setText(itemName);
                    layoutCode.getEditText().setText(code);
                    layoutPiecePrice.getEditText().setText(String.valueOf((int) piecePrice));
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Toast.makeText(AddNewinvoiceitem.this, "Error parsing item data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(AddNewinvoiceitem.this, "Failed to fetch item data", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }
    private void updateItemPrice(String selectedUnit) {
        double packetPrice = 0;
        double cartonPrice = 0;
        double piecePrice = 0;
        try {
            piecePrice = jsonObject.getDouble("item_price");
            packetPrice = jsonObject.getDouble("packet_price");
            cartonPrice = jsonObject.getDouble("carton_price");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (selectedUnit.equals("PQT")) {
            layoutPiecePrice.getEditText().setText(String.valueOf(packetPrice));
        } else if (selectedUnit.equals("CTS")) {
            layoutPiecePrice.getEditText().setText(String.valueOf(cartonPrice));
        } else {
            layoutPiecePrice.getEditText().setText(String.valueOf(piecePrice));
        }
    }

    private void submitInvoiceItemData() {
        String itemName = layoutItemName.getEditText().getText().toString();
        String code = layoutCode.getEditText().getText().toString();
        String quantityStr = layoutQuantity.getEditText().getText().toString();
        String unit = ((AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRole)).getText().toString();
        String itemPriceStr = layoutPiecePrice.getEditText().getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/add_invoice_item.php";

        // Convert quantity to double
        double quantity = Double.parseDouble(quantityStr);

        // Determine the prices based on the selected unit
        double itemPrice;
        double packetPrice;
        double cartonPrice;
        double total;
        if (unit.equals("PCS")) {
            itemPrice = Double.parseDouble(itemPriceStr);
            packetPrice = jsonObject.optDouble("packet_price");
            cartonPrice = jsonObject.optDouble("carton_price");
        } else if (unit.equals("PQT")) {
            packetPrice = Double.parseDouble(itemPriceStr);
            itemPrice = jsonObject.optDouble("item_price");
            cartonPrice = jsonObject.optDouble("carton_price");
        } else {
            cartonPrice = Double.parseDouble(itemPriceStr);
            itemPrice = jsonObject.optDouble("item_price");
            packetPrice = jsonObject.optDouble("packet_price");
        }
        if (unit.equals("PCS")) {
            total = quantity * itemPrice;
        } else if (unit.equals("PQT")) {
            total = quantity * packetPrice;
        } else {
            total = quantity * cartonPrice;
        }
        // Calculate total


        // Prepare parameters for the POST request
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("success")) {
                            //Toast.makeText(AddNewinvoiceitem.this, "Invoice item added successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Optionally close the activity
                        } else {
                            // Toast.makeText(AddNewinvoiceitem.this, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        //Toast.makeText(AddNewinvoiceitem.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(AddNewinvoiceitem.this, "Failed to add invoice item: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("item_name", itemName);
                params.put("code", code);
                params.put("quantity", quantityStr);
                params.put("unit", unit);
                params.put("item_price", String.valueOf(itemPrice));
                params.put("invoice_item_total", String.valueOf(total));
                params.put("invoices_id", InvoiceID);
                params.put("items_id", itemId);
                params.put("item_packet_price", String.valueOf(packetPrice));
                params.put("item_carton_price", String.valueOf(cartonPrice));
                return params;
            }
        };

        queue.add(postRequest);
    }

    private boolean validateForm() {
        boolean valid = true;

        if (layoutItemName.getEditText().getText().toString().isEmpty()) {
            layoutItemName.setError("Item Name is required");
            valid = false;
        } else {
            layoutItemName.setError(null);
        }

        if (layoutQuantity.getEditText().getText().toString().isEmpty()) {
            layoutQuantity.setError("Quantity is required");
            valid = false;
        } else {
            layoutQuantity.setError(null);
        }

        // Add validations for other fields similarly

        return valid;
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

