package com.example.invoicemaster;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SharedViewModelPreview extends ViewModel {
    private final MutableLiveData<String> _invoiceId = new MutableLiveData<>();
    private final MutableLiveData<String> _clientName = new MutableLiveData<>();
    private final MutableLiveData<String> _clientAddress = new MutableLiveData<>();
    private final MutableLiveData<String> _clientPhone = new MutableLiveData<>();
    private final MutableLiveData<Double> _subtotal = new MutableLiveData<>();
    private final MutableLiveData<Double> _tax = new MutableLiveData<>();
    private final MutableLiveData<Double> _discount = new MutableLiveData<>();

    private final MutableLiveData<Double> _total = new MutableLiveData<>();
    private final MutableLiveData<List<InvoiceItem>> _invoiceItems = new MutableLiveData<>();

    public LiveData<List<InvoiceItem>> getInvoiceItems() {
        return _invoiceItems;
    }

    public void updateInvoiceItems(List<InvoiceItem> invoiceItems) {
        _invoiceItems.setValue(invoiceItems);
    }

    // Invoice ID
    public LiveData<String> getInvoiceId() {
        return _invoiceId;
    }

    public void updateInvoiceId(String newInvoiceId) {
        _invoiceId.setValue(newInvoiceId);
    }

    // Client Name
    public LiveData<String> getClientName() {
        return _clientName;
    }

    public void updateClientName(String clientName) {
        _clientName.setValue(clientName);
    }

    // Client Address
    public LiveData<String> getClientAddress() {
        return _clientAddress;
    }

    public void updateClientAddress(String clientAddress) {
        _clientAddress.setValue(clientAddress);
    }

    // Client Phone
    public LiveData<String> getClientPhone() {
        return _clientPhone;
    }

    public void updateClientPhone(String clientPhone) {
        _clientPhone.setValue(clientPhone);
    }

    // Subtotal
    public LiveData<Double> getSubtotal() {
        return _subtotal;
    }

    public void updateSubtotal(Double subtotal) {
        _subtotal.setValue(subtotal);
    }

    // Tax
    public LiveData<Double> getTax() {
        return _tax;
    }

    public void updateTax(Double tax) {
        _tax.setValue(tax);
    }

    // Total
    public LiveData<Double> getTotal() {
        return _total;
    }

    public void updateTotal(Double total) {
        _total.setValue(total);
    }


    public LiveData<Double> getDiscount() {
        return _discount;
    }

    public void updateDiscount(double discount) {
        _discount.setValue(discount);
    }
}

