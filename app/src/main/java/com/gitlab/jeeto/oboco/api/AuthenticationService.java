package com.gitlab.jeeto.oboco.api;

import com.gitlab.jeeto.oboco.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthenticationService extends Service {
    private AuthenticationApi retrofitAuthenticationApi;

    public AuthenticationService(String baseUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if(BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
        }

        OkHttpClient client = builder.build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateJsonSerializer())
                .registerTypeAdapter(Date.class, new DateJsonDeserializer())
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        retrofitAuthenticationApi = retrofit.create(AuthenticationApi.class);
    }

    public Single<UserIdDto> createUserIdByUserNamePassword(UserNamePasswordDto userNamePassword) {
        Single<UserIdDto> single = new Single<UserIdDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super UserIdDto> observer) {
            Single<UserIdDto> retrofitSingle = retrofitAuthenticationApi.createUserIdByUserNamePassword(userNamePassword);
            retrofitSingle.subscribe(new SingleObserver<UserIdDto>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(UserIdDto userId) {
                    observer.onSuccess(userId);
                }

                @Override
                public void onError(Throwable e) {
                    observer.onError(getThrowable(e));
                }
            });
            }
        };

        return single;
    }

    public Single<UserIdDto> createUserIdByUserToken(UserTokenDto userToken) {
        Single<UserIdDto> single = new Single<UserIdDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super UserIdDto> observer) {
            Single<UserIdDto> retrofitSingle = retrofitAuthenticationApi.createUserIdByUserToken(userToken);
            retrofitSingle.subscribe(new SingleObserver<UserIdDto>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onSuccess(UserIdDto userId) {
                    observer.onSuccess(userId);
                }

                @Override
                public void onError(Throwable e) {
                    observer.onError(getThrowable(e));
                }
            });
            }
        };

        return single;
    }
}
