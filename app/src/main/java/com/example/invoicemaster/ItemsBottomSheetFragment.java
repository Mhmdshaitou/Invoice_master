package com.example.invoicemaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ItemsBottomSheetFragment extends BottomSheetDialogFragment {

    public ItemsBottomSheetFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.items_bottom_sheet, container, false);

        // Find the "Add Item" button
        Button addItemButton = view.findViewById(R.id.add_item);

        // Set OnClickListener on the button to start AddClientActivity
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddNewItemActivity.class);
                startActivity(intent);
            }
        });

        // Retrieve newInvoiceID from arguments
        String newInvoiceID = getArguments() != null ? getArguments().getString("newInvoiceID") : "";

        invoice_item invoiceItemFragment = new invoice_item();

        // Pass newInvoiceID to invoice_item
        Bundle bundle = new Bundle();
        bundle.putString("newInvoiceID", newInvoiceID);
        invoiceItemFragment.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.itemsfragmentContainer, invoiceItemFragment)
                .commit();

        return view;
    }
}
