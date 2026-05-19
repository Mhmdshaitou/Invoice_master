package com.example.invoicemaster;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Locale;

public class userreport extends AppCompatActivity {

    private EditText editTextstartDate, editTextendDate;
    private DatePickerDialog datePickerDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userreport);

        editTextstartDate = findViewById(R.id.editTextstartDate);
        editTextendDate = findViewById(R.id.editTextendDate);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
        }

        setUpDatePickers();
        setCurrentDateOnView();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedStartDate = formatDate(editTextstartDate.getText().toString());
                String selectedEndDate = formatDate(editTextendDate.getText().toString());

                Intent intent = new Intent(userreport.this, dashborduserinvoice.class);
                intent.putExtra("selectedStartDate", selectedStartDate);
                intent.putExtra("selectedEndDate", selectedEndDate);
                startActivity(intent);
            }
        });
    }

    private void setUpDatePickers() {
        editTextstartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextstartDate);
            }
        });

        editTextendDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextendDate);
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
        editTextstartDate.setText(currentDate);
        editTextendDate.setText(currentDate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String formatDate(String date) {
        // Split the date string into day, month, and year
        String[] parts = date.split("/");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);

        // Format the date in YYYY-MM-DD format
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }
}