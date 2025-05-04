package com.example.breeddetectorapp.ui.deposit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton; // Thêm import này
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.breeddetectorapp.R;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.model.DepositRequest;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;

public class DepositActivity extends AppCompatActivity {

    private TextView tvTransactionId;
    private EditText editAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        // Gắn view
        editAmount = findViewById(R.id.editAmount);
        tvTransactionId = findViewById(R.id.tvTransactionId);
        com.google.android.material.button.MaterialButton btnSubmit = findViewById(R.id.btnSubmit); // Cập nhật thành MaterialButton
        ImageButton btnBack = findViewById(R.id.btnBack); // Sửa thành ImageButton

        // Sự kiện trở về
        btnBack.setOnClickListener(v -> finish());

        // Tạo mã giao dịch ngẫu nhiên
        String transactionId = "MOMO_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        tvTransactionId.setText(transactionId);

        btnSubmit.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String momoTransactionId = tvTransactionId.getText().toString();

            // Lấy user_id từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                Toast.makeText(this, "Không xác định được tài khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi API
            DepositRequest depositRequest = new DepositRequest(userId, momoTransactionId, amount);

            ApiService api = RetrofitInstance.getRetrofit(this).create(ApiService.class);
            api.deposit(depositRequest).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DepositActivity.this, "✅ Đã gửi yêu cầu nạp tiền!", Toast.LENGTH_LONG).show();
                        finish(); // Quay lại màn chính
                    } else {
                        Toast.makeText(DepositActivity.this, "❌ Lỗi server: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(DepositActivity.this, "⚠️ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}