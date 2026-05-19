package com.example.invoicemaster;

import android.content.Intent;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Editclients extends AppCompatActivity {

    Button submitButton;
    TextInputLayout layoutName, layoutPhone, layoutAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editclients);


        submitButton = findViewById(R.id.submitbutton);
        layoutName = findViewById(R.id.layoutname);
        layoutPhone = findViewById(R.id.layoutphone);
        layoutAddress = findViewById(R.id.layoutaddress);



        String clientId = getIntent().getStringExtra("client_id");
        if (clientId != null) {
            fetchClientData(clientId);
        }
        // Assuming your Toolbar is defined in your layout file as "@+id/toolbar_add"
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    updateClient(clientId); // Make sure clientId is the correct client ID you're intending to update
                }
            }
        });        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeActionContentDescription("");
            toolbar.setNavigationContentDescription("");

        }

        // Set up text change listeners for the TextInputEditText fields to clear errors on text change
        layoutName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutName.setError(null); // Clear error message
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutPhone.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutPhone.setError(null); // Clear error message
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutAddress.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layoutAddress.setError(null); // Clear error message
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    // Method to validate the form fields
    private boolean validateForm() {
        boolean valid = true;

        // Check if name field is empty
        if (layoutName.getEditText().getText().toString().isEmpty()) {
            layoutName.setError("Name is required");
            valid = false;
        } else {
            layoutName.setError(null);
        }// Check if phone field is empty
        if (layoutPhone.getEditText().getText().toString().isEmpty()) {
            layoutPhone.setError("Phone is required");
            valid = false;
        } else {
            layoutPhone.setError(null);
        }

        // Check if address field is empty
        if (layoutAddress.getEditText().getText().toString().isEmpty()) {
            layoutAddress.setError("Address is required");
            valid = false;
        } else {
            layoutAddress.setError(null);
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
    private void fetchClientData(String clientId) {
        String url = "https://invoicemaster.top/getClientDetails.php?id=" + clientId;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        // Assuming your JSON response structure matches the given PHP script
                        String clientName = jsonObject.getString("client_name");
                        String phoneNumber = jsonObject.getString("phone_number");
                        String clientAddress = jsonObject.getString("client_address");

                        // Populate the input fields
                        layoutName.getEditText().setText(clientName);
                        layoutPhone.getEditText().setText(phoneNumber);
                        layoutAddress.getEditText().setText(clientAddress);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Toast.makeText(Editclients.this, "Error parsing client data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(Editclients.this, "Failed to fetch client data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
    private void updateClient(String clientId) {
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/update_client.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            //Toast.makeText(Editclients.this, response, Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_client_id", clientId);
            resultIntent.putExtra("updated_client_name", layoutName.getEditText().getText().toString());
            resultIntent.putExtra("updated_phone_number", layoutPhone.getEditText().getText().toString());
            resultIntent.putExtra("updated_client_address", layoutAddress.getEditText().getText().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        }, error -> Toast.makeText(Editclients.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("client_id", clientId);
                params.put("client_name", layoutName.getEditText().getText().toString());
                params.put("phone_number", layoutPhone.getEditText().getText().toString());
                params.put("client_address", layoutAddress.getEditText().getText().toString());
                return params;
            }
        };
        queue.add(request);
    }

}
