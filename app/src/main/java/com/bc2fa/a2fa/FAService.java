package com.bc2fa.a2fa;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Oleg Levitsky on 06.03.2018.
 */

public interface FAService {
    @Headers({
            "accept: application/json"
    })
    @GET("v1/api/users/verify-number")
    Call<DummyDTO> getVerifyNumber(@Query("phone_number") String phoneNumber, @Query("client_timestamp") String ts);

    @Headers({
            "accept: application/json",
            "Content-Type: application/json"
    })
    @POST("v1/api/users/verify-number")
    Call<DummyDTO> postVerifyNumber(@Body PostVerifyNumberDTO body);

    @Headers({
            "accept: application/json"
    })
    @GET("v1/api/users/code ")
    Call<CodeDTO> getVerificationCode(@Query("phone_number") String phoneNumber, @Query("push_token") String pushToken, @Query("client_timestamp") String ts);

    @Headers({
            "accept: application/json",
            "Content-Type: application/json"
    })
    @POST("v1/api/users/verify")
    Call<DummyDTO> postVerificationCode(@Body PostCodeDTO body);
}
