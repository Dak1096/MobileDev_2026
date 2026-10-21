package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class MailDetailActivity extends AppCompatActivity {

    private static String s(String v) {
        return v == null ? "" : v;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        TextView tvSubject = findViewById(R.id.tvDetailSubject);
        TextView tvSender = findViewById(R.id.tvDetailSender);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvBody = findViewById(R.id.tvDetailBody);
        Button btnSpam = findViewById(R.id.btnSpam);
        Button btnDelete = findViewById(R.id.btnDelete);

        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        String sender = s(intent.getStringExtra("sender"));
        String subject = s(intent.getStringExtra("subject"));
        String date = s(intent.getStringExtra("date"));
        String body = s(intent.getStringExtra("body"));
        String folder = s(intent.getStringExtra("folder"));

        tvSubject.setText(subject);
        tvSender.setText(getString(R.string.detail_from) + ": " + sender);
        tvDate.setText(date);
        tvBody.setText(body);

        final Email email = new Email(sender, subject, body, date);
        final boolean inTrash = folder.contains("Trash");
        final boolean inSpam = folder.contains("Spam");

        if (inTrash) btnDelete.setText(R.string.delete_forever);
        if (inSpam) btnSpam.setText(R.string.not_spam);

        btnDelete.setOnClickListener(v -> {
            if (inTrash) {
                EmailRepository.deleteForever(email);
                Toast.makeText(this, R.string.deleted_forever, Toast.LENGTH_SHORT).show();
            } else {
                EmailRepository.moveEmail(email, "TRASH");
                Toast.makeText(this, R.string.moved_trash, Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        btnSpam.setOnClickListener(v -> {
            if (inSpam) {
                EmailRepository.moveEmail(email, "INBOX");
                Toast.makeText(this, R.string.moved_inbox, Toast.LENGTH_SHORT).show();
            } else {
                EmailRepository.moveEmail(email, "SPAM");
                Toast.makeText(this, R.string.moved_spam, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }
}