package com.example.invoicemaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.user.UserMainActivity;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final RequestQueue queue = Volley.newRequestQueue(this);
        final Button loginButton = findViewById(R.id.start);
        final EditText emailEditText = findViewById(R.id.email);
        final EditText passwordEditText = findViewById(R.id.password);

        PreferenceManager preferenceManager = new PreferenceManager(this);
        String userEmail = preferenceManager.getUserEmail();
        String userId = preferenceManager.getUserId();
        String userRole = preferenceManager.getUserRole();
        if (userEmail != null && userId != null) {

            if (userRole.equals("Admin")) {

                Intent intent = new Intent(this, MainActivity.class);

                intent.putExtra("user_id", userId);

                startActivity(intent);
            } else {

                Intent intent = new Intent(this, UserMainActivity.class);

                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
            finish();
        } else {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String userEmail = emailEditText.getText().toString();
                    final String userPassword = passwordEditText.getText().toString();

                    // Input validation
                    if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
                        Toast.makeText(login.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    loginButton.setEnabled(false);
                    String url = "https://invoicemaster.top/login.php";

                    // Create a POST request
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Handle response from the server
                                    //Toast.makeText(login.this, response, Toast.LENGTH_SHORT).show();

                                    // Check if login was successful
                                    String[] parts = response.split("\\|");
                                    if (parts.length == 3 && parts[0].trim().equals("Login successful")) {
                                        // Retrieve user role and user ID from the server
                                        String userRole = parts[1].trim();
                                        String userId = parts[2].trim();
                                        // After successful login
                                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("userEmail", userEmail);
                                        editor.putString("userId", userId);
                                        editor.putString("userRole", userRole); // Store the user role

                                        editor.apply();

                                        if (userRole.equals("Admin")) {
                                            Intent intent = new Intent(login.this, MainActivity.class);
                                            intent.putExtra("user_email", userEmail);
                                            intent.putExtra("user_id", userId);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(login.this, UserMainActivity.class);
                                            intent.putExtra("User_email", userEmail);
                                            intent.putExtra("user_id", userId);
                                            startActivity(intent);
                                        }

                                        finish(); // Finish the LoginActivity to prevent going back on pressing the back button
                                    }
                                    loginButton.setEnabled(true);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Handle error
                                    loginButton.setEnabled(true);
                                    Toast.makeText(login.this, "check your internet connection", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            // Parameters to be sent to the PHP script
                            Map<String, String> params = new HashMap<>();
                            params.put("email", userEmail);
                            params.put("password", userPassword);
                            return params;
                        }
                    };

                    // Add the request to the RequestQueue
                    queue.add(request);
                }
            });
        }
    }
}
