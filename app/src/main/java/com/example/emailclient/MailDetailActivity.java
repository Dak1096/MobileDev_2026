package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;

public class MailDetailActivity extends AppCompatActivity {

    private static String s(String v) {
        return v == null ? "" : v;
    }

    private MailDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        viewModel = new ViewModelProvider(this).get(MailDetailViewModel.class);

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

        viewModel.init(new Email(sender, subject, body, date), folder);

        if (viewModel.isInTrash()) btnDelete.setText(R.string.delete_forever);
        if (viewModel.isInSpam()) btnSpam.setText(R.string.not_spam);

        btnDelete.setOnClickListener(v -> viewModel.onDeleteClicked());
        btnSpam.setOnClickListener(v -> viewModel.onSpamClicked());

        // Thao tác xong: hiện Toast rồi đóng màn hình
        viewModel.getDoneMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearDone();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}