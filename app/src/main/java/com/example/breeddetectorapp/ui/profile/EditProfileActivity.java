package com.example.breeddetectorapp.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.breeddetectorapp.MainActivity;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.databinding.ActivityEditProfileBinding;
import com.example.breeddetectorapp.model.User;
import com.example.breeddetectorapp.model.UserResponse;
import com.example.breeddetectorapp.model.UpdateUserRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ApiService
        apiService = RetrofitInstance.getRetrofit(this).create(ApiService.class);

        // Lấy thông tin người dùng hiện tại
        fetchUserInfo();

        // Nút lưu
        binding.btnSave.setOnClickListener(v -> saveProfile());

        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void fetchUserInfo() {
        // Lấy user_id và auth_token từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        String token = sharedPreferences.getString("auth_token", "");

        if (userId == -1 || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API để lấy thông tin người dùng
        Call<UserResponse> call = apiService.getUser(userId, "Bearer " + token);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    binding.editName.setText(user.getName());
                    binding.editPhone.setText(user.getPhone());
                    binding.editAddress.setText(user.getAddress());
                } else {
                    Log.d("API Error", "Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(EditProfileActivity.this, "Không thể lấy thông tin người dùng. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        // Lấy dữ liệu từ giao diện
        String name = binding.editName.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        String address = binding.editAddress.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy user_id và auth_token từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        String token = sharedPreferences.getString("auth_token", "");

        if (userId == -1 || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo request để cập nhật thông tin
        UpdateUserRequest request = new UpdateUserRequest(name, phone, address);

        // Gọi API để cập nhật thông tin người dùng
        Call<UserResponse> call = apiService.updateUser(userId, "Bearer " + token, request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", name);
                    editor.apply();
                    // Quay lại MainActivity
                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}