package com.example.breeddetectorapp.ui.chat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton; // Thêm import này
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.breeddetectorapp.MainActivity;
import com.example.breeddetectorapp.R;
import com.example.breeddetectorapp.api.ApiService;
import com.example.breeddetectorapp.api.ChatApi;
import com.example.breeddetectorapp.api.ChatBotApi;
import com.example.breeddetectorapp.api.RetrofitInstance;
import com.example.breeddetectorapp.model.ChatResponse;
import com.example.breeddetectorapp.model.CoinResponse;
import com.example.breeddetectorapp.ui.auth.LoginActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView tvBreedResult, tvResultDetail;
    private Button btnChooseImage, btnCaptureImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private Uri imageUri;
    private SharedPreferences sharedPreferences;
    private File photoFile;
    private int userCoins;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Sửa từ TextView sang ImageButton
        ImageButton btnBack = findViewById(R.id.btnBack);
        imgPreview = findViewById(R.id.imgPreview);
        tvBreedResult = findViewById(R.id.tvBreedResult);
        tvResultDetail = findViewById(R.id.tvResultDetail);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // Khởi tạo ApiService với context để thêm token vào header
        apiService = RetrofitInstance.getRetrofit(this).create(ApiService.class);

        // Lấy số dư xu từ server
        fetchUserCoins();

        // Xử lý sự kiện click cho ImageButton
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnChooseImage.setOnClickListener(v -> {
            if (userCoins >= 1) {
                openFileChooser();
            } else {
                tvBreedResult.setText("Thất bại.");
                tvResultDetail.setText("Bạn đã hết xu. Vui lòng nạp thêm xu để tiếp tục.");
            }
        });

        btnCaptureImage.setOnClickListener(v -> {
            if (userCoins >= 1) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            } else {
                tvBreedResult.setText("Thất bại.");
                tvResultDetail.setText("Bạn đã hết xu. Vui lòng nạp thêm xu để tiếp tục.");
            }
        });
    }

    // Các phương thức khác giữ nguyên
    private void fetchUserCoins() {
        String token = sharedPreferences.getString("auth_token", "");
        if (token.isEmpty()) {
            tvResultDetail.setText("Vui lòng đăng nhập để tiếp tục.");
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Call<CoinResponse> call = apiService.fetchUserCoins();
        call.enqueue(new Callback<CoinResponse>() {
            @Override
            public void onResponse(Call<CoinResponse> call, Response<CoinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userCoins = response.body().getCoins();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("coins", userCoins);
                    editor.apply();
                    Log.d("ChatActivity", "Fetched User Coins: " + userCoins);
                    updateButtonState();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không rõ lỗi";
                        Log.e("ChatActivity", "Failed to fetch coins: " + response.code() + " - " + errorBody);
                        tvResultDetail.setText("Không thể lấy số dư xu: " + errorBody);
                    } catch (IOException e) {
                        Log.e("ChatActivity", "Error reading errorBody: " + e.getMessage());
                        tvResultDetail.setText("Không thể lấy số dư xu.");
                    }
                }
            }

            @Override
            public void onFailure(Call<CoinResponse> call, Throwable t) {
                Log.e("ChatActivity", "Error fetching coins: " + t.getMessage());
                tvResultDetail.setText("Lỗi lấy số dư xu: " + t.getMessage());
            }
        });
    }

    private void updateButtonState() {
        if (userCoins < 1) {
            btnChooseImage.setEnabled(false);
            btnCaptureImage.setEnabled(false);
            btnChooseImage.setAlpha(0.5f);
            btnCaptureImage.setAlpha(0.5f);
        } else {
            btnChooseImage.setEnabled(true);
            btnCaptureImage.setEnabled(true);
            btnChooseImage.setAlpha(1.0f);
            btnCaptureImage.setAlpha(1.0f);
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                tvResultDetail.setText("Quyền camera bị từ chối. Vui lòng cấp quyền để chụp ảnh.");
            }
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.breeddetectorapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private File createImageFile() {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("ChatActivity", "Error creating image file: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                imageUri = data.getData();
                imgPreview.setImageURI(imageUri);
                Log.d("ChatActivity", "Selected Image URI: " + imageUri.toString());
                sendImageToServer(imageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                imgPreview.setImageURI(imageUri);
                Log.d("ChatActivity", "Captured Image URI: " + imageUri.toString());
                sendImageToServer(imageUri);
            }
        }
    }

    private void sendImageToServer(Uri imageUri) {
        tvBreedResult.setText("Đang xử lý...");
        tvResultDetail.setText("Đang tải ảnh lên...");

        try {
            byte[] imageBytes = getBytesFromUri(imageUri);
            Log.d("ChatActivity", "Image byte[] size: " + imageBytes.length);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

            String token = sharedPreferences.getString("auth_token", "");
            Log.d("ChatActivity", "Auth Token: " + (token.isEmpty() ? "Empty" : "Present"));

            if (token.isEmpty()) {
                tvBreedResult.setText("Thất bại.");
                tvResultDetail.setText("Vui lòng đăng nhập để tiếp tục.");
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.4/laravel_12_base/public/api/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ChatApi chatApi = retrofit.create(ChatApi.class);
            Call<ResponseBody> uploadCall = chatApi.uploadImage(body);

            uploadCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("ChatActivity", "Upload Response Code: " + response.code());
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            Log.d("ChatActivity", "Upload Response: " + responseBody);

                            if (response.code() == 200 && responseBody.contains("Xu")) {
                                tvResultDetail.setText("Đang phân tích ảnh...");
                                fetchUserCoins();
                                callAnalyzeApi(body);
                            } else {
                                tvBreedResult.setText("Thất bại.");
                                tvResultDetail.setText("Bạn đã hết xu");
                                fetchUserCoins();
                            }
                        } catch (IOException e) {
                            tvBreedResult.setText("Thất bại.");
                            tvResultDetail.setText("Lỗi xử lý phản hồi: " + e.getMessage());
                        }
                    } else {
                        try {
                            String errorMessage = response.errorBody() != null
                                    ? response.errorBody().string()
                                    : "Không rõ lỗi";
                            Log.e("ChatActivity", "Upload Error: " + errorMessage);

                            if (errorMessage.toLowerCase().contains("xu") || errorMessage.contains("Số dư xu không đủ")) {
                                tvBreedResult.setText("Thất bại.");
                                tvResultDetail.setText("Bạn đã hết xu");
                                fetchUserCoins();
                            } else {
                                tvBreedResult.setText("Thất bại.");
                                tvResultDetail.setText("Trừ xu thất bại: " + errorMessage);
                            }
                        } catch (IOException e) {
                            tvResultDetail.setText("Lỗi xử lý lỗi: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("ChatActivity", "Upload Failed: " + t.getMessage());
                    tvBreedResult.setText("Thất bại.");

                    if (t.getMessage().toLowerCase().contains("xu") || t.getMessage().contains("Số dư xu không đủ")) {
                        tvResultDetail.setText("Bạn đã hết xu");
                        fetchUserCoins();
                    } else {
                        tvResultDetail.setText("Lỗi kết nối server. Vui lòng thử lại sau.");
                    }
                }
            });

        } catch (Exception e) {
            Log.e("ChatActivity", "Image Processing Error: " + e.getMessage());
            tvBreedResult.setText("Thất bại.");
            tvResultDetail.setText("Lỗi xử lý ảnh: " + e.getMessage());
        }
    }

    private void callAnalyzeApi(MultipartBody.Part body) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.chm.adhigtechn.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChatBotApi botApi = retrofit.create(ChatBotApi.class);
        Call<ChatResponse> call = botApi.analyzeImage("app/utils/data/data_vector", body);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                Log.d("ChatActivity", "Analyze Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse res = response.body();
                    String match = res.getMatch();
                    String predictedBreed = res.getPredictedBreed();
                    String detectedBreed = res.getDetectedBreed();

                    Log.d("ChatActivity", "Predicted Breed: " + predictedBreed);
                    Log.d("ChatActivity", "Detected Breed: " + detectedBreed);
                    Log.d("ChatActivity", "Match: " + match);

                    if ("ĐÚNG".equals(match)) {
                        tvBreedResult.setText("Giống: " + predictedBreed);
                    } else if ("SAI".equals(match)) {
                        tvBreedResult.setText("Giống phát hiện: " + detectedBreed);
                    } else {
                        tvBreedResult.setText("Không xác định được.");
                    }

                    String answer = cleanResponse(res.getAnswer());
                    Log.d("ChatActivity", "Answer: " + answer);
                    tvResultDetail.setText(answer);

                } else {
                    Log.e("ChatActivity", "Analyze Response not successful or body is null.");
                    tvBreedResult.setText("Không xác định.");
                    tvResultDetail.setText("Lỗi xử lý AI.");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e("ChatActivity", "Analyze API call failed: " + t.getMessage());
                tvBreedResult.setText("Thất bại.");
                tvResultDetail.setText("Không kết nối được AI: " + t.getMessage());
            }
        });
    }

    private String cleanResponse(String response) {
        if (response != null) {
            response = response.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        }
        return response != null ? response : " Không có mô tả chi tiết.";
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
}