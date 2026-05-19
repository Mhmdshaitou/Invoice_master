package com.example.invoicemaster;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.invoicemaster.BottomSheetFragment;
import com.example.invoicemaster.R;
import com.example.invoicemaster.SharedViewModel;
import com.example.invoicemaster.viewreport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class Report extends AppCompatActivity {
    private RadioGroup radioGroup1, radioGroup2;
    private RadioButton radioBetweenDates;
    private LinearLayout layoutBetweenDates;
    private DatePickerDialog datePickerDialog;
    private EditText editTextStartDate, editTextEndDate , edittextclient;
    private boolean isUserInteracting = false;
    private SharedViewModel sharedViewModel;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        layoutBetweenDates = findViewById(R.id.layoutBetweenDates);
        radioGroup1 = findViewById(R.id.radioGroupQuickAccess);
        radioGroup2 = findViewById(R.id.radioGroupLastWeek);
        radioBetweenDates = findViewById(R.id.radio_between_dates);
        editTextStartDate = findViewById(R.id.editTextstartDate);
        edittextclient=findViewById(R.id.editTextclient);
        editTextEndDate = findViewById(R.id.editTextendDate);
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }
        isUserInteracting = true;
        Button btnViewReport = findViewById(R.id.btn_view_report);
        Button btnReset = findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields(v);
            }
        });

        edittextclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetFragment();
            }
        });

        setCurrentDateOnView();
        setUpDatePickers();

        setGroupListeners();

        sharedViewModel.getClientId().observe(this, clientId -> {
            if (clientId != null) {
                fetchClientData(clientId, edittextclient);
            }
        });
        btnViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the new activity
                Intent intent = new Intent(Report.this, viewreport.class);
                RadioButton selectedRadioButton1 = findViewById(radioGroup1.getCheckedRadioButtonId());
                String selectedOption1 = "";
                String selectedOption2 = "";

                selectedRadioButton1 = findViewById(radioGroup1.getCheckedRadioButtonId());
                if (selectedRadioButton1 != null) {
                    selectedOption1 = selectedRadioButton1.getText().toString();
                }

                RadioButton selectedRadioButton2 = findViewById(radioGroup2.getCheckedRadioButtonId());
                if (selectedRadioButton2 != null) {
                    selectedOption2 = selectedRadioButton2.getText().toString();
                }

                String selectedOption;
                if (!selectedOption1.isEmpty()) {
                    selectedOption = selectedOption1;
                } else if (!selectedOption2.isEmpty()) {
                    selectedOption = selectedOption2;
                } else {
                    // No radio button is selected, handle this case appropriately
                    // e.g., show a Toast or set a default value for selectedOption
                    selectedOption = "Applytwodate";
                }

                // Get the selected client ID
                String selectedClientId = sharedViewModel.getClientId().getValue();

                // If no client is selected, set selectedClientId to "all"
                if (selectedClientId == null) {
                    selectedClientId = "all";
                }

                intent.putExtra("selectedOption1", selectedOption);
                // Put the selected client ID into the Intent
                intent.putExtra("selectedClientId", selectedClientId);

                // Check if the "Apply filter between two dates" option is selected
                if (radioBetweenDates.isChecked()) {
                    // Get the selected start and end dates
                    String selectedStartDate = editTextStartDate.getText().toString();
                    String selectedEndDate = editTextEndDate.getText().toString();

                    // Put the selected start and end dates into the Intent
                    intent.putExtra("selectedStartDate", selectedStartDate);
                    intent.putExtra("selectedEndDate", selectedEndDate);
                }

                // Start the new activity with the Intent
                startActivity(intent);
            }
        });

    }

    private void fetchClientData(String clientId, final EditText editTextClientName) {
        String url = "https://invoicemaster.top/getClientDetails.php?id=" + clientId;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String clientName = jsonObject.getString("client_name");

                        editTextClientName.setText(clientName);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), "Error parsing client data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Failed to fetch client data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void showBottomSheetFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    private void setUpDatePickers() {
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextStartDate);
            }
        });

        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextEndDate);
            }
        });
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                editText.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year));
            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void setCurrentDateOnView() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String currentDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
        editTextStartDate.setText(currentDate);
        editTextEndDate.setText(currentDate);
    }

    private void setGroupListeners() {
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isUserInteracting) {
                    clearSelectionOutsideGroup(radioGroup1);
                }
            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isUserInteracting) {
                    clearSelectionOutsideGroup(radioGroup2);
                }
            }
        });

        radioBetweenDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserInteracting) {
                    clearSelectionOutsideGroup(null);
                    toggleDateInputs();
                }
            }
        });
    }

    private void clearSelectionOutsideGroup(RadioGroup selectedGroup) {
        isUserInteracting = false;

        if (selectedGroup != radioGroup1) {
            radioGroup1.clearCheck();
        }
        if (selectedGroup != radioGroup2) {
            radioGroup2.clearCheck();
        }

        if (selectedGroup != null) {
            radioBetweenDates.setChecked(false);
            toggleDateInputs();
        }

        isUserInteracting = true;
    }

    private void toggleDateInputs() {
        if (radioBetweenDates.isChecked()) {
            layoutBetweenDates.setVisibility(View.VISIBLE);
        } else {
            layoutBetweenDates.setVisibility(View.GONE);
        }
    }
    public void resetFields(View view) {
        // Clear radio button selections
        radioGroup1.clearCheck();
        radioGroup2.clearCheck();
        radioBetweenDates.setChecked(false);

        // Hide date inputs layout
        layoutBetweenDates.setVisibility(View.GONE);

        // Set start and end dates to the current date
        setCurrentDateOnView();

        // Clear text in client name field
        edittextclient.setText("");

        // Optionally, reset other fields if needed
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
