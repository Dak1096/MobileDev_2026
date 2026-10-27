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

    private MailDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_detail);

        viewModel = new ViewModelProvider(this).get(MailDetailViewModel.class);

        String id = getIntent().getStringExtra("id");
        String folder = getIntent().getStringExtra("folder");
        if (folder == null) folder = "";

        MailStore store = MailStore.get(this);
        Email email = id == null ? null : store.findById(id);
        if (email == null) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        TextView tvSubject = findViewById(R.id.tvDetailSubject);
        TextView tvSender = findViewById(R.id.tvDetailSender);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvBody = findViewById(R.id.tvDetailBody);
        TextView btnStar = findViewById(R.id.btnStar);
        Button btnReply = findViewById(R.id.btnReply);
        Button btnForward = findViewById(R.id.btnForward);
        Button btnSpam = findViewById(R.id.btnSpam);
        Button btnDelete = findViewById(R.id.btnDelete);

        toolbar.setNavigationOnClickListener(v -> finish());

        tvSubject.setText(email.getSubject());
        tvSender.setText(getString(R.string.detail_from) + ": " + email.getSender());
        tvDate.setText(email.getDate());
        tvBody.setText(email.getBody());

        viewModel.init(store, email, folder);

        if (viewModel.isInTrash()) btnDelete.setText(R.string.delete_forever);
        if (viewModel.isInSpam()) btnSpam.setText(R.string.not_spam);

        btnDelete.setOnClickListener(v -> viewModel.onDeleteClicked());
        btnSpam.setOnClickListener(v -> viewModel.onSpamClicked());
        btnStar.setOnClickListener(v -> viewModel.toggleStar());
        btnReply.setOnClickListener(v -> compose("Re: " + email.getSubject(), email.getSender(), email.getBody()));
        btnForward.setOnClickListener(v -> compose("Fwd: " + email.getSubject(), "", email.getBody()));

        viewModel.getStarred().observe(this, s ->
                btnStar.setText(Boolean.TRUE.equals(s) ? "\u2605" : "\u2606"));

        viewModel.getDoneMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearDone();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void compose(String subject, String to, String quotedBody) {
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra("to", to);
        intent.putExtra("subject", subject);
        intent.putExtra("body", "\n\n----------\n" + quotedBody);
        startActivity(intent);
    }
}
