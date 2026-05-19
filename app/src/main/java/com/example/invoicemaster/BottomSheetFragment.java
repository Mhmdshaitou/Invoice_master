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

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private invoice_client invoiceClientFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.clients_bottom_sheet, container, false);

        Button addClientButton = view.findViewById(R.id.addclient);
        addClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Addnewclient.class);
                startActivity(intent);
            }
        });

        invoiceClientFragment = new invoice_client();

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, invoiceClientFragment)
                .commit();

        return view;
    }
}
