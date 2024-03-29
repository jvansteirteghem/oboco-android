package com.gitlab.jeeto.oboco.client;

import android.content.Context;

import com.gitlab.jeeto.oboco.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApplicationService extends Service {
    private ApplicationApi retrofitApplicationApi;

    public ApplicationService(Context context, String baseUrl, AuthenticationManager authenticationManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(authenticationManager));

        if(BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
        }

        OkHttpClient client = builder.build();

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateJsonSerializer())
                .registerTypeAdapter(Date.class, new DateJsonDeserializer())
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        retrofitApplicationApi = retrofit.create(ApplicationApi.class);
    }

    public Single<UserDto> getAuthenticatedUser() {
        Single<UserDto> single = new Single<UserDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super UserDto> observer) {
                Single<UserDto> retrofitSingle = retrofitApplicationApi.getAuthenticatedUser();
                retrofitSingle.subscribe(new SingleObserver<UserDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserDto user) {
                        observer.onSuccess(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<UserDto> updateAuthenticatedUserPassword(UserPasswordDto userPassword) {
        Single<UserDto> single = new Single<UserDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super UserDto> observer) {
                Single<UserDto> retrofitSingle = retrofitApplicationApi.updateAuthenticatedUserPassword(userPassword);
                retrofitSingle.subscribe(new SingleObserver<UserDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserDto user) {
                        observer.onSuccess(user);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookCollectionDto>> getBookCollections(String filterType, Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                Single<PageableListDto<BookCollectionDto>> retrofitSingle = retrofitApplicationApi.getBookCollections(filterType, page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                        observer.onSuccess(bookCollectionPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookCollectionDto>> getBookCollections(String searchType, String search, String filterType, Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                Single<PageableListDto<BookCollectionDto>> retrofitSingle = retrofitApplicationApi.getBookCollections(searchType, search, filterType, page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                        observer.onSuccess(bookCollectionPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookCollectionDto>> getBookCollectionsByBookCollection(Long bookCollectionId, Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                Single<PageableListDto<BookCollectionDto>> retrofitSingle = retrofitApplicationApi.getBookCollectionsByBookCollection(bookCollectionId, page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                        observer.onSuccess(bookCollectionPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookCollectionDto>> getBookCollectionsByBookCollection(Long bookCollectionId, String searchType, String search, Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                Single<PageableListDto<BookCollectionDto>> retrofitSingle = retrofitApplicationApi.getBookCollectionsByBookCollection(bookCollectionId, searchType, search, page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                        observer.onSuccess(bookCollectionPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<BookCollectionDto> getRootBookCollection(String graph) {
        Single<BookCollectionDto> single = new Single<BookCollectionDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionDto> observer) {
                Single<BookCollectionDto> retrofitSingle = retrofitApplicationApi.getRootBookCollection(graph);
                retrofitSingle.subscribe(new SingleObserver<BookCollectionDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookCollectionDto bookCollection) {
                        observer.onSuccess(bookCollection);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<BookCollectionDto> getBookCollection(Long id, String graph) {
        Single<BookCollectionDto> single = new Single<BookCollectionDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionDto> observer) {
                Single<BookCollectionDto> retrofitSingle = retrofitApplicationApi.getBookCollection(id, graph);
                retrofitSingle.subscribe(new SingleObserver<BookCollectionDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookCollectionDto bookCollection) {
                        observer.onSuccess(bookCollection);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<LinkableDto<BookDto>> getLinkableBookByBookCollection(Long bookCollectionId, Long id, String graph) {
        Single<LinkableDto<BookDto>> single = new Single<LinkableDto<BookDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super LinkableDto<BookDto>> observer) {
                Single<LinkableDto<BookDto>> retrofitSingle = retrofitApplicationApi.getLinkableBookByBookCollection(bookCollectionId, id, graph);
                retrofitSingle.subscribe(new SingleObserver<LinkableDto<BookDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(LinkableDto<BookDto> bookLinkable) {
                        observer.onSuccess(bookLinkable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookDto>> getBooksByBookCollection(Long bookCollectionId, Integer page, Integer pageSize, String graph) {
        return getBooksByBookCollection(bookCollectionId, null, page, pageSize, graph);
    }

    public Single<PageableListDto<BookDto>> getBooksByBookCollection(Long bookCollectionId, String filterType, Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookDto>> single = new Single<PageableListDto<BookDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookDto>> observer) {
                Single<PageableListDto<BookDto>> retrofitSingle = retrofitApplicationApi.getBooksByBookCollection(bookCollectionId, filterType, page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookDto> bookPageableList) {
                        observer.onSuccess(bookPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<BookDto> getBook(Long id, String graph) {
        Single<BookDto> single = new Single<BookDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookDto> observer) {
                Single<BookDto> retrofitSingle = retrofitApplicationApi.getBook(id, graph);
                retrofitSingle.subscribe(new SingleObserver<BookDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookDto book) {
                        observer.onSuccess(book);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<PageableListDto<BookMarkDto>> getBookMarks(Integer page, Integer pageSize, String graph) {
        Single<PageableListDto<BookMarkDto>> single = new Single<PageableListDto<BookMarkDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookMarkDto>> observer) {
                Single<PageableListDto<BookMarkDto>> retrofitSingle = retrofitApplicationApi.getBookMarks(page, pageSize, graph);
                retrofitSingle.subscribe(new SingleObserver<PageableListDto<BookMarkDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(PageableListDto<BookMarkDto> bookMarkPageableList) {
                        observer.onSuccess(bookMarkPageableList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<BookMarkDto> getBookMarkByBook(Long bookId, String graph) {
        Single<BookMarkDto> single = new Single<BookMarkDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookMarkDto> observer) {
                Single<BookMarkDto> retrofitSingle = retrofitApplicationApi.getBookMarkByBook(bookId, graph);
                retrofitSingle.subscribe(new SingleObserver<BookMarkDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookMarkDto bookMark) {
                        observer.onSuccess(bookMark);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<BookMarkDto> createOrUpdateBookMarkByBook(Long bookId, BookMarkDto bookMark) {
        Single<BookMarkDto> single = new Single<BookMarkDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookMarkDto> observer) {
                Single<BookMarkDto> retrofitSingle = retrofitApplicationApi.createOrUpdateBookMarkByBook(bookId, bookMark);
                retrofitSingle.subscribe(new SingleObserver<BookMarkDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookMarkDto bookMark) {
                        observer.onSuccess(bookMark);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Completable deleteBookMarkByBook(Long bookId) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                Completable retrofitCompletable = retrofitApplicationApi.deleteBookMarkByBook(bookId);
                retrofitCompletable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return completable;
    }

    public Single<BookCollectionMarkDto> createOrUpdateBookMarksByBookCollection(Long bookCollectionId, BookCollectionMarkDto bookCollectionMark) {
        Single<BookCollectionMarkDto> single = new Single<BookCollectionMarkDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionMarkDto> observer) {
                Single<BookCollectionMarkDto> retrofitSingle = retrofitApplicationApi.createOrUpdateBookMarksByBookCollection(bookCollectionId, bookCollectionMark);
                retrofitSingle.subscribe(new SingleObserver<BookCollectionMarkDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(BookCollectionMarkDto bookCollectionMark) {
                        observer.onSuccess(bookCollectionMark);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Completable deleteBookMarksByBookCollection(Long bookCollectionId) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                Completable retrofitCompletable = retrofitApplicationApi.deleteBookMarksByBookCollection(bookCollectionId);
                retrofitCompletable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return completable;
    }

    public Single<ResponseBody> downloadBookCollectionPage(Long bookCollectionId, String scaleType, Integer scaleWidth, Integer scaleHeight) {
        Single<ResponseBody> single = new Single<ResponseBody>() {
            @Override
            protected void subscribeActual(SingleObserver<? super ResponseBody> observer) {
                Single<ResponseBody> retrofitSingle = retrofitApplicationApi.downloadBookCollectionPage(bookCollectionId, scaleType, scaleWidth, scaleHeight);
                retrofitSingle.subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        observer.onSuccess(responseBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<ResponseBody> downloadBookPage(Long bookId, Integer page, String scaleType, Integer scaleWidth, Integer scaleHeight) {
        Single<ResponseBody> single = new Single<ResponseBody>() {
            @Override
            protected void subscribeActual(SingleObserver<? super ResponseBody> observer) {
                Single<ResponseBody> retrofitSingle = retrofitApplicationApi.downloadBookPage(bookId, page, scaleType, scaleWidth, scaleHeight);
                retrofitSingle.subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        observer.onSuccess(responseBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }

    public Single<ResponseBody> downloadBook(Long bookId) {
        Single<ResponseBody> single = new Single<ResponseBody>() {
            @Override
            protected void subscribeActual(SingleObserver<? super ResponseBody> observer) {
                Single<ResponseBody> retrofitSingle = retrofitApplicationApi.downloadBook(bookId);
                retrofitSingle.subscribe(new SingleObserver<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {
                        observer.onSuccess(responseBody);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(getThrowable(e));
                    }
                });
            }
        };

        return single;
    }
}
