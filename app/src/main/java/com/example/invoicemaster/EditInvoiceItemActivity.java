package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
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


public class EditInvoiceItemActivity extends AppCompatActivity {
    TextInputLayout layoutItemName, layoutCode, layoutPiecePrice,layoutUnit,layoutQuantity;
    AutoCompleteTextView autoCompleteTextViewUnit;
    int invoiceItemId;
    Button submitbutton;
    JSONObject jsonObject;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_invoice_item);
        layoutItemName = findViewById(R.id.layoutitemname);
        layoutCode = findViewById(R.id.layoutcode);
        layoutQuantity = findViewById(R.id.layoutquantity);
        layoutUnit = findViewById(R.id.layoutunit);
        layoutPiecePrice = findViewById(R.id.layoutpiecep);
        submitbutton = findViewById(R.id.submitbutton);
        String[] unitItems = {"PCS", "PQT", "CTS"};


        autoCompleteTextViewUnit = findViewById(R.id.autoCompleteTextViewRole);
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

        invoiceItemId = getIntent().getIntExtra("invoice_item_id", -1);
        fetchData(String.valueOf(invoiceItemId));

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
    private void fetchData(String invoiceItemId) {
        String url = "https://invoicemaster.top/get_invoiceitem_details.php?id=" + invoiceItemId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    jsonObject = new JSONObject(response);
                    layoutItemName.getEditText().setText(jsonObject.getString("item_name"));
                    layoutCode.getEditText().setText(jsonObject.getString("code"));
                    layoutQuantity.getEditText().setText(jsonObject.getString("quantity"));
                    autoCompleteTextViewUnit.setText(jsonObject.getString("unit"), false);
                    String unit = ((AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewRole)).getText().toString();
                    if (unit.equals("PCS")) {
                        layoutPiecePrice.getEditText().setText(jsonObject.getString("item_price"));
                    } else if (unit.equals("PQT")) {
                        layoutPiecePrice.getEditText().setText(jsonObject.getString("item_packet_price"));
                    } else {
                        layoutPiecePrice.getEditText().setText(jsonObject.getString("item_carton_price"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void updateItemPrice(String selectedUnit) {
        double packetPrice = 0;
        double cartonPrice = 0;
        double piecePrice = 0;
        try {
            piecePrice = jsonObject.getDouble("item_price");
            packetPrice = jsonObject.getDouble("item_packet_price");
            cartonPrice = jsonObject.getDouble("item_carton_price");
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
        String unit = autoCompleteTextViewUnit.getText().toString();
        String itemPriceStr = layoutPiecePrice.getEditText().getText().toString();

        double itemPrice = Double.parseDouble(itemPriceStr);

        double packetPrice = 0;
        double cartonPrice = 0;

        // Determine the price to update based on the selected unit
        if (unit.equals("PQT")) {
            packetPrice = itemPrice;
        } else if (unit.equals("CTS")) {
            cartonPrice = itemPrice;
        }else if (unit.equals("PCS")) {

        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/update_invoice_item.php";

        double finalPacketPrice = packetPrice;
        double finalCartonPrice = cartonPrice;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle response from the server
                        finish();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Add parameters to the request
                params.put("invoice_item_id", String.valueOf(invoiceItemId));
                params.put("item_name", itemName);
                params.put("code", code);
                params.put("quantity", quantityStr);
                params.put("unit", unit);

                if (unit.equals("PQT")) {
                    params.put("item_packet_price", String.valueOf(finalPacketPrice));
                } else if (unit.equals("CTS")) {
                    params.put("item_carton_price", String.valueOf(finalCartonPrice));
                }else if (unit.equals("PCS")) {
                    params.put("item_price", String.valueOf(itemPrice));
                }

                return params;
            }
        };

        // Add the request to the RequestQueue
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