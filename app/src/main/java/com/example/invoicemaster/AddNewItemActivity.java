

package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class AddNewItemActivity extends AppCompatActivity {

    Button submitButton;
    TextInputLayout   layoutLimit,layoutCostprice, layoutCartonQty,layoutName, layoutCode, layoutCartonNb, layoutPacketNb, layoutItemPrice;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);

        submitButton = findViewById(R.id.submitbutton);
        layoutName = findViewById(R.id.layoutitemname);
        layoutCode = findViewById(R.id.layoutcode);
        layoutLimit = findViewById(R.id.layoutitemlimit);
        layoutCartonNb = findViewById(R.id.layoutcartonnb);
        layoutPacketNb = findViewById(R.id.layoutpacketnb);
        layoutCartonQty = findViewById(R.id.layoutcartonQty);
        layoutCostprice = findViewById(R.id.layoutcostprice);
        layoutItemPrice = findViewById(R.id.layoutpiecep);

        // Assuming your Toolbar is defined in your layout file as "@+id/toolbar_add"
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeActionContentDescription("");
            toolbar.setNavigationContentDescription("");

        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    double cartonPrice = calculateCartonPrice();
                    double packetPrice = calculatePacketPrice();
                    saveItemToDatabase();
                }
            }
        });

        layoutName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        layoutLimit.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutLimit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        layoutCode.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutCode.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        layoutCartonNb.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutCartonNb.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutPacketNb.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutPacketNb.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });layoutCostprice.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutCostprice.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutCartonQty.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutCartonQty.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutItemPrice.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutItemPrice.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        // Check if the fields are empty
        if (layoutName.getEditText().getText().toString().isEmpty()) {
            layoutName.setError("Le nom est requis");
            valid = false;
        } else {
            layoutName.setError(null);
        }
        if (layoutLimit.getEditText().getText().toString().isEmpty()) {
            layoutLimit.setError("La limite est requise");
            valid = false;
        } else {
            layoutLimit.setError(null);
        }
        if (layoutCode.getEditText().getText().toString().isEmpty()) {
            layoutCode.setError("Le code est requis");
            valid = false;
        } else {
            layoutCode.setError(null);
        }
        if (layoutCartonNb.getEditText().getText().toString().isEmpty()) {
            layoutCartonNb.setError("Le numéro de carton est requis");
            valid = false;
        } else {
            layoutCartonNb.setError(null);
        }
        if (layoutPacketNb.getEditText().getText().toString().isEmpty()) {
            layoutPacketNb.setError("Le numéro de paquet est requis");
            valid = false;
        } else {
            layoutPacketNb.setError(null);
        }
        if (layoutCostprice.getEditText().getText().toString().isEmpty()) {
            layoutCostprice.setError("Le prix de revient est requis");
            valid = false;
        } else {
            layoutCostprice.setError(null);
        }
        if (layoutCartonQty.getEditText().getText().toString().isEmpty()) {
            layoutCartonQty.setError("La quantité de carton est requise");
            valid = false;
        } else {
            layoutCartonQty.setError(null);
        }
        if (layoutItemPrice.getEditText().getText().toString().isEmpty()) {
            layoutItemPrice.setError("Le prix de l'article est requis");
            valid = false;
        } else {
            layoutItemPrice.setError(null);
        }

        // Check if the cost price is greater than the item price
        if (valid) {
            double costPrice = Double.parseDouble(layoutCostprice.getEditText().getText().toString());
            double itemPrice = Double.parseDouble(layoutItemPrice.getEditText().getText().toString());

            if (costPrice > itemPrice) {
                layoutCostprice.setError("Le prix de revient ne peut pas être supérieur au prix de l'article");
                valid = false;
            } else {
                layoutCostprice.setError(null);
            }
        }

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the action of the up button
        if (item.getItemId() == android.R.id.home) {
            finish(); // or navigate to the parent activity as appropriate for your app
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private double calculateCartonPrice() {
        int cartonNumber = Integer.parseInt(layoutCartonNb.getEditText().getText().toString());
        double itemPrice = Double.parseDouble(layoutItemPrice.getEditText().getText().toString());

        return cartonNumber * itemPrice;
    }

    private double calculatePacketPrice() {
        int packetNumber = Integer.parseInt(layoutPacketNb.getEditText().getText().toString());
        double itemPrice = Double.parseDouble(layoutItemPrice.getEditText().getText().toString());

        return packetNumber * itemPrice;
    }

    private void saveItemToDatabase() {
        String url = "https://invoicemaster.top/additem.php";
        final RequestQueue queue = Volley.newRequestQueue(this);

        final String name = layoutName.getEditText().getText().toString().trim();
        final String code = layoutCode.getEditText().getText().toString().trim();
        final String cartonNb = layoutCartonNb.getEditText().getText().toString().trim();
        final String packetNb = layoutPacketNb.getEditText().getText().toString().trim();
        final String cartonQty = layoutCartonQty.getEditText().getText().toString().trim();
        final String costPrice = layoutCostprice.getEditText().getText().toString().trim();
        final String itemPrice = layoutItemPrice.getEditText().getText().toString().trim();
        final double cartonPrice = calculateCartonPrice();
        final double packetPrice = calculatePacketPrice();
        final String limit = layoutLimit.getEditText().getText().toString().trim();
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Toast.makeText(AddNewItemActivity.this, response, Toast.LENGTH_SHORT).show();

                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(AddNewItemActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("code", code);
                params.put("carton_number", cartonNb);
                params.put("packet_number", packetNb);
                params.put("cartonQty", cartonQty);
                params.put("cost_price", costPrice);
                params.put("item_price", itemPrice);
                params.put("carton_price", String.valueOf(cartonPrice));
                params.put("packet_price", String.valueOf(packetPrice));
                params.put("item_limit", String.valueOf(limit));
                // Adjust this key according to your server requirements
                return params;
            }
        };

        queue.add(request);
    }
}