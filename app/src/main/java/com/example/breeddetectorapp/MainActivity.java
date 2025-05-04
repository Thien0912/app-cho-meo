package com.example.breeddetectorapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.databinding.ActivityMainBinding;
import com.example.breeddetectorapp.model.CoinResponse;
import com.example.breeddetectorapp.model.GoogleLoginRequest;
import com.example.breeddetectorapp.model.LoginResponse;
import com.example.breeddetectorapp.ui.auth.LoginActivity;
import com.example.breeddetectorapp.ui.auth.RegisterActivity;
import com.example.breeddetectorapp.ui.chat.ChatActivity;
import com.example.breeddetectorapp.ui.deposit.DepositActivity;
import com.example.breeddetectorapp.ui.profile.EditProfileActivity;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // Khởi tạo GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("862055789452-rto0tusn27ta57t6r7nbd2ila01a69rm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        setupUI();
        if (isLoggedIn()) {
            updateUserCoins();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            googleLogin(account.getIdToken(), account.getEmail());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        if (isLoggedIn()) {
            updateUserCoins();
        }
    }

    private boolean isLoggedIn() {
        String token = sharedPreferences.getString("auth_token", "");
        return !token.isEmpty();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupUI() {
        binding.btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        binding.btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        binding.btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void updateUI() {
        if (isLoggedIn()) {
            String username = sharedPreferences.getString("username", "");
            int coins = sharedPreferences.getInt("coins", 0);
            String token = sharedPreferences.getString("auth_token", "");
            Log.d("MainActivity", "Token in updateUI: " + token);

            binding.tvUsername.setText("Chào " + username);
            binding.tvCoins.setText("Coin(s): " + coins);

            binding.btnExplore.setVisibility(View.VISIBLE);
            binding.btnLogin.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.GONE);
            binding.btnGoogleLogin.setVisibility(View.GONE);
            binding.tvGoogleLoginPromo.setVisibility(View.GONE); // Ẩn TextView khi đã đăng nhập

            binding.btnExplore.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            });

            binding.tvUsername.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_user, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.itemLogout) {
                        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                            Log.d("MainActivity", "Google Sign-Out Complete");
                        });

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        Log.d("MainActivity", "auth_token after logout: " + sharedPreferences.getString("auth_token", ""));
                        RetrofitInstance.resetRetrofit();

                        Toast.makeText(MainActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                        updateUI();
                        return true;

                    } else if (itemId == R.id.itemDeposit) {
                        Intent intent = new Intent(MainActivity.this, DepositActivity.class);
                        startActivity(intent);
                        return true;

                    } else if (itemId == R.id.itemEditProfile) {
                        if (token.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Token không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                            redirectToLogin();
                            return true;
                        }
                        Log.d("MainActivity", "Navigating to EditProfileActivity with token: " + token);
                        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            });
        } else {
            binding.tvUsername.setText("Xin chào");
            binding.tvCoins.setText("");
            binding.btnExplore.setVisibility(View.GONE);
            binding.btnLogin.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.VISIBLE);
            binding.btnGoogleLogin.setVisibility(View.VISIBLE);
            binding.tvGoogleLoginPromo.setVisibility(View.VISIBLE); // Hiện TextView khi chưa đăng nhập

            binding.btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
            });

            binding.btnRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });

            binding.tvUsername.setOnClickListener(null);
        }
    }

    private void updateUserCoins() {
        String token = sharedPreferences.getString("auth_token", "");
        if (token.isEmpty()) {
            Log.d("MainActivity", "No auth token, skipping coin update");
            return;
        }

        binding.tvCoins.setText("Đang tải...");

        ApiService apiService = RetrofitInstance.getRetrofit(this).create(ApiService.class);
        Call<CoinResponse> call = apiService.fetchUserCoins();

        call.enqueue(new Callback<CoinResponse>() {
            @Override
            public void onResponse(Call<CoinResponse> call, Response<CoinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newCoins = response.body().getCoins();
                    Log.d("MainActivity", "Updated coins: " + newCoins);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("coins", newCoins);
                    editor.apply();

                    binding.tvCoins.setText("Coin(s): " + newCoins);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không rõ lỗi";
                        Log.e("MainActivity", "Failed to update coins: " + response.code() + " - " + errorBody);
                        binding.tvCoins.setText("Lỗi tải xu");
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error reading errorBody: " + e.getMessage());
                        binding.tvCoins.setText("Lỗi tải xu");
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinResponse> call, Throwable t) {
                Log.e("MainActivity", "Error updating coins: " + t.getMessage());
                binding.tvCoins.setText("Lỗi tải xu");
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            String email = account.getEmail();
            googleLogin(idToken, email);
        } catch (ApiException e) {
            Log.e("MainActivity", "Google Sign-In Failed: " + e.getStatusCode());
            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void googleLogin(String idToken, String email) {
        GoogleLoginRequest request = new GoogleLoginRequest(idToken, email);

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

        api.googleLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MainActivity", "Google Login Success: " + response.body().getMessage());
                    handleSuccessfulLogin(response.body());
                } else {
                    String errorMessage = "Không rõ lỗi";
                    try {
                        errorMessage = response.errorBody() != null
                                ? response.errorBody().string()
                                : "Không nhận được phản hồi hợp lệ";
                    } catch (IOException e) {
                        errorMessage = "Lỗi đọc phản hồi: " + e.getMessage();
                    }
                    Log.e("MainActivity", "Google Login Failed: Code=" + response.code() + ", Error=" + errorMessage);
                    Toast.makeText(MainActivity.this, "Đăng nhập Google thất bại. Mã lỗi: " + response.code() + " - " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("MainActivity", "Google Login Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
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

        Log.d("MainActivity", "Saved auth_token: " + token);

        updateUI();
    }
}