package com.example.invoicemaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditItemActivity extends AppCompatActivity {

    TextInputLayout layoutLimit,layoutItemName, layoutCode, layoutPiecePrice, layoutCostPrice, layoutCartonQty, layoutCartonNb, layoutPacketNb;
    String itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        layoutItemName = findViewById(R.id.layoutitemname);
        layoutCode = findViewById(R.id.layoutcode);
        layoutPiecePrice = findViewById(R.id.layoutpiecep);
        layoutCostPrice = findViewById(R.id.layoutcostprice);
        layoutCartonQty = findViewById(R.id.layoutcartonQty);
        layoutCartonNb = findViewById(R.id.layoutcartonnb);
        layoutPacketNb = findViewById(R.id.layoutpacketnb);
        layoutLimit = findViewById(R.id.layoutitemlimit);

        itemId = getIntent().getStringExtra("item_id");

        if (itemId != null) {
            fetchData(itemId);
        }
        //Toast.makeText(this, "id="+itemId, Toast.LENGTH_SHORT).show();
        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_24);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeActionContentDescription("");
            toolbar.setNavigationContentDescription("");
        }
        findViewById(R.id.submitbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    updateItem(itemId);
                }
            }
        });
    }

    private void fetchData(String itemId) {
        String url = "https://invoicemaster.top/getitemdetails.php?id=" + itemId;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String itemName = jsonObject.getString("name");
                    String code = jsonObject.getString("code");
                    double piecePrice = jsonObject.getDouble("item_price");
                    double costPrice = jsonObject.getDouble("cost_price");
                    String cartonQty = jsonObject.getString("cartonQty");
                    int cartonNb = jsonObject.getInt("carton_number");
                    int packetNb = jsonObject.getInt("packet_number");
                    int itemLimit = jsonObject.getInt("item_limit");

                    Objects.requireNonNull(layoutLimit.getEditText()).setText(String.valueOf(itemLimit));
                    Objects.requireNonNull(layoutItemName.getEditText()).setText(itemName);
                    Objects.requireNonNull(layoutCode.getEditText()).setText(code);
                    Objects.requireNonNull(layoutPiecePrice.getEditText()).setText(String.valueOf((int) piecePrice));
                    Objects.requireNonNull(layoutCostPrice.getEditText()).setText(String.valueOf((int) costPrice));
                    Objects.requireNonNull(layoutCartonQty.getEditText()).setText(cartonQty);
                    Objects.requireNonNull(layoutCartonNb.getEditText()).setText(String.valueOf(cartonNb));
                    Objects.requireNonNull(layoutPacketNb.getEditText()).setText(String.valueOf(packetNb));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        queue.add(request);
    }




    private void updateItem(String itemId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://invoicemaster.top/update_item.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            //Toast.makeText(EditItemActivity.this, response, Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_item_id", itemId);
            resultIntent.putExtra("updated_item_name", layoutItemName.getEditText().getText().toString());
            // Add other fields similarly
            setResult(RESULT_OK, resultIntent);
            finish();
        }, error -> Toast.makeText(EditItemActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("item_id", itemId);
                params.put("item_name", Objects.requireNonNull(layoutItemName.getEditText()).getText().toString());
                params.put("code", Objects.requireNonNull(layoutCode.getEditText()).getText().toString());
                params.put("piece_price", Objects.requireNonNull(layoutPiecePrice.getEditText()).getText().toString());
                params.put("cost_price", Objects.requireNonNull(layoutCostPrice.getEditText()).getText().toString());
                params.put("carton_qty", Objects.requireNonNull(layoutCartonQty.getEditText()).getText().toString());
                params.put("carton_nb", Objects.requireNonNull(layoutCartonNb.getEditText()).getText().toString());
                params.put("packet_nb", Objects.requireNonNull(layoutPacketNb.getEditText()).getText().toString());
                params.put("item_limit", Objects.requireNonNull(layoutLimit.getEditText()).getText().toString());
                return params;
            }
        };
        queue.add(request);
    }

    private boolean validateForm() {
        boolean valid = true;

        if (Objects.requireNonNull(layoutItemName.getEditText()).getText().toString().isEmpty()) {
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
