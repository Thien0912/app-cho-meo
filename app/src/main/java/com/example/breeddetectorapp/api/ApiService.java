package com.example.breeddetectorapp.api;

import com.example.breeddetectorapp.model.*;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("deposit")
    Call<ResponseBody> deposit(@Body DepositRequest request);

    @POST("google-login")
    Call<LoginResponse> googleLogin(@Body GoogleLoginRequest request);

    @GET("get-coins")
    Call<CoinResponse> fetchUserCoins();

    @GET("user/{id}")
    Call<UserResponse> getUser(@Path("id") int userId, @Header("Authorization") String token);

    @PUT("user/{id}")
    Call<UserResponse> updateUser(@Path("id") int userId, @Header("Authorization") String token, @Body UpdateUserRequest request);
}