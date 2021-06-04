package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.BookReaderFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.Okio;

public class RemoteBookReaderManager extends BookReaderManager {
    public static final String PARAM_BOOK_ID = "PARAM_BOOK_ID";
    private BookReaderFragment mBookReaderFragment;
    private Long mBookId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteBookReaderManager(BookReaderFragment bookReaderFragment) {
        super();
        mBookReaderFragment = bookReaderFragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        Bundle bundle = mBookReaderFragment.getArguments();

        mBookId = bundle.getLong(RemoteBookReaderManager.PARAM_BOOK_ID);

        SharedPreferences sp = mBookReaderFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mBookReaderFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mBookReaderFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mBookReaderFragment.getContext(), mBaseUrl, mAuthenticationManager);
    }

    @Override
    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void load() {
        Single<BookDto> single = mApplicationService.getBook(mBookId, "(bookCollection,bookMark)");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookDto bookDto) {
                if(bookDto != null) {
                    Single<PageableListDto<BookDto>> single = mApplicationService.getBooks(bookDto.getBookCollection().getId(), bookDto.getId(), "()");
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PageableListDto<BookDto> bookPageableListDto) {
                            List<BookDto> bookList = bookPageableListDto.getElements();

                            mBookReaderFragment.onLoad(bookDto, bookList);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mBookReaderFragment.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mBookReaderFragment.onError(e);
            }
        });
    }

    @Override
    public void addBookMark(int bookPage) {
        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setPage(bookPage);

        Single<BookMarkDto> single = mApplicationService.createOrUpdateBookMark(mBookId, bookMarkDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookMarkDto bookMarkDto) {
                mBookReaderFragment.onAddBookMark(bookMarkDto);
            }

            @Override
            public void onError(Throwable e) {
                mBookReaderFragment.onError(e);
            }
        });
    }

    public Uri getBookPageUri(int bookPage) {
        return new Uri.Builder()
                .scheme("bookReaderManager")
                .authority("")
                .path("/bookPage")
                .appendQueryParameter("bookPage", Integer.toString(bookPage))
                .build();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return request.uri.getScheme().equals("bookReaderManager");
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        if(request.uri.getPath().equals("/bookPage")) {
            Integer bookPage = Integer.parseInt(request.uri.getQueryParameter("bookPage"));

            ResponseBody responseBody = mApplicationService.downloadBookPage(mBookId, bookPage, null, null, null).blockingGet();

            InputStream inputStream = responseBody.byteStream();

            return new RequestHandler.Result(Okio.source(inputStream), Picasso.LoadedFrom.NETWORK);
        } else {
            throw new IOException("uri is invalid");
        }
    }
}
