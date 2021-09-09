package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.os.Bundle;

import androidx.room.Room;

import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.LinkableDto;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;
import com.gitlab.jeeto.oboco.reader.BookReader;
import com.gitlab.jeeto.oboco.reader.ZipBookReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    public BookReaderRequestHandler getRequestHandler() {
        return new LocalBookReaderRequestHandler(mBookReader);
    }

    @Override
    public void load() {
        mIsFullscreenObservable.setValue(true);

        BookDto bookDto = new BookDto();
        LinkableDto<BookDto> bookLinkableDto = new LinkableDto<BookDto>();

        File[] files = mBookFile.getParentFile().listFiles();
        List<File> bookFileList = new ArrayList<File>();
        for(File file: files) {
            if(file.isFile() && file.getName().endsWith(".cbz")) {
                bookFileList.add(file);
            }
        }

        Collections.sort(bookFileList, new NaturalOrderComparator<File>() {
            @Override
            public String toString(File o) {
                return o.getName();
            }
        });

        Integer index = 0;
        while(index < bookFileList.size()) {
            File bookFile = bookFileList.get(index);

            if(bookFile.getName().equals(mBookFile.getName())) {
                if(index - 1 >= 0) {
                    File previousBookFile = bookFileList.get(index - 1);

                    BookDto previousBookDto = new BookDto();
                    previousBookDto.setName(previousBookFile.getName());
                    previousBookDto.setNumberOfPages(0);
                    previousBookDto.setPath(previousBookFile.getAbsolutePath());

                    bookLinkableDto.setPreviousElement(previousBookDto);
                }
                bookDto.setName(bookFile.getName());
                bookDto.setNumberOfPages(mBookReader.getNumberOfPages());
                bookDto.setPath(bookFile.getAbsolutePath());

                bookLinkableDto.setElement(bookDto);
                if(index + 1 < bookFileList.size()) {
                    File nextBookFile = bookFileList.get(index + 1);

                    BookDto nextBookDto = new BookDto();
                    nextBookDto.setName(nextBookFile.getName());
                    nextBookDto.setNumberOfPages(0);
                    nextBookDto.setPath(nextBookFile.getAbsolutePath());

                    bookLinkableDto.setNextElement(nextBookDto);
                }
                break;
            }

            index = index + 1;
        }

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
        BookMarkDto bookMarkDto = new BookMarkDto();
        bookMarkDto.setPage(mSelectedBookPageObservable.getValue());

        BookDto bookDto = mBookObservable.getValue();
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
}
