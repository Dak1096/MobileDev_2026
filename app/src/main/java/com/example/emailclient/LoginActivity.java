package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Nối sang màn hình Forgot password
        TextView tvForgot = findViewById(R.id.tvForgot);
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            intent.putExtra("email", etEmail.getText().toString().trim());
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> viewModel.login(
                etEmail.getText().toString().trim(),
                etPassword.getText().toString()));

        // Khóa/mở nút theo trạng thái đang tải
        viewModel.getLoading().observe(this, loading ->
                btnLogin.setEnabled(!Boolean.TRUE.equals(loading)));

        // Thông báo lỗi (hiện một lần)
        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        // Đăng nhập thành công: mở Inbox rồi đóng màn này
        viewModel.getLoggedInEmail().observe(this, email -> {
            if (email != null) {
                viewModel.clearLoggedIn();
                Intent intent = new Intent(LoginActivity.this, InboxActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });
    }
}