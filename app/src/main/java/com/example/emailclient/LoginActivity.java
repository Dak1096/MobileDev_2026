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

        if (AuthStore.get(this).isLoggedIn()) {
            goInbox(AuthStore.get(this).currentEmail());
            return;
        }

        setContentView(R.layout.activity_login);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgot = findViewById(R.id.tvForgot);
        TextView tvRegister = findViewById(R.id.tvRegister);

        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("email", etEmail.getText().toString().trim());
            startActivity(intent);
        });
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> viewModel.login(
                etEmail.getText().toString().trim(),
                etPassword.getText().toString()));

        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getLoggedInEmail().observe(this, email -> {
            if (email != null) {
                viewModel.clearLoggedIn();
                goInbox(email);
            }
        });
    }

    private void goInbox(String email) {
        Intent intent = new Intent(this, InboxActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
