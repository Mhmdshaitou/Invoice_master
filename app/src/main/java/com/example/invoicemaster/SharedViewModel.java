package com.example.invoicemaster;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> clientId = new MutableLiveData<>();
    private final MutableLiveData<String> _date = new MutableLiveData<>();
    private final MutableLiveData<String> _tax = new MutableLiveData<>();
    private final MutableLiveData<String> _discount = new MutableLiveData<>();

    public LiveData<String> getDiscount() {return _discount;}
    public void selectClientId(String id) {
        clientId.setValue(id);
    }
    public void selectDate(String id) {
        _date.setValue(id);
    }
    public LiveData<String> getClientId() {
        return clientId;
    }
}
