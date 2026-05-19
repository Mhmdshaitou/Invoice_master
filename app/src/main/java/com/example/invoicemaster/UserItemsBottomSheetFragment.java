package com.example.invoicemaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.invoicemaster.AddNewItemActivity;
import com.example.invoicemaster.R;
import com.example.invoicemaster.invoice_item;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserItemsBottomSheetFragment extends BottomSheetDialogFragment {

    public UserItemsBottomSheetFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_items_bottom_sheet, container, false);

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
