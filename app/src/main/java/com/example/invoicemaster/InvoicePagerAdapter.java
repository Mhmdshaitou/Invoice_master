package com.example.invoicemaster;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.invoicemaster.fragments.EditInvoiceFragment;
import com.example.invoicemaster.fragments.PreviewInvoiceFragment;

public class InvoicePagerAdapter extends FragmentStateAdapter {

    private final String invoiceId;

    public InvoicePagerAdapter(@NonNull FragmentActivity fa, String invoiceId) {
        super(fa);
        this.invoiceId = invoiceId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("invoice_id", invoiceId);

        if (position == 0) {
            fragment = new EditInvoiceFragment();
        } else {
            fragment = new PreviewInvoiceFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // We have two fragments
    }
}
