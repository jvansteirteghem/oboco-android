package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.client.LinkableDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.squareup.picasso.Picasso;

public abstract class BookReaderViewModel extends BaseViewModel {
    public static final String PARAM_MODE = "PARAM_MODE";
    public enum Mode {
        MODE_REMOTE,
        MODE_LOCAL;
    }
    public static final String PARAM_BOOK_ID = "PARAM_BOOK_ID";
    public static final String PARAM_BOOK_PATH = "PARAM_BOOK_PATH";
    protected MutableLiveData<BookDto> mBookObservable;
    protected MutableLiveData<LinkableDto<BookDto>> mBookLinkableObservable;
    protected MutableLiveData<BookMarkDto> mBookMarkObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<BookDto> mSelectedBookObservable;
    protected MutableLiveData<Boolean> mShowSwitchSelectedBookDialogObservable;
    protected MutableLiveData<Boolean> mIsFullscreenObservable;
    protected MutableLiveData<Integer> mSelectedBookPageObservable;

    public BookReaderViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBookObservable = new MutableLiveData<BookDto>();
        mBookLinkableObservable = new MutableLiveData<LinkableDto<BookDto>>();
        mBookMarkObservable = new MutableLiveData<BookMarkDto>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedBookObservable = new MutableLiveData<BookDto>();
        mShowSwitchSelectedBookDialogObservable = new MutableLiveData<Boolean>();
        mIsFullscreenObservable = new MutableLiveData<Boolean>();
        mSelectedBookPageObservable = new MutableLiveData<Integer>();
    }

    public BookDto getBook() {
        return mBookObservable.getValue();
    }
    public LiveData<BookDto> getBookObservable() {
        return mBookObservable;
    }
    public LinkableDto<BookDto> getBookLinkable() {
        return mBookLinkableObservable.getValue();
    }
    public LiveData<LinkableDto<BookDto>> getBookLinkableObservable() {
        return mBookLinkableObservable;
    }
    public BookMarkDto getBookMark() {
        return mBookMarkObservable.getValue();
    }
    public LiveData<BookMarkDto> getBookMarkObservable() {
        return mBookMarkObservable;
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
    public BookDto getSelectedBook() {
        return mSelectedBookObservable.getValue();
    }
    public LiveData<BookDto> getSelectedBookObservable() {
        return mSelectedBookObservable;
    }
    public void setSelectedBook(BookDto book) {
        mSelectedBookObservable.setValue(book);
    }
    public Boolean getShowSwitchSelectedBookDialog() {
        return mShowSwitchSelectedBookDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowSwitchSelectedBookDialogObservable() {
        return mShowSwitchSelectedBookDialogObservable;
    }
    public void setShowSwitchSelectedBookDialog(Boolean showSwitchSelectedBookDialog) {
        mShowSwitchSelectedBookDialogObservable.setValue(showSwitchSelectedBookDialog);
    }
    public Boolean getIsFullscreen() {
        return mIsFullscreenObservable.getValue();
    }
    public LiveData<Boolean> getIsFullscreenObservable() {
        return mIsFullscreenObservable;
    }
    public void setIsFullscreen(Boolean isFullscreen) {
        mIsFullscreenObservable.setValue(isFullscreen);
    }
    public Integer getSelectedBookPage() {
        return mSelectedBookPageObservable.getValue();
    }
    public LiveData<Integer> getSelectedBookPageObservable() {
        return mSelectedBookPageObservable;
    }
    public void setSelectedBookPage(Integer bookPage) {
        mSelectedBookPageObservable.setValue(bookPage);
    }

    public abstract void load();
    public abstract void addBookMark();
    public abstract Uri getBookPageUri(int bookPage);
    public abstract Picasso getPicasso();
}
