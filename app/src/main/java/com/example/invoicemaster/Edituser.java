package com.example.invoicemaster;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Edituser extends AppCompatActivity {

    Button submitButton;
    TextInputLayout layoutEmail, layoutPassword, layoutRole;
    AutoCompleteTextView autoCompleteTextViewRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edituser);

        submitButton = findViewById(R.id.submitbutton);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutRole = findViewById(R.id.layoutRole);
        autoCompleteTextViewRole = findViewById(R.id.autoCompleteTextViewRole);

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) {
            fetchUserData(userId);
        } else {
            //Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }

        setupRoleDropdown();
        setupTextChangeListeners();

        submitButton.setOnClickListener(v -> {
            if (userId != -1) {
                updateUserData(userId);
            } else {
                //Toast.makeText(Edituser.this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupRoleDropdown() {
        String[] roles = new String[]{"Admin", "User"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        autoCompleteTextViewRole.setAdapter(adapter);
    }

    private void setupTextChangeListeners() {
        layoutEmail.getEditText().addTextChangedListener(new Addnewuser.ClearErrorTextWatcher(layoutEmail));
        layoutPassword.getEditText().addTextChangedListener(new Addnewuser.ClearErrorTextWatcher(layoutPassword));
        autoCompleteTextViewRole.addTextChangedListener(new Addnewuser.ClearErrorTextWatcher(layoutRole));
    }

    private boolean validateForm() {
        boolean valid = true;

        if (layoutEmail.getEditText().getText().toString().isEmpty()) {
            layoutEmail.setError("Email is required");
            valid = false;
        }
        if (layoutPassword.getEditText().getText().toString().isEmpty()) {
            layoutPassword.setError("Password is required");
            valid = false;
        }
        if (autoCompleteTextViewRole.getText().toString().isEmpty()) {
            layoutRole.setError("Role is required");
            valid = false;
        }

        return valid;
    }

    private void fetchUserData(int userId) {
        String url = "https://invoicemaster.top/getuserdetails.php?user_id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            String email = jsonObject.optString("email");
                            String password = jsonObject.optString("password"); // Consider security implications
                            String role = jsonObject.optString("role");

                            if (layoutEmail.getEditText() != null) {
                                layoutEmail.getEditText().setText(email);
                            }
                            if (layoutPassword.getEditText() != null) {
                                layoutPassword.getEditText().setText(password);
                            }
                            autoCompleteTextViewRole.setText(role, false);
                        } else {
                            //Toast.makeText(Edituser.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Toast.makeText(Edituser.this, "Error parsing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(Edituser.this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void updateUserData(int userId) {
        if (!validateForm()) {
            //Toast.makeText(this, "Validation failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://invoicemaster.top/update_user.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Handle response from the server
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.optString("message", "Update successful");
                        //Toast.makeText(Edituser.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                       // Toast.makeText(Edituser.this, "Error parsing update response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(Edituser.this, "Update failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("email", layoutEmail.getEditText().getText().toString());
                params.put("password", layoutPassword.getEditText().getText().toString());
                params.put("role", autoCompleteTextViewRole.getText().toString());

                return params;
            }
        };

        queue.add(postRequest);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ClearErrorTextWatcher implements TextWatcher {
        private final TextInputLayout textInputLayout;

        ClearErrorTextWatcher(TextInputLayout textInputLayout) {
            this.textInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textInputLayout.setError(null); // Clear the error message as user types.
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}