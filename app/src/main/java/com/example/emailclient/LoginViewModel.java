package com.example.emailclient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();
    private final MutableLiveData<String> loggedInEmail = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
    public LiveData<Integer> getMessageRes() {
        return messageRes;
    }
    public LiveData<String> getLoggedInEmail() {
        return loggedInEmail;
    }

    public void login(String email, String password) {
        if (Boolean.TRUE.equals(loading.getValue())) return;   // đang chờ API, bỏ qua lần bấm thêm

        if (email.isEmpty()) {
            messageRes.setValue(R.string.error_email_required);
            return;
        }
        if (password.isEmpty()) {
            messageRes.setValue(R.string.error_password_required);
            return;
        }

        loading.setValue(true);
        EmailRepository.login(getApplication(), email, new ApiClient.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean ok) {
                loading.postValue(false);
                if (Boolean.TRUE.equals(ok)) {
                    loggedInEmail.postValue(email);
                } else {
                    messageRes.postValue(R.string.error_wrong_credentials);
                }
            }

            @Override
            public void onError(Exception e) {
                loading.postValue(false);
                messageRes.postValue(R.string.error_load_failed);
            }
        });
    }

    public void clearMessage() { messageRes.setValue(null); }
    public void clearLoggedIn() { loggedInEmail.setValue(null); }
}