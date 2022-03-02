package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.LinkableDto;
import com.gitlab.jeeto.oboco.client.ProblemDto;
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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_USER_TOKEN_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_user_log_in_token_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });

        mApplicationService = new ApplicationService(getApplication().getApplicationContext(), mBaseUrl, mAuthenticationManager);

        mRequestHandler = new RemoteBookReaderRequestHandler(mApplicationService, mBookId);

        mPicasso = new Picasso.Builder(getApplication())
                .addRequestHandler(mRequestHandler)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e) {
                        String message = null;

                        ProblemDto p = getProblem(e);
                        if(p != null) {
                            if(404 == p.getStatusCode()) {
                                if("PROBLEM_BOOK_NOT_FOUND".equals(p.getCode()) || "PROBLEM_BOOK_PAGE_NOT_FOUND".equals(p.getCode())) {
                                    message = getMessage(R.string.action_book_page_get_error);
                                }
                            }
                        }

                        if(message == null) {
                            message = getMessage(e);
                        }

                        mMessageObservable.setValue(message);
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
        mPicasso.shutdown();

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

                if(bookMarkDto != null && bookMarkDto.getPage() != 0) {
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
                        String message = null;

                        ProblemDto p = getProblem(e);
                        if(p != null) {
                            if(400 == p.getStatusCode()) {
                                if("PROBLEM_GRAPH_INVALID".equals(p.getCode())) {
                                    message = getMessage(R.string.action_book_get_error);
                                }
                            } else if(400 == p.getStatusCode()) {
                                if("PROBLEM_BOOK_NOT_FOUND".equals(p.getCode())) {
                                    message = getMessage(R.string.action_book_get_error);
                                }
                            }
                        }

                        if(message == null) {
                            message = getMessage(e);
                        }

                        mMessageObservable.setValue(message);
                        mShowMessageObservable.setValue(true);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_GRAPH_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_get_error);
                        }
                    } else if(400 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_NOT_FOUND".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_get_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });
    }

    @Override
    public void addBookMark() {
        BookDto bookDto = mBookObservable.getValue();

        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setNumberOfPages(bookDto.getNumberOfPages());
        bookMarkDto.setPage(mSelectedBookPageObservable.getValue());

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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_MARK_PAGE_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_mark_create_update_error);
                        }
                    } else if(400 == p.getStatusCode()) {
                        if("PROBLEM_BOOK_NOT_FOUND".equals(p.getCode())) {
                            message = getMessage(R.string.action_book_mark_create_update_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
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
