package com.gitlab.jeeto.oboco.activity;

import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.gitlab.jeeto.oboco.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseActivity extends AppCompatActivity {
    public abstract View getMessageView();

    public void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getMessageView(), message, 10000);
        snackbar.setAction(R.string.snackbar_action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        snackbar.setGestureInsetBottomIgnored(true);
        snackbar.setBehavior(new BaseTransientBottomBar.Behavior() {
            @Override
            public boolean canSwipeDismissView(View child) {
                return false;
            }
        });

        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.darker));

        TextView textView = view.findViewById(R.id.snackbar_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightest));
        textView.setMaxLines(5);

        textView = view.findViewById(R.id.snackbar_action);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightest));

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        snackbar.show();
    }
}
