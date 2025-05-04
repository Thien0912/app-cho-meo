package com.example.breeddetectorapp.api;

import com.example.breeddetectorapp.model.ChatResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ChatBotApi {

    @Multipart
    @POST("chatbot/analyze")  // URL API của bạn
    Call<ChatResponse> analyzeImage(
            @Query("vector_store_path") String vectorStorePath,  // Truyền thêm tham số nếu cần
            @Part MultipartBody.Part file  // Phần này để gửi ảnh
    );
}