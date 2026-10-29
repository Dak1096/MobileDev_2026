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

    private EditText etTo, etSubject, etBody;
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

        // Nếu mở từ mục Drafts thì điền sẵn dữ liệu
        Intent in = getIntent();
        viewModel.init(in.getStringExtra("draftKey"));
        etTo.setText(in.getStringExtra("to"));
        etSubject.setText(in.getStringExtra("subject"));
        etBody.setText(in.getStringExtra("body"));

        btnSend.setOnClickListener(v -> viewModel.send(
                etTo.getText().toString().trim(),
                etSubject.getText().toString().trim(),
                etBody.getText().toString()));

        // Khóa/mở nút Gửi theo trạng thái đang gửi
        viewModel.getLoading().observe(this, loading ->
                btnSend.setEnabled(!Boolean.TRUE.equals(loading)));

        // Toast lỗi (hiện một lần)
        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearMessage();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
            }
        });

        // Gửi thành công: Toast rồi đóng màn hình
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
        // Thoát màn hình mà chưa gửi: ViewModel quyết định lưu nháp hay bỏ
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