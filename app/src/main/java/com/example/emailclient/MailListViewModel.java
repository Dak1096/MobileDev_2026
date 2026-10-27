package com.example.emailclient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MailListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Email>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> toastRes = new MutableLiveData<>();

    private String folder = "INBOX";
    private String query = "";

    public MailListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Email>> getMessages() { return messages; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Integer> getToastRes() { return toastRes; }

    public void open(String folder) {
        this.folder = folder;
        this.query = "";
        publish();
        String canon = MailStore.canon(folder);
        boolean localOnly = "DRAFTS".equals(canon) || "STARRED".equals(canon);
        if (!localOnly && !MailStore.get(getApplication()).hasSynced(canon)) {
            sync();
        }
    }

    public void reload() {
        publish();
    }

    public void refresh() {
        sync();
    }

    public void setQuery(String q) {
        this.query = q == null ? "" : q;
        publish();
    }

    public void toggleStar(String id) {
        MailStore.get(getApplication()).toggleStar(id);
        publish();
    }

    public void clearToast() {
        toastRes.setValue(null);
    }

    private void publish() {
        MailStore store = MailStore.get(getApplication());
        messages.setValue(query.isEmpty() ? store.getMessages(folder) : store.search(query));
    }

    private void sync() {
        loading.setValue(true);
        EmailRepository.syncFolder(getApplication(), folder, new ApiClient.Callback<List<Email>>() {
            @Override
            public void onSuccess(List<Email> emails) {
                loading.setValue(false);
                publish();
            }

            @Override
            public void onError(Exception e) {
                loading.setValue(false);
                toastRes.setValue(R.string.error_load_failed);
                publish();
            }
        });
    }
}
