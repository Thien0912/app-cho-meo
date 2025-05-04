package com.example.breeddetectorapp.api;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
    private static final String BASE_URL = "http://192.168.1.4/laravel_12_base/public/api/";
    private static volatile Retrofit retrofit; // Sử dụng volatile để đảm bảo thread-safety

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitInstance.class) { // Đồng bộ hóa để tránh tạo nhiều instance trong môi trường đa luồng
                if (retrofit == null) {
                    // Tạo HttpLoggingInterceptor để debug request/response
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log toàn bộ request và response

                    // Tạo OkHttpClient với Interceptor
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(120, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .writeTimeout(120, TimeUnit.SECONDS)
                            .addInterceptor(logging) // Thêm logging interceptor
                            .addInterceptor(chain -> {
                                // Lấy token mới nhất từ SharedPreferences cho mỗi request
                                SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                                String token = sharedPreferences.getString("auth_token", "");

                                Request request = chain.request().newBuilder()
                                        .addHeader("Accept", "application/json") // Thêm header Accept
                                        .addHeader("Authorization", "Bearer " + token) // Thêm header Authorization
                                        .build();
                                return chain.proceed(request);
                            })
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    // Phương thức để hỗ trợ client tùy chỉnh (giữ nguyên nhưng cải thiện)
    public static Retrofit getRetrofit(OkHttpClient client) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        if (client != null) {
            builder.client(client);
        } else {
            // Nếu không có client tùy chỉnh, sử dụng client mặc định với logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            client = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();
            builder.client(client);
        }

        return builder.build();
    }

    // Phương thức để làm mới Retrofit instance (gọi khi đăng xuất hoặc cần làm mới token)
    public static void resetRetrofit() {
        retrofit = null;
    }
}