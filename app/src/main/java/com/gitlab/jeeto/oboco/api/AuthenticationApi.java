package com.gitlab.jeeto.oboco.api;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthenticationApi {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/authentication")
    public Single<UserIdDto> createUserIdByUserNamePassword(@Body UserNamePasswordDto userNamePassword);

    @Headers("Content-Type: application/json")
    @POST("/api/v1/authentication/refresh")
    public Single<UserIdDto> createUserIdByUserToken(@Body UserTokenDto userToken);
}
