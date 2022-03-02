package com.gitlab.jeeto.oboco.common;

import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.AndroidViewModel;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ProblemDto;
import com.gitlab.jeeto.oboco.client.ProblemException;

public class BaseViewModel extends AndroidViewModel {
    private Bundle mArguments;

    public BaseViewModel(Application application, Bundle arguments) {
        super(application);

        mArguments = arguments;
    }

    public Bundle getArguments() {
        return mArguments;
    }

    public ProblemDto getProblem(Throwable e) {
        ProblemDto p = null;

        if(e instanceof RuntimeException) {
            if(e.getCause() instanceof ProblemException) {
                e = e.getCause();
            }
        }

        if(e instanceof ProblemException) {
            ProblemException pe = (ProblemException) e;

            p = pe.getProblem();
        }

        return p;
    }

    public String getMessage(Throwable e) {
        String message = null;

        ProblemDto p = getProblem(e);
        if(p != null) {
            if(401 == p.getStatusCode()) {
                if("PROBLEM_USER_NOT_AUTHENTICATED".equals(p.getCode())) {
                    message = getMessage(R.string.action_error_user_not_authenticated);
                }
            } else if(403 == p.getStatusCode()) {
                if("PROBLEM_USER_NOT_AUTHORIZED".equals(p.getCode())) {
                    message = getMessage(R.string.action_error_user_not_authorized);
                }
            } else if(404 == p.getStatusCode()) {
                if("PROBLEM_USER_ROOT_BOOK_COLLECTION_NOT_FOUND".equals(p.getCode())) {
                    message = getMessage(R.string.action_error_user_root_book_collection);
                }
            } else if(500 == p.getStatusCode()) {
                if("PROBLEM".equals(p.getCode())) {
                    message = getMessage(R.string.action_error);
                }
            } else if(503 == p.getStatusCode()) {
                if("PROBLEM_BOOK_SCANNER_STATUS_INVALID".equals(p.getCode())) {
                    message = getMessage(R.string.action_error_book_scanner);
                }
            }
        }

        if(message == null) {
            message = getMessage(R.string.action_error);
        }

        return message;
    }

    public String getMessage(int id) {
        String message = getApplication().getResources().getString(id);

        return message;
    }
}
