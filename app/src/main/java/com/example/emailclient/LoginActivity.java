package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        btnLogin.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, R.string.error_email_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, R.string.error_password_required, Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            EmailRepository.login(this, email, new ApiClient.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean ok) {
                    btnLogin.setEnabled(true);
                    if (ok) {
                        Intent intent = new Intent(LoginActivity.this, InboxActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                R.string.error_wrong_credentials, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(Exception e) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                            R.string.error_load_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}