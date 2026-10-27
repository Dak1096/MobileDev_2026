package com.example.emailclient;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        tvBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> viewModel.register(
                etEmail.getText().toString().trim(),
                etPassword.getText().toString()));

        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });

        viewModel.getRegisteredEmail().observe(this, email -> {
            if (email != null) {
                viewModel.clearRegistered();
                Toast.makeText(this, R.string.register_ok, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
