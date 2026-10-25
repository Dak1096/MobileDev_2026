package com.example.emailclient;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MailDetailViewModel extends ViewModel {

    private Email email;
    private boolean inTrash;
    private boolean inSpam;
    private boolean finished = false;   // chống bấm đúp

    // Chuỗi Toast cần hiện trước khi đóng màn hình
    private final MutableLiveData<Integer> doneMessageRes = new MutableLiveData<>();

    // Chỉ khởi tạo lần đầu; xoay màn hình thì giữ nguyên
    public void init(Email email, String folder) {
        if (this.email != null) return;
        this.email = email;
        this.inTrash = folder.contains("Trash");
        this.inSpam = folder.contains("Spam");
    }

    public boolean isInTrash() { return inTrash; }
    public boolean isInSpam() { return inSpam; }
    public LiveData<Integer> getDoneMessageRes() { return doneMessageRes; }

    public void onDeleteClicked() {
        if (finished) return;
        finished = true;
        if (inTrash) {
            EmailRepository.deleteForever(email);
            doneMessageRes.setValue(R.string.deleted_forever);
        } else {
            EmailRepository.moveEmail(email, "TRASH");
            doneMessageRes.setValue(R.string.moved_trash);
        }
    }

    public void onSpamClicked() {
        if (finished) return;
        finished = true;
        if (inSpam) {
            EmailRepository.moveEmail(email, "INBOX");
            doneMessageRes.setValue(R.string.moved_inbox);
        } else {
            EmailRepository.moveEmail(email, "SPAM");
            doneMessageRes.setValue(R.string.moved_spam);
        }
    }

    public void clearDone() {
        doneMessageRes.setValue(null); }
}