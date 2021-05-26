package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.ReaderFragment;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteBookReaderManager extends BookReaderManager {
    private ReaderFragment mReaderFragment;
    private Long mBookId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private BookHandler mBookHandler;

    public RemoteBookReaderManager(Long bookId) {
        super();
        mBookId = bookId;
    }

    @Override
    public void create(ReaderFragment readerFragment) {
        mReaderFragment = readerFragment;

        SharedPreferences sp = mReaderFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mReaderFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mReaderFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mReaderFragment.getContext(), mBaseUrl, mAuthenticationManager);

        mBookHandler = new RemoteBookHandler(mApplicationService, mBookId);
    }

    @Override
    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    public BookHandler getBookHandler() {
        return mBookHandler;
    }

    @Override
    public void loadBook() {
        Single<BookDto> single = new Single<BookDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookDto> observer) {
                try {
                    BookDto book = mApplicationService.getBook(mBookId, "(bookCollection,bookMark)").blockingGet();

                    observer.onSuccess(book);
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookDto book) {
                if(book != null) {
                    Single<List<BookDto>> single = new Single<List<BookDto>>() {
                        @Override
                        protected void subscribeActual(SingleObserver<? super List<BookDto>> observer) {
                            try {
                                PageableListDto<BookDto> bookPageableList = mApplicationService.getBooks(book.getBookCollection().getId(), book.getId(), "()").blockingGet();

                                observer.onSuccess(bookPageableList.getElements());
                            } catch(Exception e) {
                                observer.onError(e);
                            }
                        }
                    };
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<List<BookDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(List<BookDto> bookList) {
                            if(bookList != null) {
                                mReaderFragment.setBook(book);
                                mReaderFragment.setBookList(bookList);
                                mReaderFragment.loadBook();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mReaderFragment.onError(e);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mReaderFragment.onError(e);
            }
        });
    }

    @Override
    public void saveBookMark(int bookPage) {
        Single<BookMarkDto> single = new Single<BookMarkDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookMarkDto> observer) {
                try {
                    BookMarkDto bookMark = new BookMarkDto();
                    bookMark.setPage(bookPage);

                    bookMark = mApplicationService.createOrUpdateBookMark(mBookId, bookMark).blockingGet();

                    observer.onSuccess(bookMark);
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookMarkDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookMarkDto bookMark) {
                BookDto book = mReaderFragment.getBook();
                book.setBookMark(bookMark);
            }

            @Override
            public void onError(Throwable e) {
                mReaderFragment.onError(e);
            }
        });
    }
}
