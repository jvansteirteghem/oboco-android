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

public class AccountLogoutFragment extends Fragment {
    private Button mLogoutButton;
    private EditText mPasswordEditText;
    private String mPassword;
    private EditText mUpdatePasswordEditText;
    private String mUpdatePassword;
    private CheckBox mShowPasswordCheckBox;
    private Boolean mShowPassword;
    private Button mUpdatePasswordButton;

    private AccountLogoutViewModel mViewModel;

    public AccountLogoutFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteAccountLogoutViewModel.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_account_logout, container, false);

        mLogoutButton = (Button) view.findViewById(R.id.account_logout_btn_logout);
        mPasswordEditText = (EditText) view.findViewById(R.id.account_logout_et_password);
        mUpdatePasswordEditText = (EditText) view.findViewById(R.id.account_logout_et_update_password);
        mShowPasswordCheckBox = (CheckBox) view.findViewById(R.id.account_logout_cb_show_password);
        mUpdatePasswordButton = (Button) view.findViewById(R.id.account_logout_btn_update_password);

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.logout();
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

        mUpdatePasswordEditText.addTextChangedListener(new TextWatcher() {
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
                mUpdatePassword = editable.toString();

                mViewModel.setUpdatePassword(mUpdatePassword);
            }
        });

        mShowPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mShowPassword = isChecked;

                mViewModel.setShowPassword(mShowPassword);
            }
        });

        mUpdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.updatePassword();
            }
        });

        mViewModel.getPasswordObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String password) {
                if(!(mPassword != null && mPassword.equals(password))) {
                    mPassword = password;
                    mPasswordEditText.setText(mPassword);
                }
            }
        });
        mViewModel.getUpdatePasswordObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String updatePassword) {
                if(!(mUpdatePassword != null && mUpdatePassword.equals(updatePassword))) {
                    mUpdatePassword = updatePassword;
                    mUpdatePasswordEditText.setText(mUpdatePassword);
                }
            }
        });
        mViewModel.getShowPasswordObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showPassword) {
                if(showPassword) {
                    mPasswordEditText.setTransformationMethod(null);
                    mUpdatePasswordEditText.setTransformationMethod(null);
                } else {
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mUpdatePasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

                if(!(mShowPassword != null && mShowPassword.equals(showPassword))) {
                    mShowPassword = showPassword;
                    mShowPasswordCheckBox.setChecked(mShowPassword);
                }
            }
        });
        mViewModel.getIsEnabledObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEnabled) {
                mLogoutButton.setEnabled(isEnabled);
                mUpdatePasswordButton.setEnabled(isEnabled);
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
        mViewModel.getNavigateToAccountLoginViewObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean navigateToAccountLoginView) {
                if(navigateToAccountLoginView) {
                    mViewModel.setNavigateToAccountLoginView(false);

                    ((MainActivity) getActivity()).navigateToAccountLoginView();
                }
            }
        });

        return view;
    }
}
