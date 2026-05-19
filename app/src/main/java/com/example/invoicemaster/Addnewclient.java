package com.example.invoicemaster;

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

import java.util.HashMap;
import java.util.Map;

public class Addnewclient extends AppCompatActivity {

    Button submitButton;
    TextInputLayout layoutName, layoutPhone, layoutAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewclient);


        submitButton = findViewById(R.id.submitbutton);
        layoutName = findViewById(R.id.layoutname);
        layoutPhone = findViewById(R.id.layoutphone);
        layoutAddress = findViewById(R.id.layoutaddress);

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

        // Perform form validation when user tries to submit
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    String name = layoutName.getEditText().getText().toString();
                    String phone = layoutPhone.getEditText().getText().toString();
                    String address = layoutAddress.getEditText().getText().toString();
                    submitClientData(name, phone, address);
                }
            }
        });

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
    private void submitClientData(String name, String phone, String address) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/addClient.php"; // Change this URL to your PHP script's URL

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Handle response
                    //Toast.makeText(Addnewclient.this, "Client added successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    // Handle error

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("phone", phone);
                params.put("address", address);
                return params;
            }
        };
        queue.add(postRequest);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // or navigate to the parent activity as appropriate for your app
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}