package com.example.emailclient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ComposeViewModel extends AndroidViewModel {

    private String draftKey;          // khác null nếu đang sửa một thư nháp
    private boolean sent = false;     // true nếu đã gửi thành công
    private boolean initialized = false;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();   // Toast lỗi
    private final MutableLiveData<Integer> sentMessageRes = new MutableLiveData<>(); // gửi xong: Toast + đóng

    public ComposeViewModel(@NonNull Application application) {
        super(application);
    }

    // Chỉ nhận draftKey ở lần đầu; xoay màn hình thì giữ nguyên
    public void init(String draftKey) {
        if (initialized) return;
        initialized = true;
        this.draftKey = draftKey;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Integer> getMessageRes() { return messageRes; }
    public LiveData<Integer> getSentMessageRes() { return sentMessageRes; }

    public void send(String to, String subject, String body) {
        if (Boolean.TRUE.equals(loading.getValue())) return;   // đang gửi, bỏ qua lần bấm thêm

        if (to.isEmpty()) {
            messageRes.setValue(R.string.error_to_required);
            return;
        }
        if (subject.isEmpty()) {
            messageRes.setValue(R.string.error_subject_required);
            return;
        }

        loading.setValue(true);
        EmailRepository.sendEmail(getApplication(), to, subject, body,
                new ApiClient.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean ok) {
                        loading.postValue(false);
                        if (Boolean.TRUE.equals(ok)) {
                            sent = true;
                            if (draftKey != null) EmailRepository.discardDraft(draftKey);
                            sentMessageRes.postValue(R.string.sent_ok);
                        } else {
                            messageRes.postValue(R.string.error_send_failed);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        loading.postValue(false);
                        messageRes.postValue(R.string.error_send_failed);
                    }
                });
    }

    // Gọi khi màn hình đang đóng. Trả về true nếu vừa lưu một bản nháp.
    public boolean onExit(String to, String subject, String body) {
        if (sent) return false;
        if (!to.isEmpty() || !subject.isEmpty() || !body.trim().isEmpty()) {
            EmailRepository.saveDraft(draftKey, to, subject, body);
            return true;
        } else if (draftKey != null) {
            EmailRepository.discardDraft(draftKey);
        }
        return false;
    }

    public void clearMessage() {
        messageRes.setValue(null); }
    public void clearSentMessage() {
        sentMessageRes.setValue(null); }
}