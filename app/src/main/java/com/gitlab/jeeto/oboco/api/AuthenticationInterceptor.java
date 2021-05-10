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
        String accessToken = authenticationManager.getAccessToken();

        Request request = newRequestWithAccessToken(chain.request(), accessToken);

        Response response = chain.proceed(request);

        if(response.code() == 401) {
            synchronized(this) {
                String accessToken2 = authenticationManager.getAccessToken();

                // refresh?
                if (accessToken.equals("") == false && accessToken.equals(accessToken2) == false) {
                    response.close();

                    return chain.proceed(newRequestWithAccessToken(request, accessToken2));
                } else {
                    // refresh
                    try {
                        authenticationManager.refresh().blockingAwait();
                    } catch (Exception e) {
                        //e.printStackTrace();

                        return response;
                    }

                    String accessToken3 = authenticationManager.getAccessToken();

                    response.close();

                    return chain.proceed(newRequestWithAccessToken(request, accessToken3));
                }
            }
        }

        return response;
    }

    private Request newRequestWithAccessToken(@NonNull Request request, String accessToken) {
        if(accessToken.equals("") == false) {
            return request.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        } else {
            return request;
        }
    }
}
