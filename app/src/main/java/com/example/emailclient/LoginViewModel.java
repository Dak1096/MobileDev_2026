package com.example.emailclient;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();
    private final MutableLiveData<String> loggedInEmail = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getMessageRes() { return messageRes; }
    public LiveData<String> getLoggedInEmail() { return loggedInEmail; }

    public void login(String email, String password) {
        if (email.isEmpty()) {
            messageRes.setValue(R.string.error_email_required);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageRes.setValue(R.string.error_invalid_email);
            return;
        }
        if (password.isEmpty()) {
            messageRes.setValue(R.string.error_password_required);
            return;
        }
        if (AuthStore.get(getApplication()).login(email, password)) {
            loggedInEmail.setValue(email);
        } else {
            messageRes.setValue(R.string.error_wrong_credentials);
        }
    }

    public void clearMessage() { messageRes.setValue(null); }
    public void clearLoggedIn() { loggedInEmail.setValue(null); }
}
