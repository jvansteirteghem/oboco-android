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
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.OnErrorListener;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AccountLoginFragment extends Fragment {
    private OnLoginListener mOnLoginListener;
    private OnErrorListener mOnErrorListener;

    public AccountLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = getContext().getSharedPreferences("application", Context.MODE_PRIVATE);

        String baseUrl = preferences.getString("baseUrl", "");
        String name = preferences.getString("name", "");
        String password = preferences.getString("password", "");

        EditText etBaseUrl = (EditText) getActivity().findViewById(R.id.et_baseUrl);
        etBaseUrl.setText(baseUrl);

        EditText etName = (EditText) getActivity().findViewById(R.id.et_name);
        etName.setText(name);

        EditText etPassword = (EditText) getActivity().findViewById(R.id.et_password);
        etPassword.setText(password);

        CheckBox cbShowPassword = (CheckBox) getActivity().findViewById(R.id.cb_show_password);
        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // show password
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // hide password
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        Button btnLogin = (Button) getActivity().findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String baseUrl = etBaseUrl.getText().toString();
                baseUrl = baseUrl.replaceAll("\\/+$", "");

                String name = etName.getText().toString();
                String password = etPassword.getText().toString();

                SharedPreferences preferences = getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("baseUrl", baseUrl);
                editor.putString("name", name);
                editor.putString("password", "");
                editor.putString("idToken", "");
                editor.putString("refreshToken", "");
                editor.commit();

                Completable completable = new Completable() {
                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        try {
                            AuthenticationManager authenticationManager = new AuthenticationManager(getContext());
                            authenticationManager.login(name, password).blockingAwait();

                            observer.onComplete();
                        } catch(Exception e) {
                            observer.onError(e);
                        }
                    }
                };
                completable = completable.observeOn(AndroidSchedulers.mainThread());
                completable = completable.subscribeOn(Schedulers.io());
                completable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        btnLogin.setEnabled(false);
                    }

                    @Override
                    public void onComplete() {
                        btnLogin.setEnabled(true);

                        mOnLoginListener.onLogin();
                    }

                    @Override
                    public void onError(Throwable e) {
                        btnLogin.setEnabled(true);

                        mOnErrorListener.onError(e);
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginListener) {
            mOnLoginListener = (OnLoginListener) context;
        }
        if(context instanceof OnErrorListener) {
            mOnErrorListener = (OnErrorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnLoginListener = null;
        mOnErrorListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface OnLoginListener {
        void onLogin();
    }
}
