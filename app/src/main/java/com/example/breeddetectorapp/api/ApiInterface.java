package com.example.breeddetectorapp.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiInterface {

    // Phương thức để lấy số coins của người dùng
    @GET("api/user/coins")
    Call<ResponseBody> getUserCoins(@Header("Authorization") String authToken);
}
