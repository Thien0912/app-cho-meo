package com.example.breeddetectorapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.breeddetectorapp.MainActivity;
import com.example.breeddetectorapp.databinding.ActivityRegisterBinding;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.model.RegisterRequest;
import com.example.breeddetectorapp.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút Trở về (ImageButton)
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.editName.getText().toString().trim();
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            register(name, email, password);
        });

        binding.btnBackToLogin.setOnClickListener(v -> {
            finish(); // Quay lại LoginActivity
        });
    }

    private void register(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest(name, email, password);
        // Truyền this làm Context để RetrofitInstance lấy token từ SharedPreferences
        ApiService api = RetrofitInstance.getRetrofit(this).create(ApiService.class);

        api.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Email đã tồn tại hoặc lỗi khác", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}