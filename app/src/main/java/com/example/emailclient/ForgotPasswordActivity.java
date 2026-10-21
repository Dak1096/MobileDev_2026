package com.example.emailclient;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private AppCompatButton btnSendReset;
    private View layoutForm, layoutDone;
    private TextView tvDoneMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        layoutForm = findViewById(R.id.layoutForm);
        layoutDone = findViewById(R.id.layoutDone);
        tvDoneMessage = findViewById(R.id.tvDoneMessage);

        // Điền sẵn email nếu LoginActivity truyền sang
        String prefill = getIntent().getStringExtra("email");
        if (prefill != null) etEmail.setText(prefill);

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
        findViewById(R.id.btnDone).setOnClickListener(v -> finish());
        btnSendReset.setOnClickListener(v -> onSendClicked());
    }

    private void onSendClicked() {
        String email = etEmail.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendReset.setEnabled(false); // tránh bấm 2 lần

        // giả lập
        showDone(email);
    }

    private void showDone(String email) {
        tvDoneMessage.setText(getString(R.string.done_message, email));
        layoutForm.setVisibility(View.GONE);
        layoutDone.setVisibility(View.VISIBLE);
    }
}