package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import androidx.room.Room;

import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.LinkableDto;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;
import com.gitlab.jeeto.oboco.reader.BookReader;
import com.gitlab.jeeto.oboco.reader.ZipBookReader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LocalBookReaderViewModel extends BookReaderViewModel {
    private String mBookPath;

    private BookReader mBookReader;
    private File mBookFile;
    private AppDatabase mAppDatabase;

    private BookReaderRequestHandler mRequestHandler;
    private Picasso mPicasso;

    public LocalBookReaderViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBookPath = getArguments().getString(BookReaderViewModel.PARAM_BOOK_PATH);

        mBookFile = new File(mBookPath);

        try {
            mBookReader = new ZipBookReader(mBookFile);
            mBookReader.create();
        } catch(Exception e) {
            mMessageObservable.setValue(toMessage(e));
            mShowMessageObservable.setValue(true);
        }

        mAppDatabase = Room.databaseBuilder(getApplication().getApplicationContext(), AppDatabase.class, "database").build();

        mRequestHandler = new LocalBookReaderRequestHandler(mBookReader);

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
        if(mAppDatabase != null) {
            if(mAppDatabase.isOpen()) {
                mAppDatabase.close();
            }
            mAppDatabase = null;
        }

        try {
            mBookReader.destroy();
        } catch(Exception e) {
            mMessageObservable.setValue(toMessage(e));
            mShowMessageObservable.setValue(true);
        }
    }

    @Override
    public void load() {
        mIsFullscreenObservable.setValue(true);

        BookDto bookDto = new BookDto();
        bookDto.setName(mBookFile.getName());
        bookDto.setNumberOfPages(mBookReader.getNumberOfPages());
        bookDto.setPath(mBookFile.getAbsolutePath());

        LinkableDto<BookDto> bookLinkableDto = new LinkableDto<BookDto>();
        bookLinkableDto.setElement(bookDto);

        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(mBookFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                if(bookList.size() == 1) {
                    Book book = bookList.get(0);

                    BookMarkDto bookMarkDto = new BookMarkDto();
                    bookMarkDto.setPage(book.page);

                    bookDto.setBookMark(bookMarkDto);

                    mSelectedBookPageObservable.setValue(bookMarkDto.getPage());
                } else {
                    mSelectedBookPageObservable.setValue(1);
                }

                mBookObservable.setValue(bookDto);
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
    public void addBookMark() {
        BookDto bookDto = mBookObservable.getValue();

        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setNumberOfPages(bookDto.getNumberOfPages());
        bookMarkDto.setPage(mSelectedBookPageObservable.getValue());

        bookDto.setBookMark(bookMarkDto);

        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(mBookFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                if(bookList.size() == 1) {
                    Book book = bookList.get(0);
                    book.page = bookMarkDto.getPage();

                    Completable completable = mAppDatabase.bookDao().update(book);
                    completable = completable.observeOn(AndroidSchedulers.mainThread());
                    completable = completable.subscribeOn(Schedulers.io());
                    completable.subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onComplete() {
                            mBookMarkObservable.setValue(bookMarkDto);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mMessageObservable.setValue(toMessage(e));
                            mShowMessageObservable.setValue(true);
                        }
                    });
                } else {
                    Book book = new Book();
                    book.path = mBookFile.getAbsolutePath();
                    book.bookCollectionPath = mBookFile.getParentFile().getAbsolutePath();
                    book.page = bookMarkDto.getPage();
                    book.numberOfPages = mBookReader.getNumberOfPages();

                    Completable completable = mAppDatabase.bookDao().create(book);
                    completable = completable.observeOn(AndroidSchedulers.mainThread());
                    completable = completable.subscribeOn(Schedulers.io());
                    completable.subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable disposable) {

                        }

                        @Override
                        public void onComplete() {
                            mBookMarkObservable.setValue(bookMarkDto);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mMessageObservable.setValue(toMessage(e));
                            mShowMessageObservable.setValue(true);
                        }
                    });
                }
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
