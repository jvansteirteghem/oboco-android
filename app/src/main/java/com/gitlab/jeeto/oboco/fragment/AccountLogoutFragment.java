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
import com.gitlab.jeeto.oboco.client.OnErrorListener;
import com.gitlab.jeeto.oboco.manager.AccountLogoutManager;
import com.gitlab.jeeto.oboco.manager.RemoteAccountLogoutManager;

public class AccountLogoutFragment extends Fragment {
    private Button mLogoutButton;
    private EditText mPasswordEditText;
    private EditText mUpdatePasswordEditText;
    private CheckBox mShowPasswordCheckBox;
    private Button mUpdatePasswordButton;

    private OnAccountLogoutListener mOnAccountLogoutListener;
    private OnErrorListener mOnErrorListener;

    private AccountLogoutManager mAccountLogoutManager;

    public interface OnAccountLogoutListener {
        void onLogout();
    }

    public AccountLogoutFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountLogoutManager = new RemoteAccountLogoutManager(this);
        mAccountLogoutManager.create(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        mAccountLogoutManager.destroy();

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
                mLogoutButton.setEnabled(false);
                mUpdatePasswordButton.setEnabled(false);

                mAccountLogoutManager.logout();
            }
        });

        mShowPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // show password
                    mPasswordEditText.setTransformationMethod(null);
                    mUpdatePasswordEditText.setTransformationMethod(null);
                } else {
                    // hide password
                    mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mUpdatePasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        mUpdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLogoutButton.setEnabled(false);
                mUpdatePasswordButton.setEnabled(false);

                String password = mPasswordEditText.getText().toString();
                String updatePassword = mUpdatePasswordEditText.getText().toString();

                mAccountLogoutManager.updatePassword(password, updatePassword);
            }
        });

        mAccountLogoutManager.load();

        return view;
    }

    public void onLoad() {

    }

    public void onLogout() {
        mLogoutButton.setEnabled(true);
        mUpdatePasswordButton.setEnabled(true);

        mOnAccountLogoutListener.onLogout();
    }

    public void onUpdatePassword() {
        mAccountLogoutManager.logout();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAccountLogoutListener) {
            mOnAccountLogoutListener = (OnAccountLogoutListener) context;
        }
        if (context instanceof OnErrorListener) {
            mOnErrorListener = (OnErrorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnAccountLogoutListener = null;
        mOnErrorListener = null;
    }

    public void onError(Throwable e) {
        mLogoutButton.setEnabled(true);
        mUpdatePasswordButton.setEnabled(true);

        if(mOnErrorListener != null) {
            mOnErrorListener.onError(e);
        }
    }
}
