package com.example.breeddetectorapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.breeddetectorapp.MainActivity;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.databinding.ActivityLoginBinding;
import com.example.breeddetectorapp.model.LoginRequest;
import com.example.breeddetectorapp.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // Kiểm tra nếu đã đăng nhập thì chuyển thẳng đến MainActivity
        if (isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Back button
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Email/Password login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            login(email, password);
        });

        // Register button
        binding.btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean isLoggedIn() {
        String token = sharedPreferences.getString("auth_token", "");
        return !token.isEmpty();
    }

    private void login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build()))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.4/laravel_12_base/public/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d("LoginActivity", "Response code: " + response.code());
                Log.d("LoginActivity", "Response headers: " + response.headers());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LoginActivity", "Login Success: " + response.body().getMessage());
                    handleSuccessfulLogin(response.body());
                } else {
                    String errorMessage = "Unknown error";
                    try {
                        errorMessage = response.errorBody() != null
                                ? response.errorBody().string()
                                : "No valid response received";
                    } catch (IOException e) {
                        errorMessage = "Error reading response: " + e.getMessage();
                    }
                    Log.e("LoginActivity", "Login Failed: Code=" + response.code() + ", Error=" + errorMessage);
                    Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginActivity", "Login Error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSuccessfulLogin(LoginResponse response) {
        String name = response.getUser().getName();
        int coins = response.getUser().getCoins();
        int userId = response.getUser().getId();
        String token = response.getToken();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", name);
        editor.putInt("coins", coins);
        editor.putInt("user_id", userId);
        editor.putString("auth_token", token);
        editor.apply();

        Log.d("LoginActivity", "Saved auth_token: " + token);
        Toast.makeText(LoginActivity.this, "Xin chào " + name + " - Coins: " + coins, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}