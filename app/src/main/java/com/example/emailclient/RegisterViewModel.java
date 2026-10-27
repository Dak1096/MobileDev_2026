package com.example.emailclient;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class RegisterViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();
    private final MutableLiveData<String> registeredEmail = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getMessageRes() { return messageRes; }
    public LiveData<String> getRegisteredEmail() { return registeredEmail; }

    public void register(String email, String password) {
        if (email.isEmpty()) {
            messageRes.setValue(R.string.error_email_required);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageRes.setValue(R.string.error_invalid_email);
            return;
        }
        if (password.length() < 6) {
            messageRes.setValue(R.string.error_password_short);
            return;
        }
        if (AuthStore.get(getApplication()).register(email, password)) {
            registeredEmail.setValue(email);
        } else {
            messageRes.setValue(R.string.error_email_exists);
        }
    }

    public void clearMessage() { messageRes.setValue(null); }
    public void clearRegistered() { registeredEmail.setValue(null); }
}
