package com.gitlab.jeeto.oboco.api;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.HttpException;
import retrofit2.Retrofit;

public abstract class Service {
    protected Retrofit retrofit;

    protected Throwable getThrowable(Throwable e) {
        try {
            if (e instanceof HttpException) {
                HttpException httpException = (HttpException) e;

                ResponseBody errorBody = httpException.response().errorBody();

                Converter<ResponseBody, ProblemDto> converter = retrofit.responseBodyConverter(ProblemDto.class, new Annotation[0]);

                ProblemDto problem = converter.convert(errorBody);
                ProblemException problemException = new ProblemException(problem);

                return problemException;
            } else {
                return e;
            }
        } catch(IOException ioException) {
            return ioException;
        }
    }
}
