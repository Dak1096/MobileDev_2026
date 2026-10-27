package com.example.emailclient;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MailDetailViewModel extends ViewModel {

    private Email email;
    private boolean inTrash;
    private boolean inSpam;
    private boolean finished;
    private MailStore store;

    private final MutableLiveData<Integer> doneMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> starred = new MutableLiveData<>(false);

    public void init(MailStore store, Email email, String folder) {
        if (this.email != null) return;
        this.store = store;
        this.email = email;
        this.inTrash = folder.contains("Trash");
        this.inSpam = folder.contains("Spam");
        store.markRead(email.getId());
        starred.setValue(email.isStarred());
    }

    public boolean isInTrash() { return inTrash; }
    public boolean isInSpam() { return inSpam; }
    public LiveData<Integer> getDoneMessageRes() { return doneMessageRes; }
    public LiveData<Boolean> getStarred() { return starred; }

    public void toggleStar() {
        if (email == null) return;
        store.toggleStar(email.getId());
        starred.setValue(!Boolean.TRUE.equals(starred.getValue()));
    }

    public void onDeleteClicked() {
        if (finished || email == null) return;
        finished = true;
        if (inTrash) {
            store.deleteForever(email.getId());
            doneMessageRes.setValue(R.string.deleted_forever);
        } else {
            store.move(email.getId(), "TRASH");
            doneMessageRes.setValue(R.string.moved_trash);
        }
    }

    public void onSpamClicked() {
        if (finished || email == null) return;
        finished = true;
        if (inSpam) {
            store.move(email.getId(), "INBOX");
            doneMessageRes.setValue(R.string.moved_inbox);
        } else {
            store.move(email.getId(), "SPAM");
            doneMessageRes.setValue(R.string.moved_spam);
        }
    }

    public void clearDone() {
        doneMessageRes.setValue(null);
    }
}
