package com.example.emailclient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ComposeViewModel extends AndroidViewModel {

    private String draftId;
    private boolean sent;
    private boolean initialized;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> messageRes = new MutableLiveData<>();
    private final MutableLiveData<Integer> sentMessageRes = new MutableLiveData<>();

    public ComposeViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(String draftId) {
        if (initialized) return;
        initialized = true;
        this.draftId = draftId;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Integer> getMessageRes() { return messageRes; }
    public LiveData<Integer> getSentMessageRes() { return sentMessageRes; }

    public void send(String to, String subject, String body) {
        if (Boolean.TRUE.equals(loading.getValue())) return;

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
                            if (draftId != null) MailStore.get(getApplication()).discardDraft(draftId);
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

    public boolean onExit(String to, String subject, String body) {
        if (sent) return false;
        MailStore store = MailStore.get(getApplication());
        if (!to.isEmpty() || !subject.isEmpty() || !body.trim().isEmpty()) {
            store.saveDraft(draftId, to, subject, body);
            return true;
        } else if (draftId != null) {
            store.discardDraft(draftId);
        }
        return false;
    }

    public void clearMessage() { messageRes.setValue(null); }
    public void clearSentMessage() { sentMessageRes.setValue(null); }
}
