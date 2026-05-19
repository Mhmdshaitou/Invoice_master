package com.example.invoicemaster;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UserInvoicePagerAdapter extends FragmentStateAdapter {

    private final String userId;

    public UserInvoicePagerAdapter(@NonNull FragmentActivity fa, String userId) {
        super(fa);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("user_id", userId);

        if (position == 0) {
            fragment = new UserEditFragment();
        } else {
            fragment = new PreviewFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // We have two fragments
    }
}
