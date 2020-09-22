package com.gitlab.jeeto.oboco.api;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class AuthenticationManager {
    private Context context;
    private SharedPreferences sp;
    private AuthenticationService authenticationService;
    private PublishSubject<Throwable> subject = PublishSubject.create();

    public AuthenticationManager(Context context) {
        super();

        sp = context.getSharedPreferences("application", Context.MODE_PRIVATE);
        String baseUrl = sp.getString("baseUrl", "");

        authenticationService = new AuthenticationService(baseUrl);
    }

    public PublishSubject<Throwable> getErrors() {
        return subject;
    }

    public String getIdToken() {
        return sp.getString("idToken", "");
    }

    public String getRefreshToken() {
        return sp.getString("refreshToken", "");
    }

    public Completable login(String name, String password) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                UserNamePasswordDto userNamePassword = new UserNamePasswordDto();
                userNamePassword.setName(name);
                userNamePassword.setPassword(password);

                Single<UserIdDto> single = authenticationService.createUserIdByUserNamePassword(userNamePassword);
                single.subscribe(new SingleObserver<UserIdDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserIdDto userId) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("idToken", userId.getIdToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onNext(e);

                        observer.onError(e);
                    }
                });
            }
        };
        return completable;
    }

    public Completable refresh() {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                UserTokenDto userToken = new UserTokenDto();
                userToken.setToken(getRefreshToken());

                Single<UserIdDto> single = authenticationService.createUserIdByUserToken(userToken);
                single.subscribe(new SingleObserver<UserIdDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserIdDto userId) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("idToken", userId.getIdToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onNext(e);

                        observer.onError(e);
                    }
                });
            }
        };
        return completable;
    }

    public Completable logout() {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("idToken", "");
                editor.putString("refreshToken", "");
                editor.commit();

                observer.onComplete();
            }
        };
        return completable;
    }
}
