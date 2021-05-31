package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.fragment.LibraryBrowserFragment;

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

public class RemoteBookBrowserManager extends BookBrowserManager {
    private LibraryBrowserFragment mLibraryBrowserFragment;
    private Long mBookCollectionId;

    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteBookBrowserManager(Long bookCollectionId) {
        super();
        mBookCollectionId = bookCollectionId;
    }

    public void create(LibraryBrowserFragment libraryBrowserFragment) {
        mLibraryBrowserFragment = libraryBrowserFragment;

        SharedPreferences sp = mLibraryBrowserFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mLibraryBrowserFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mLibraryBrowserFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mLibraryBrowserFragment.getContext(), mBaseUrl, mAuthenticationManager);
    }

    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void load(String bookMarkStatus, int page, int pageSize) {
        mLibraryBrowserFragment.setRefreshing(true);

        Single<BookCollectionDto> single = new Single<BookCollectionDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionDto> observer) {
                try {
                    if(mBookCollectionId == null) {
                        BookCollectionDto bookCollection = mApplicationService.getRootBookCollection("(parentBookCollection)").blockingGet();

                        observer.onSuccess(bookCollection);
                    } else {
                        BookCollectionDto bookCollection = mApplicationService.getBookCollection(mBookCollectionId, "(parentBookCollection)").blockingGet();

                        observer.onSuccess(bookCollection);
                    }
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<BookCollectionDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(BookCollectionDto bookCollection) {
                if(bookCollection != null) {
                    Single<PageableListDto<BookDto>> single = new Single<PageableListDto<BookDto>>() {
                        @Override
                        protected void subscribeActual(SingleObserver<? super PageableListDto<BookDto>> observer) {
                            try {
                                PageableListDto<BookDto> bookPageableList = mApplicationService.getBooks(bookCollection.getId(), bookMarkStatus, page, pageSize, "(bookMark)").blockingGet();

                                observer.onSuccess(bookPageableList);
                            } catch(Exception e) {
                                observer.onError(e);
                            }
                        }
                    };
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PageableListDto<BookDto> bookPageableList) {
                            mLibraryBrowserFragment.onLoad(bookCollection, bookPageableList);
                            mLibraryBrowserFragment.setRefreshing(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mLibraryBrowserFragment.onError(e);
                            mLibraryBrowserFragment.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mLibraryBrowserFragment.onError(e);
                mLibraryBrowserFragment.setRefreshing(false);
            }
        });
    }

    @Override
    public void loadBookPageableList(String bookMarkStatus, int page, int pageSize) {
        mLibraryBrowserFragment.setRefreshing(true);

        Single<PageableListDto<BookDto>> single = new Single<PageableListDto<BookDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookDto>> observer) {
                try {
                    PageableListDto<BookDto> bookPageableList = mApplicationService.getBooks(mBookCollectionId, bookMarkStatus, page, pageSize, "(bookMark)").blockingGet();

                    observer.onSuccess(bookPageableList);
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookDto> bookPageableList) {
                mLibraryBrowserFragment.onLoadBookPageableList(bookPageableList);
                mLibraryBrowserFragment.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryBrowserFragment.onError(e);
                mLibraryBrowserFragment.setRefreshing(false);
            }
        });
    }

    @Override
    public void addBookMark(BookDto book) {
        Single<BookMarkDto> single = new Single<BookMarkDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookMarkDto> observer) {
                try {
                    BookMarkDto bookMark = new BookMarkDto();
                    bookMark.setPage(book.getNumberOfPages());

                    bookMark = mApplicationService.createOrUpdateBookMark(book.getId(), bookMark).blockingGet();

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
                mLibraryBrowserFragment.onAddBookMark(book, bookMark);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryBrowserFragment.onError(e);
            }
        });
    }

    @Override
    public void removeBookMark(BookDto book) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                try {
                    mApplicationService.deleteBookMark(book.getId()).blockingAwait();

                    observer.onComplete();
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mLibraryBrowserFragment.onRemoveBookMark(book);
            }

            @Override
            public void onError(Throwable e) {
                mLibraryBrowserFragment.onError(e);
            }
        });
    }

    public InputStream getBookPage(BookDto book, String scaleType, int scaleWidth, int scaleHeight) throws IOException {
        ResponseBody responseBody = mApplicationService.downloadBookPage(book.getId(), 1, scaleType, scaleWidth, scaleHeight).blockingGet();

        InputStream inputStream = responseBody.byteStream();

        return inputStream;
    }
}
