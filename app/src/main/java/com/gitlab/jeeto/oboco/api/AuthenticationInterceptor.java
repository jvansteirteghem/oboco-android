package com.gitlab.jeeto.oboco.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {
    private AuthenticationManager authenticationManager;

    public AuthenticationInterceptor(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String idToken = authenticationManager.getIdToken();

        Request request = newRequestWithIdToken(chain.request(), idToken);

        Response response = chain.proceed(request);

        if(response.code() == 401) {
            synchronized(this) {
                String idToken2 = authenticationManager.getIdToken();

                // refresh?
                if (idToken.equals("") == false && idToken.equals(idToken2) == false) {
                    response.close();

                    return chain.proceed(newRequestWithIdToken(request, idToken2));
                } else {
                    // refresh
                    try {
                        authenticationManager.refresh().blockingAwait();
                    } catch (Exception e) {
                        //e.printStackTrace();

                        return response;
                    }

                    String idToken3 = authenticationManager.getIdToken();

                    response.close();

                    return chain.proceed(newRequestWithIdToken(request, idToken3));
                }
            }
        }

        return response;
    }

    private Request newRequestWithIdToken(@NonNull Request request, String idToken) {
        if(idToken.equals("") == false) {
            return request.newBuilder()
                    .header("Authorization", "Bearer " + idToken)
                    .build();
        } else {
            return request;
        }
    }
}
