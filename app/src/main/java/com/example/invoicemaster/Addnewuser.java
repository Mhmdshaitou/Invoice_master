package com.example.invoicemaster;

import android.annotation.SuppressLint;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Addnewuser extends AppCompatActivity {

    Button submitButton;
    TextInputLayout layoutEmail, layoutPassword, layoutRole;
    AutoCompleteTextView autoCompleteTextViewRole;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnewuser);

        submitButton = findViewById(R.id.submitbutton);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutRole = findViewById(R.id.layoutRole);
        autoCompleteTextViewRole = findViewById(R.id.autoCompleteTextViewRole);

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }

        setupRoleDropdown();
        setupTextChangeListeners();

        submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                submitUserData();

            }
        });
    }

    private void setupRoleDropdown() {
        String[] roles = new String[]{"Admin", "User"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        autoCompleteTextViewRole.setAdapter(adapter);
    }

    private void setupTextChangeListeners() {
        layoutEmail.getEditText().addTextChangedListener(new ClearErrorTextWatcher(layoutEmail));
        layoutPassword.getEditText().addTextChangedListener(new ClearErrorTextWatcher(layoutPassword));
        autoCompleteTextViewRole.addTextChangedListener(new ClearErrorTextWatcher(layoutRole));
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

    private void submitUserData() {
        String email = layoutEmail.getEditText().getText().toString();
        String password = layoutPassword.getEditText().getText().toString();
        String role = autoCompleteTextViewRole.getText().toString();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/add_user.php";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("success")) {
                            //Toast.makeText(Addnewuser.this, "User added successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity if user added successfully
                        } else {
                        }
                    } catch (JSONException e) {
                    }
                },
                error -> Toast.makeText(Addnewuser.this, "Failed to add user: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password); // Sending the plain password directly
                params.put("role", role);
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

    static class ClearErrorTextWatcher implements TextWatcher {
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
