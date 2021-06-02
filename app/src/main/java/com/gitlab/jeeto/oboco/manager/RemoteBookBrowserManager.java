package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.BookBrowserFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookBrowserManager extends BookBrowserManager {
    public static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";
    private BookBrowserFragment mBookBrowserFragment;
    private Long mBookCollectionId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteBookBrowserManager(BookBrowserFragment bookBrowserFragment) {
        super();
        mBookBrowserFragment = bookBrowserFragment;
    }

    public void create(Bundle savedInstanceState) {
        mBookCollectionId = mBookBrowserFragment.getArguments().getLong(PARAM_BOOK_COLLECTION_ID);

        SharedPreferences sp = mBookBrowserFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mBookBrowserFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mBookBrowserFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mBookBrowserFragment.getContext(), mBaseUrl, mAuthenticationManager);
    }

    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void load(String bookMarkStatus, int page, int pageSize) {
        Single<BookCollectionDto> single;
        if(mBookCollectionId == null) {
            single = mApplicationService.getRootBookCollection("(parentBookCollection)");
        } else {
            single = mApplicationService.getBookCollection(mBookCollectionId, "(parentBookCollection)");
        }
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookCollectionDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookCollectionDto bookCollection) {
                if(bookCollection != null) {
                    Single<PageableListDto<BookDto>> single = mApplicationService.getBooks(bookCollection.getId(), bookMarkStatus, page, pageSize, "(bookMark)");
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PageableListDto<BookDto> bookPageableList) {
                            mBookBrowserFragment.onLoad(bookCollection, bookPageableList);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mBookBrowserFragment.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mBookBrowserFragment.onError(e);
            }
        });
    }

    @Override
    public void loadBookPageableList(String bookMarkStatus, int page, int pageSize) {
        Single<PageableListDto<BookDto>> single = mApplicationService.getBooks(mBookCollectionId, bookMarkStatus, page, pageSize, "(bookMark)");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookDto> bookPageableList) {
                mBookBrowserFragment.onLoadBookPageableList(bookPageableList);
            }

            @Override
            public void onError(Throwable e) {
                mBookBrowserFragment.onError(e);
            }
        });
    }

    @Override
    public void addBookMark(BookDto book) {
        BookMarkDto bookMark = new BookMarkDto();
        bookMark.setPage(book.getNumberOfPages());

        Single<BookMarkDto> single = mApplicationService.createOrUpdateBookMark(book.getId(), bookMark);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookMarkDto bookMark) {
                mBookBrowserFragment.onAddBookMark(book, bookMark);
            }

            @Override
            public void onError(Throwable e) {
                mBookBrowserFragment.onError(e);
            }
        });
    }

    @Override
    public void removeBookMark(BookDto book) {
        Completable completable = mApplicationService.deleteBookMark(book.getId());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mBookBrowserFragment.onRemoveBookMark(book);
            }

            @Override
            public void onError(Throwable e) {
                mBookBrowserFragment.onError(e);
            }
        });
    }

    public Uri getBookPageUri(BookDto book, String scaleType, int scaleWidth, int scaleHeight) {
        return new Uri.Builder()
                .scheme("bookBrowserManager")
                .authority("")
                .path("/bookPage")
                .appendQueryParameter("bookId", Long.toString(book.getId()))
                .appendQueryParameter("scaleType", scaleType)
                .appendQueryParameter("scaleWidth", Integer.toString(scaleWidth))
                .appendQueryParameter("scaleHeight", Integer.toString(scaleHeight))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookBrowserManager");
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookPage")) {
            Long bookId = Long.parseLong(request.uri.getQueryParameter("bookId"));
            String scaleType = request.uri.getQueryParameter("scaleType");
            Integer scaleWidth = Integer.parseInt(request.uri.getQueryParameter("scaleWidth"));
            Integer scaleHeight = Integer.parseInt(request.uri.getQueryParameter("scaleHeight"));

            ResponseBody responseBody = mApplicationService.downloadBookPage(bookId, 1, scaleType, scaleWidth, scaleHeight).blockingGet();

            InputStream inputStream = responseBody.byteStream();

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
