package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.LinkableDto;
import com.squareup.picasso.Picasso;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteBookReaderViewModel extends BookReaderViewModel {
    private Long mBookId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private BookReaderRequestHandler mRequestHandler;
    private Picasso mPicasso;

    public RemoteBookReaderViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBookId = getArguments().getLong(BookReaderViewModel.PARAM_BOOK_ID);

        SharedPreferences sp = getApplication().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(getApplication().getApplicationContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });

        mApplicationService = new ApplicationService(getApplication().getApplicationContext(), mBaseUrl, mAuthenticationManager);

        mRequestHandler = new RemoteBookReaderRequestHandler(mApplicationService, mBookId);

        mPicasso = new Picasso.Builder(getApplication())
                .addRequestHandler(mRequestHandler)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        mMessageObservable.setValue(toMessage(exception));
                        mShowMessageObservable.setValue(true);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();

        load();
    }

    @Override
    protected void onCleared() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void load() {
        mIsFullscreenObservable.setValue(true);

        Single<BookDto> single = mApplicationService.getBook(mBookId, "(bookCollection,bookMark)");
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookDto bookDto) {
                BookMarkDto bookMarkDto = bookDto.getBookMark();

                if(bookMarkDto != null) {
                    mSelectedBookPageObservable.setValue(bookMarkDto.getPage());
                } else {
                    mSelectedBookPageObservable.setValue(1);
                }

                mBookObservable.setValue(bookDto);

                Single<LinkableDto<BookDto>> single = mApplicationService.getLinkableBookByBookCollection(bookDto.getBookCollection().getId(), bookDto.getId(), "()");
                single = single.observeOn(AndroidSchedulers.mainThread());
                single = single.subscribeOn(Schedulers.io());
                single.subscribe(new SingleObserver<LinkableDto<BookDto>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(LinkableDto<BookDto> bookLinkableDto) {
                        bookLinkableDto.setElement(bookDto);

                        mBookLinkableObservable.setValue(bookLinkableDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mMessageObservable.setValue(toMessage(e));
                        mShowMessageObservable.setValue(true);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public void addBookMark() {
        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setPage(mSelectedBookPageObservable.getValue());

        BookDto bookDto = mBookObservable.getValue();
        bookDto.setBookMark(bookMarkDto);

        Single<BookMarkDto> single = mApplicationService.createOrUpdateBookMarkByBook(mBookId, bookMarkDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookMarkDto bookMarkDto) {
                mBookMarkObservable.setValue(bookMarkDto);
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public Uri getBookPageUri(int bookPage) {
        return mRequestHandler.getBookPageUri(bookPage);
    }

    @Override
    public Picasso getPicasso() {
        return mPicasso;
    }
}
