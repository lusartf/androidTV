package com.example.theoplayertv.api;

import com.example.theoplayertv.models.CategoriasResponse;
import com.example.theoplayertv.models.ChannelResponse;
import com.example.theoplayertv.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @FormUrlEncoded
    @POST("apiv2/credentials/login")
    Call<LoginResponse> userLogin(
        @Field("company_id") String company_id,
        @Field("appid") String appid,
        @Field("app_name") String app_name,
        @Field("api_version") String api_version,
        @Field("appversion") String appversion,
        @Field("auth") String auth
    );

    @FormUrlEncoded
    Call<LoginResponse> userLogout(
        @Field("auth") String auth
    );

    @FormUrlEncoded
    @POST("apiv2/channels/genre")
    Call<CategoriasResponse> allCategories(
        @Field("auth") String auth
    );

    @FormUrlEncoded
    @POST("apiv2/channels/list")
    Call<ChannelResponse> allChannels(
            @Field("auth") String auth
    );

}
