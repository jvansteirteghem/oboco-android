package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.os.Bundle;
import android.os.Environment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;

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

public class DownloadBrowserViewModel extends BaseViewModel {
    public final static String PARAM_BOOK_COLLECTION_PATH = "PARAM_BOOK_COLLECTION_PATH";
    protected MutableLiveData<BookCollectionDto> mCurrentBookCollectionObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<BookCollectionDto> mSelectedBookCollectionObservable;
    protected MutableLiveData<BookDto> mSelectedBookObservable;
    protected MutableLiveData<Boolean> mShowDeleteSelectedBookCollectionDialogObservable;
    protected MutableLiveData<Boolean> mShowDeleteSelectedBookDialogObservable;

    private File mRootFile;

    private AppDatabase mAppDatabase;

    public DownloadBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mAppDatabase = Room.databaseBuilder(getApplication().getApplicationContext(), AppDatabase.class, "database").build();

        mCurrentBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mSelectedBookObservable = new MutableLiveData<BookDto>();
        mShowDeleteSelectedBookCollectionDialogObservable = new MutableLiveData<Boolean>();
        mShowDeleteSelectedBookDialogObservable = new MutableLiveData<Boolean>();

        load();
    }

    public BookCollectionDto getCurrentBookCollection() {
        return mCurrentBookCollectionObservable.getValue();
    }
    public LiveData<BookCollectionDto> getCurrentBookCollectionObservable() {
        return mCurrentBookCollectionObservable;
    }
    public void setCurrentBookCollection(BookCollectionDto currentBookCollection) {
        mCurrentBookCollectionObservable.setValue(currentBookCollection);
    }
    public String getMessage() {
        return mMessageObservable.getValue();
    }
    public LiveData<String> getMessageObservable() {
        return mMessageObservable;
    }
    public void setMessage(String message) {
        mMessageObservable.setValue(message);
    }
    public Boolean getShowMessage() {
        return mShowMessageObservable.getValue();
    }
    public LiveData<Boolean> getShowMessageObservable() {
        return mShowMessageObservable;
    }
    public void setShowMessage(Boolean showMessage) {
        mShowMessageObservable.setValue(showMessage);
    }
    public BookCollectionDto getSelectedBookCollection() {
        return mSelectedBookCollectionObservable.getValue();
    }
    public LiveData<BookCollectionDto> getSelectedBookCollectionObservable() {
        return mSelectedBookCollectionObservable;
    }
    public void setSelectedBookCollection(BookCollectionDto bookCollection) {
        mSelectedBookCollectionObservable.setValue(bookCollection);
    }
    public BookDto getSelectedBook() {
        return mSelectedBookObservable.getValue();
    }
    public LiveData<BookDto> getSelectedBookObservable() {
        return mSelectedBookObservable;
    }
    public void setSelectedBook(BookDto book) {
        mSelectedBookObservable.setValue(book);
    }
    public Boolean getShowDeleteSelectedBookCollectionDialog() {
        return mShowDeleteSelectedBookCollectionDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowDeleteSelectedBookCollectionDialogObservable() {
        return mShowDeleteSelectedBookCollectionDialogObservable;
    }
    public void setShowDeleteSelectedBookCollectionDialog(Boolean showDeleteSelectedBookCollectionDialog) {
        mShowDeleteSelectedBookCollectionDialogObservable.setValue(showDeleteSelectedBookCollectionDialog);
    }
    public Boolean getShowDeleteSelectedBookDialog() {
        return mShowDeleteSelectedBookDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowDeleteSelectedBookDialogObservable() {
        return mShowDeleteSelectedBookDialogObservable;
    }
    public void setShowDeleteSelectedBookDialog(Boolean showDeleteSelectedBookDialog) {
        mShowDeleteSelectedBookDialogObservable.setValue(showDeleteSelectedBookDialog);
    }

    @Override
    protected void onCleared() {
        if(mAppDatabase != null) {
            if(mAppDatabase.isOpen()) {
                mAppDatabase.close();
            }
            mAppDatabase = null;
        }
    }

    public void load() {
        mRootFile = getApplication().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File file;
        if(getArguments() != null) {
            String path = getArguments().getString(PARAM_BOOK_COLLECTION_PATH);

            file = new File(path);
        } else {
            file = mRootFile;
        }

        BookCollectionDto bookCollectionDto = new BookCollectionDto();
        bookCollectionDto.setName(file.getName());
        bookCollectionDto.setPath(file.getAbsolutePath());

        loadBookCollection(bookCollectionDto);
    }

    public void loadBookCollection(BookCollectionDto bookCollectionDto) {
        BookCollectionDto currentBookCollectionDto = getCurrentBookCollection(bookCollectionDto.getPath());

        Single<List<Book>> single = mAppDatabase.bookDao().findByBookCollectionPath(currentBookCollectionDto.getPath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                for(BookDto bookDto: currentBookCollectionDto.getBooks()) {
                    for(Book book: bookList) {
                        if(bookDto.getPath().equals(book.path)) {
                            bookDto.setNumberOfPages(book.numberOfPages);

                            BookMarkDto bookMarkDto = new BookMarkDto();
                            bookMarkDto.setPage(book.page);

                            bookDto.setBookMark(bookMarkDto);

                            break;
                        }
                    }
                }

                mCurrentBookCollectionObservable.setValue(currentBookCollectionDto);
            }

            @Override
            public void onError(Throwable e) {
                String message = getMessage(R.string.action_books_get_error);

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });
    }

    private BookCollectionDto getCurrentBookCollection(String path) {
        File currentFile = new File(path);

        BookCollectionDto currentBookCollectionDto = new BookCollectionDto();
        currentBookCollectionDto.setName(currentFile.getName());
        currentBookCollectionDto.setPath(currentFile.getAbsolutePath());

        if(!currentFile.getAbsolutePath().equals(mRootFile.getAbsolutePath())) {
            File parentFile = currentFile.getParentFile();

            BookCollectionDto parentBookCollectionDto = new BookCollectionDto();
            parentBookCollectionDto.setName(parentFile.getName());
            parentBookCollectionDto.setPath(parentFile.getAbsolutePath());

            currentBookCollectionDto.setParentBookCollection(parentBookCollectionDto);
        }

        List<BookCollectionDto> bookCollectionListDto = new ArrayList<BookCollectionDto>();
        List<BookDto> bookListDto = new ArrayList<BookDto>();

        File[] files = currentFile.listFiles();
        if(files != null) {
            int index = 0;

            while(index < files.length) {
                File file = files[index];

                if(file.isDirectory()) {
                    BookCollectionDto bookCollectionDto = new BookCollectionDto();
                    bookCollectionDto.setName(file.getName());
                    bookCollectionDto.setPath(file.getAbsolutePath());

                    bookCollectionListDto.add(bookCollectionDto);
                } else if(file.isFile()) {
                    BookDto bookDto = new BookDto();
                    bookDto.setName(file.getName());
                    bookDto.setPath(file.getAbsolutePath());

                    bookListDto.add(bookDto);
                }

                index = index + 1;
            }
        }

        Collections.sort(bookCollectionListDto, new NaturalOrderComparator<BookCollectionDto>() {
            @Override
            public String toString(BookCollectionDto o) {
                return o.getName();
            }
        });

        Collections.sort(bookListDto, new NaturalOrderComparator<BookDto>() {
            @Override
            public String toString(BookDto o) {
                return o.getName();
            }
        });

        currentBookCollectionDto.setBookCollections(bookCollectionListDto);
        currentBookCollectionDto.setBooks(bookListDto);

        return currentBookCollectionDto;
    }

    public void deleteSelectedBook() {
        BookDto selectedBookDto = mSelectedBookObservable.getValue();

        File selectedFile = new File(selectedBookDto.getPath());
        selectedFile.delete();

        BookCollectionDto currentBookCollectionDto = mCurrentBookCollectionObservable.getValue();
        currentBookCollectionDto.getBooks().remove(selectedBookDto);

        Single<List<Book>> single = mAppDatabase.bookDao().findByPath(selectedBookDto.getPath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                Completable complatable = mAppDatabase.bookDao().delete(bookList);
                complatable = complatable.observeOn(AndroidSchedulers.mainThread());
                complatable = complatable.subscribeOn(Schedulers.io());
                complatable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onComplete() {
                        mCurrentBookCollectionObservable.setValue(currentBookCollectionDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        String message = getMessage(R.string.action_book_delete_error);

                        mMessageObservable.setValue(message);
                        mShowMessageObservable.setValue(true);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                String message = getMessage(R.string.action_book_delete_error);

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });
    }

    public void deleteSelectedBookCollection() {
        BookCollectionDto selectedBookCollectionDto = mSelectedBookCollectionObservable.getValue();

        File selectedFile = new File(selectedBookCollectionDto.getPath());
        for(File file: selectedFile.listFiles()) {
            file.delete();
        }
        selectedFile.delete();

        BookCollectionDto currentBookCollectionDto = mCurrentBookCollectionObservable.getValue();
        currentBookCollectionDto.getBookCollections().remove(selectedBookCollectionDto);

        Single<List<Book>> single = mAppDatabase.bookDao().findByBookCollectionPath(selectedBookCollectionDto.getPath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                Completable complatable = mAppDatabase.bookDao().delete(bookList);
                complatable = complatable.observeOn(AndroidSchedulers.mainThread());
                complatable = complatable.subscribeOn(Schedulers.io());
                complatable.subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onComplete() {
                        mCurrentBookCollectionObservable.setValue(currentBookCollectionDto);
                    }

                    @Override
                    public void onError(Throwable e) {
                        String message = getMessage(R.string.action_book_collection_delete_error);

                        mMessageObservable.setValue(message);
                        mShowMessageObservable.setValue(true);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                String message = getMessage(R.string.action_book_collection_delete_error);

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);
            }
        });
    }
}
