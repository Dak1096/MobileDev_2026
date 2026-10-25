package com.example.emailclient;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private AppCompatButton btnSendReset;
    private View layoutForm, layoutDone;
    private TextView tvDoneMessage;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

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
        btnSendReset.setOnClickListener(v ->
                viewModel.sendReset(etEmail.getText().toString().trim()));

        // Toast lỗi (hiện một lần)
        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                viewModel.clearMessage();
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
            }
        });

        // Đã gửi xong: chuyển sang màn thông báo
        viewModel.getDoneEmail().observe(this, email -> {
            if (email != null) showDone(email);
        });
    }

    private void showDone(String email) {
        btnSendReset.setEnabled(false);
        tvDoneMessage.setText(getString(R.string.done_message, email));
        layoutForm.setVisibility(View.GONE);
        layoutDone.setVisibility(View.VISIBLE);
    }
}