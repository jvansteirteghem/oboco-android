package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.manager.AccountLoginManager;
import com.gitlab.jeeto.oboco.manager.RemoteAccountLoginManager;

public class AccountLoginFragment extends Fragment {
    private EditText mBaseUrlEditText;
    private EditText mNameEditText;
    private EditText mPasswordEditText;
    private CheckBox mShowPasswordCheckBox;
    private Button mLoginButton;

    private OnAccountLoginListener mOnAccountLoginListener;
    private OnErrorListener mOnErrorListener;

    private AccountLoginManager mAccountLoginManager;

    public interface OnAccountLoginListener {
        void onLogin();
    }

    public AccountLoginFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountLoginManager = new RemoteAccountLoginManager(this);
        mAccountLoginManager.create(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mAccountLoginManager.destroy();

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

        mShowPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // show password
                    mPasswordEditText.setTransformationMethod(null);
                } else {
                    // hide password
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String baseUrl = mBaseUrlEditText.getText().toString();
                baseUrl = baseUrl.replaceAll("\\/+$", "");

                String name = mNameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                mLoginButton.setEnabled(false);

                mAccountLoginManager.login(baseUrl, name, password);
            }
        });

        mAccountLoginManager.load();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountLoginListener) {
            mOnAccountLoginListener = (OnAccountLoginListener) context;
        }
        if(context instanceof OnErrorListener) {
            mOnErrorListener = (OnErrorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnAccountLoginListener = null;
        mOnErrorListener = null;
    }

    public void onError(Throwable e) {
        mLoginButton.setEnabled(true);

        if(mOnErrorListener != null) {
            mOnErrorListener.onError(e);
        }
    }

    public void onLoad(String baseUrl, String name) {
        mBaseUrlEditText.setText(baseUrl);
        mNameEditText.setText(name);
        mPasswordEditText.setText("");
    }

    public void onLogin() {
        mLoginButton.setEnabled(true);

        if(mOnAccountLoginListener != null) {
            mOnAccountLoginListener.onLogin();
        }
    }
}
