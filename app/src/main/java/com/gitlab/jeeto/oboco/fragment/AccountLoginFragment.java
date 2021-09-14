package com.gitlab.jeeto.oboco.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.MainActivity;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;

import java.util.Objects;

public class AccountLoginFragment extends Fragment {
    private EditText mBaseUrlEditText;
    private String mBaseUrl;
    private EditText mNameEditText;
    private String mName;
    private EditText mPasswordEditText;
    private String mPassword;
    private CheckBox mShowPasswordCheckBox;
    private Boolean mShowPassword;
    private Button mLoginButton;

    private AccountLoginViewModel mViewModel;

    public AccountLoginFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteAccountLoginViewModel.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_account_login, container, false);

        mBaseUrlEditText = (EditText) view.findViewById(R.id.account_login_et_baseUrl);
        mNameEditText = (EditText) view.findViewById(R.id.account_login_et_name);
        mPasswordEditText = (EditText) view.findViewById(R.id.account_login_et_password);
        mShowPasswordCheckBox = (CheckBox) view.findViewById(R.id.account_login_cb_show_password);
        mLoginButton = (Button) view.findViewById(R.id.account_login_btn_login);

        mBaseUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBaseUrl = editable.toString();

                mViewModel.setBaseUrl(mBaseUrl);
            }
        });

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mName = editable.toString();

                mViewModel.setName(mName);
            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPassword = editable.toString();

                mViewModel.setPassword(mPassword);
            }
        });

        mShowPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mShowPassword = isChecked;

                mViewModel.setShowPassword(mShowPassword);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.login();
            }
        });

        mViewModel.getBaseUrlObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String baseUrl) {
                if(!Objects.equals(mBaseUrl, baseUrl)) {
                    mBaseUrl = baseUrl;
                    mBaseUrlEditText.setText(mBaseUrl);
                }
            }
        });
        mViewModel.getNameObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String name) {
                if(!Objects.equals(mName, name)) {
                    mName = name;
                    mNameEditText.setText(mName);
                }
            }
        });
        mViewModel.getPasswordObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String password) {
                if(!Objects.equals(mPassword, password)) {
                    mPassword = password;
                    mPasswordEditText.setText(mPassword);
                }
            }
        });
        mViewModel.getShowPasswordObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showPassword) {
                if(showPassword) {
                    mPasswordEditText.setTransformationMethod(null);
                } else {
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

                if(!Objects.equals(mShowPassword, showPassword)) {
                    mShowPassword = showPassword;
                    mShowPasswordCheckBox.setChecked(mShowPassword);
                }
            }
        });
        mViewModel.getIsEnabledObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEnabled) {
                mLoginButton.setEnabled(isEnabled);
            }
        });
        mViewModel.getShowMessageObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showMessage) {
                if(showMessage) {
                    mViewModel.setShowMessage(false);

                    Toast toast = Toast.makeText(getContext(), mViewModel.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        mViewModel.getNavigateToAccountLogoutViewObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean navigateToAccountLogoutView) {
                if(navigateToAccountLogoutView) {
                    mViewModel.setNavigateToAccountLogoutView(false);

                    ((MainActivity) getActivity()).navigateToAccountLogoutView();
                }
            }
        });

        return view;
    }
}
