package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ComposeActivity extends AppCompatActivity {

    private EditText etTo, etSubject, etBody;
    private String draftKey;      // khác null nếu đang sửa một thư nháp
    private boolean sent = false; // true nếu đã gửi thành công

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCompose);
        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        Button btnSend = findViewById(R.id.btnSend);

        toolbar.setNavigationOnClickListener(v -> finish());

        // Nếu mở từ mục Drafts thì điền sẵn dữ liệu
        Intent in = getIntent();
        draftKey = in.getStringExtra("draftKey");
        etTo.setText(in.getStringExtra("to"));
        etSubject.setText(in.getStringExtra("subject"));
        etBody.setText(in.getStringExtra("body"));

        btnSend.setOnClickListener(v -> {
            final String to = etTo.getText().toString().trim();
            final String subject = etSubject.getText().toString().trim();
            final String body = etBody.getText().toString();

            if (to.isEmpty()) {
                Toast.makeText(this, R.string.error_to_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (subject.isEmpty()) {
                Toast.makeText(this, R.string.error_subject_required, Toast.LENGTH_SHORT).show();
                return;
            }

            btnSend.setEnabled(false);
            EmailRepository.sendEmail(this, to, subject, body,
                    new ApiClient.Callback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean ok) {
                            btnSend.setEnabled(true);
                            if (ok) {
                                sent = true;
                                if (draftKey != null) EmailRepository.discardDraft(draftKey);
                                Toast.makeText(ComposeActivity.this,
                                        R.string.sent_ok, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ComposeActivity.this,
                                        R.string.error_send_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            btnSend.setEnabled(true);
                            Toast.makeText(ComposeActivity.this,
                                    R.string.error_send_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Thoát màn hình mà chưa gửi: lưu thành thư nháp nếu có nội dung
        if (isFinishing() && !sent) {
            String to = etTo.getText().toString().trim();
            String subject = etSubject.getText().toString().trim();
            String body = etBody.getText().toString();
            if (!to.isEmpty() || !subject.isEmpty() || !body.trim().isEmpty()) {
                EmailRepository.saveDraft(draftKey, to, subject, body);
                Toast.makeText(this, R.string.draft_saved, Toast.LENGTH_SHORT).show();
            } else if (draftKey != null) {
                EmailRepository.discardDraft(draftKey);
            }
        }
    }
}