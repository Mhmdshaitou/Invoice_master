package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class mangestockdetails extends AppCompatActivity {
    private EditText editTextDate;
    private EditText editTextQuantity; // Declare editTextQuantity at the class level
    Button btnadd, btncencel;
    TextView tvConnectionError;
    LinearLayout errorLayout,buttonslayout;
    Button btnRefresh;
    ShimmerFrameLayout shimmerFrameLayout;
    DatePickerDialog datapickerdialog;
    private RecyclerView recyclerView;
    private StockOperationAdapter adapter;
    private List<StockOperation> stockOperations;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mangestockdetails);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }

        Intent intent = getIntent();
        if (intent != null) {
            String itemId = intent.getStringExtra("item_id");

        }

        Button btnIn = findViewById(R.id.btnIn);
        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStockDialog();
            }
        });
        Button btnOut = findViewById(R.id.btnOut);
        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOutStockDialog();
            }
        });


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockOperations = new ArrayList<>();
        adapter = new StockOperationAdapter(stockOperations);
        recyclerView.setAdapter(adapter);
        shimmerFrameLayout=findViewById(R.id.shimmer);
        errorLayout = findViewById(R.id.error_layout);
        buttonslayout=findViewById(R.id.buttons_layout);
        shimmerFrameLayout.startShimmer();
        tvConnectionError =findViewById(R.id.tv_connection_error);
        btnRefresh = findViewById(R.id.btn_refresh);
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the TextView and Button
                tvConnectionError.setVisibility(View.GONE);
                btnRefresh.setVisibility(View.GONE);

                // Show the ShimmerFrameLayout
                shimmerFrameLayout.startShimmer();
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                buttonslayout.setVisibility(View.GONE);

                // Fetch data again
                fetchStockOperations();
            }
        });

        fetchStockOperations();

    }
    private void fetchStockOperations() {
        stockOperations.clear();
        String itemId = getIntent().getStringExtra("item_id");
        String apiUrl = "https://invoicemaster.top/get_stock_operations.php?itemid=" + itemId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                int id = jsonObject.getInt("id");
                                int itemId = jsonObject.getInt("item_id");
                                String operationType = jsonObject.getString("operation_type");
                                int quantity = jsonObject.getInt("quantity");
                                int currentQty = jsonObject.getInt("current_qty");
                                Timestamp operationDate = Timestamp.valueOf(jsonObject.getString("operation_date"));

                                StockOperation stockOperation = new StockOperation(id, itemId, operationType, quantity, currentQty, operationDate);
                                stockOperations.add(stockOperation);
                            }

                            // Sort the stockOperations list in descending order based on the id
                            stockOperations.sort((operation1, operation2) -> operation2.getId() - operation1.getId());
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            buttonslayout.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            handleErrorFetchingData(new VolleyError(e));

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Network Request", "Error: " + error.getMessage());
                        handleErrorFetchingData(error);
                    }
                });
        queue.add(request);
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Update EditText with selected date
                        editTextDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }
    private void handleErrorFetchingData(VolleyError error) {
        error.printStackTrace();
        // Hide the ShimmerFrameLayout and RecyclerView


        // Get the TextView and Button instances

        // Initially hide the TextView and Button
        tvConnectionError.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);

        // Delay showing the TextView and Button for 2 seconds (2000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Show the TextView and Button after the delay
                errorLayout.setVisibility(View.VISIBLE);
                tvConnectionError.setVisibility(View.VISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                buttonslayout.setVisibility(View.GONE);
            }
        }, 2000); // Adjust the delay time (in milliseconds) as needed

       // Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }
    private void showAddStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_stock, null);
        builder.setView(dialogView);

        editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        editTextDate = dialogView.findViewById(R.id.editTextDate);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set the default date in the EditText
        editTextDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datapickerdialog = new DatePickerDialog(mangestockdetails.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                    }
                }, year, month, dayOfMonth);
                datapickerdialog.show();
            }
        });

        Button btnAdd = dialogView.findViewById(R.id.buttonAdd);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);

        final AlertDialog dialog = builder.create(); // Instantiate the dialog here

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Dismiss the dialog here
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the quantity entered by the user

                Button btnAdd = dialogView.findViewById(R.id.buttonAdd);
                String enteredQuantity = editTextQuantity.getText().toString().trim();
                btnAdd.setEnabled(false);
                if (!enteredQuantity.isEmpty()) {
                    // If the entered quantity is not empty, proceed with the API call

                    // Get the item ID from the intent
                    Intent intent = getIntent();
                    final String itemId; // Declare itemId as final
                    if (intent != null) {
                        itemId = intent.getStringExtra("item_id");
                    } else {
                        // Handle the case where the intent is null or item_id is not present
                        //Toast.makeText(mangestockdetails.this, "Item ID not found", Toast.LENGTH_SHORT).show();
                        return; // Exit the onClick method
                    }

                    // Check if itemId is null or empty
                    if (itemId == null || itemId.isEmpty()) {
                        // Handle the case where itemId is null or empty
                       // Toast.makeText(mangestockdetails.this, "Invalid Item ID", Toast.LENGTH_SHORT).show();
                        return; // Exit the onClick method
                    }

                    // Construct the API URL to retrieve the current carton quantity
                    String apiUrl = "https://invoicemaster.top/get_current_carton_qty.php?item_id=" + itemId;

                    // Make the API call to retrieve the current carton quantity
                    RequestQueue queue = Volley.newRequestQueue(mangestockdetails.this);
                    StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Parse the response to get the current carton quantity
                                    int currentCartonQty = Integer.parseInt(response);

                                    // Calculate the new carton quantity
                                    int enteredQuantityInt = Integer.parseInt(enteredQuantity);
                                    int newCartonQty = currentCartonQty + enteredQuantityInt;

                                    // Construct the API URL to update the carton quantity
                                    String updateApiUrl = "https://invoicemaster.top/update_carton_qty.php";

                                    // Make the API call to update the carton quantity
                                    StringRequest updateRequest = new StringRequest(Request.Method.POST, updateApiUrl,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    // Handle the response from the API
                                                    // This may include showing a success message or updating UI
                                                   // Toast.makeText(mangestockdetails.this, "Carton quantity updated successfully", Toast.LENGTH_SHORT).show();

                                                    // Now, make another API call to save the stock operation
                                                    saveStockOperation(itemId, "add", enteredQuantity, currentCartonQty, newCartonQty );

                                                    dialog.dismiss(); // Dismiss the dialog here
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    // Handle error responses from the API
                                                    Toast.makeText(mangestockdetails.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }) {
                                        @Override
                                        protected Map<String, String> getParams() {
                                            // Set parameters for the API call
                                            Map<String, String> params = new HashMap<>();
                                            params.put("item_id", itemId);
                                            params.put("new_carton_qty", String.valueOf(newCartonQty));
                                            return params;
                                        }
                                    };
                                    queue.add(updateRequest);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Handle error responses from the API
                                    //Toast.makeText(mangestockdetails.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    queue.add(request);
                } else {
                    // If the entered quantity is empty, show an error message to the user
                    editTextQuantity.setError("Quantity is required");
                }
            }
        });





        dialog.show(); // Show the dialog after setting up everything
    }

    private boolean validateForm() {
        boolean valid = true;

        if (editTextQuantity.getText().toString().isEmpty()) {
            editTextQuantity.setError("Quantity is required");
            valid = false;
        } else {
            editTextQuantity.setError(null);
        }

        return valid;
    }
    private void saveStockOperation(String itemId, String operationType, String quantity, int currentCartonQty, int newCartonQty) {
        // Construct the API URL to save the stock operation


        String operationDate = editTextDate.getText().toString().trim();
        String saveOperationApiUrl = "https://invoicemaster.top/save_stock_operation.php";

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            java.util.Date date = inputFormat.parse(operationDate);
            operationDate = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Get the current date


        // Make the API call to save the stock operation
        RequestQueue queue = Volley.newRequestQueue(mangestockdetails.this);
        String finalOperationDate = operationDate;
        StringRequest request = new StringRequest(Request.Method.POST, saveOperationApiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the API
                        // This may include showing a success message or updating UI
                        //Toast.makeText(mangestockdetails.this, response, Toast.LENGTH_SHORT).show();
                        fetchStockOperations();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error responses from the API
                        //Toast.makeText(mangestockdetails.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Set parameters for the API call
                Map<String, String> params = new HashMap<>();
                params.put("item_id", itemId);
                params.put("operation_type", operationType);
                params.put("quantity", quantity);
                params.put("current_qty", String.valueOf(newCartonQty));
                params.put("operation_date", finalOperationDate);
                return params;
            }
        };
        queue.add(request);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showOutStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_out_stock, null);
        builder.setView(dialogView);

        editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        editTextDate = dialogView.findViewById(R.id.editTextDate);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Set the default date in the EditText
        editTextDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datapickerdialog = new DatePickerDialog(mangestockdetails.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                    }
                }, year, month, dayOfMonth);
                datapickerdialog.show();
            }
        });

        Button btnOut = dialogView.findViewById(R.id.buttonOut);
         Button btnCancel = dialogView.findViewById(R.id.buttonCancel);

        final AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnOut.setText("Out"); // Change button text to "Out"
        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredQuantity = editTextQuantity.getText().toString().trim();
                if (!enteredQuantity.isEmpty()) {
                    // If the entered quantity is not empty, proceed with the "out" operation
                    btnOut.setEnabled(false);


                    // Get the item ID from the intent
                    Intent intent = getIntent();
                    final String itemId;
                    if (intent != null) {
                        itemId = intent.getStringExtra("item_id");
                    } else {
                        // Handle the case where the intent is null or item_id is not present
                        //Toast.makeText(mangestockdetails.this, "Item ID not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if itemId is null or empty
                    if (itemId == null || itemId.isEmpty()) {
                        // Handle the case where itemId is null or empty
                       // Toast.makeText(mangestockdetails.this, "Invalid Item ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Construct the API URL to retrieve the current carton quantity
                    String apiUrl = "https://invoicemaster.top/get_current_carton_qty.php?item_id=" + itemId;

                    // Make the API call to retrieve the current carton quantity
                    RequestQueue queue = Volley.newRequestQueue(mangestockdetails.this);
                    StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    int currentCartonQty = Integer.parseInt(response);
                                    int enteredQuantityInt = Integer.parseInt(enteredQuantity);
                                    int newCartonQty = currentCartonQty - enteredQuantityInt; // Subtract quantity for "out" operation

                                    // Construct the API URL to update the carton quantity
                                    String updateApiUrl = "https://invoicemaster.top/update_carton_qty.php";

                                    // Make the API call to update the carton quantity
                                    StringRequest updateRequest = new StringRequest(Request.Method.POST, updateApiUrl,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    //Toast.makeText(mangestockdetails.this, "Carton quantity updated successfully", Toast.LENGTH_SHORT).show();
                                                    saveStockOperation(itemId, "out", enteredQuantity, currentCartonQty, newCartonQty);
                                                    dialog.dismiss();
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Toast.makeText(mangestockdetails.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }) {
                                        @Override
                                        protected Map<String, String> getParams() {
                                            Map<String, String> params = new HashMap<>();
                                            params.put("item_id", itemId);
                                            params.put("new_carton_qty", String.valueOf(newCartonQty));
                                            return params;
                                        }
                                    };
                                    queue.add(updateRequest);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //Toast.makeText(mangestockdetails.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    queue.add(request);
                } else {
                    editTextQuantity.setError("Quantity is required");
                }
            }
        });

        dialog.show();

    }




}