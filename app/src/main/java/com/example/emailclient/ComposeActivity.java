package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;

public class ComposeActivity extends AppCompatActivity {

    private EditText etTo;
    private EditText etSubject;
    private EditText etBody;
    private ComposeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        viewModel = new ViewModelProvider(this).get(ComposeViewModel.class);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCompose);
        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        Button btnSend = findViewById(R.id.btnSend);

        toolbar.setNavigationOnClickListener(v -> finish());

        Intent in = getIntent();
        String draftId = in.getStringExtra("draftId");
        viewModel.init(draftId);

        if (draftId != null) {
            Email draft = MailStore.get(this).findById(draftId);
            if (draft != null) {
                etTo.setText(draft.getSender());
                etSubject.setText(draft.getSubject());
                etBody.setText(draft.getBody());
            }
        } else {
            etTo.setText(in.getStringExtra("to"));
            etSubject.setText(in.getStringExtra("subject"));
            etBody.setText(in.getStringExtra("body"));
        }

        btnSend.setOnClickListener(v -> viewModel.send(
                etTo.getText().toString().trim(),
                etSubject.getText().toString().trim(),
                etBody.getText().toString()));

        viewModel.getLoading().observe(this, loading ->
                btnSend.setEnabled(!Boolean.TRUE.equals(loading)));

        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearMessage();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSentMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearSentMessage();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            boolean saved = viewModel.onExit(
                    etTo.getText().toString().trim(),
                    etSubject.getText().toString().trim(),
                    etBody.getText().toString());
            if (saved) {
                Toast.makeText(this, R.string.draft_saved, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
