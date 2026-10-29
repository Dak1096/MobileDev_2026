package com.example.emailclient;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ForgotPasswordViewModel extends ViewModel {

    // Trạng thái: null = đang ở form, khác null = đã gửi xong (KHÔNG xóa, để sống sót qua xoay màn hình)
    private final MutableLiveData<String> doneEmail = new MutableLiveData<>();
    // Sự kiện một lần: Toast lỗi
    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();

    public LiveData<String> getDoneEmail() { return doneEmail; }
    public LiveData<Integer> getMessageRes() { return messageRes; }

    public void sendReset(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageRes.setValue(R.string.error_invalid_email);
            return;
        }
        // giả lập: chưa gọi server
        doneEmail.setValue(email);
    }

    public void clearMessage() {
        messageRes.setValue(null); }
}