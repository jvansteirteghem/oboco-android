package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.api.UserDto;
import com.gitlab.jeeto.oboco.api.UserPasswordDto;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AccountLogoutFragment extends Fragment {
    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private OnLogoutListener mOnLogoutListener;
    private OnErrorListener mOnErrorListener;

    public AccountLogoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mOnErrorListener.onError(e);
            }
        });

        mApplicationService = new ApplicationService(getContext(), mBaseUrl, mAuthenticationManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = (Button) getActivity().findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Completable completable = mAuthenticationManager.logout();
                completable = completable.observeOn(AndroidSchedulers.mainThread());
                completable = completable.subscribeOn(Schedulers.io());
                completable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        btnLogout.setEnabled(false);
                    }

                    @Override
                    public void onComplete() {
                        btnLogout.setEnabled(true);

                        mOnLogoutListener.onLogout();
                    }

                    @Override
                    public void onError(Throwable e) {
                        btnLogout.setEnabled(true);

                        mOnErrorListener.onError(e);
                    }
                });
            }
        });

        EditText etPassword = (EditText) getActivity().findViewById(R.id.et_password);
        EditText etUpdatePassword = (EditText) getActivity().findViewById(R.id.et_update_password);
        CheckBox cbShowPassword = (CheckBox) getActivity().findViewById(R.id.cb_show_password);
        Button btnUpdatePassword = (Button) getActivity().findViewById(R.id.btn_update_password);

        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // show password
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etUpdatePassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // hide password
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etUpdatePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserPasswordDto userPasswordDto = new UserPasswordDto();
                userPasswordDto.setPassword(etPassword.getText().toString());
                userPasswordDto.setUpdatePassword(etUpdatePassword.getText().toString());

                Single<UserDto> single = mApplicationService.updateAuthenticatedUserPassword(userPasswordDto);
                single = single.observeOn(AndroidSchedulers.mainThread());
                single = single.subscribeOn(Schedulers.io());
                single.subscribe(new SingleObserver<UserDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        btnUpdatePassword.setEnabled(false);
                    }

                    @Override
                    public void onSuccess(UserDto userDto) {
                        btnUpdatePassword.setEnabled(true);

                        Completable completable = mAuthenticationManager.logout();
                        completable = completable.observeOn(AndroidSchedulers.mainThread());
                        completable = completable.subscribeOn(Schedulers.io());
                        completable.subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                mOnLogoutListener.onLogout();
                            }

                            @Override
                            public void onError(Throwable e) {
                                mOnErrorListener.onError(e);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        btnUpdatePassword.setEnabled(true);

                        mOnErrorListener.onError(e);
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogoutListener) {
            mOnLogoutListener = (OnLogoutListener) context;
        }
        if (context instanceof OnErrorListener) {
            mOnErrorListener = (OnErrorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnLogoutListener = null;
        mOnErrorListener = null;
    }

    @Override
    public void onDestroy() {
        mAuthenticationManagerDisposable.dispose();
        super.onDestroy();
    }

    public interface OnLogoutListener {
        void onLogout();
    }
}
