package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public abstract class BookCollectionBrowserViewModel extends BaseViewModel {
    public static final String PARAM_MODE = "PARAM_MODE";
    public enum Mode {
        MODE_REMOTE,
        MODE_REMOTE_ALL,
        MODE_REMOTE_NEW,
        MODE_REMOTE_LATEST_READ,
        MODE_REMOTE_READ,
        MODE_REMOTE_READING,
        MODE_REMOTE_UNREAD;
    }
    public static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";
    protected MutableLiveData<BookCollectionDto> mBookCollectionObservable;
    protected MutableLiveData<List<BookCollectionDto>> mBookCollectionListObservable;
    protected MutableLiveData<String> mSearchTypeObservable;
    protected MutableLiveData<String> mSearchObservable;
    protected MutableLiveData<Boolean> mIsLoadingObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<BookCollectionDto> mSelectedBookCollectionObservable;
    protected MutableLiveData<Boolean> mShowMarkSelectedBookCollectionDialogObservable;
    protected MutableLiveData<Boolean> mShowDownloadSelectedBookCollectionDialogObservable;

    public BookCollectionBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mBookCollectionListObservable= new MutableLiveData<List<BookCollectionDto>>();
        mSearchTypeObservable = new MutableLiveData<String>();
        mSearchObservable = new MutableLiveData<String>();
        mIsLoadingObservable = new MutableLiveData<Boolean>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mShowMarkSelectedBookCollectionDialogObservable = new MutableLiveData<Boolean>();
        mShowDownloadSelectedBookCollectionDialogObservable = new MutableLiveData<Boolean>();
    }

    public BookCollectionDto getBookCollection() {
        return mBookCollectionObservable.getValue();
    }
    public LiveData<BookCollectionDto> getBookCollectionObservable() {
        return mBookCollectionObservable;
    }
    public List<BookCollectionDto> getBookCollectionList() {
        return mBookCollectionListObservable.getValue();
    }
    public LiveData<List<BookCollectionDto>> getBookCollectionListObservable() {
        return mBookCollectionListObservable;
    }
    public String getSearchType() {
        return mSearchTypeObservable.getValue();
    }
    public LiveData<String> getSearchTypeObservable() {
        return mSearchTypeObservable;
    }
    public void setSearchType(String searchType) {
        mSearchTypeObservable.setValue(searchType);
    }
    public String getSearch() {
        return mSearchObservable.getValue();
    }
    public LiveData<String> getSearchObservable() {
        return mSearchObservable;
    }
    public void setSearch(String search) {
        mSearchObservable.setValue(search);
    }
    public Boolean getIsLoading() {
        return mIsLoadingObservable.getValue();
    }
    public LiveData<Boolean> getIsLoadingObservable() {
        return mIsLoadingObservable;
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
    public Boolean getShowMarkSelectedBookCollectionDialog() {
        return mShowMarkSelectedBookCollectionDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowMarkSelectedBookCollectionDialogObservable() {
        return mShowMarkSelectedBookCollectionDialogObservable;
    }
    public void setShowMarkSelectedBookCollectionDialog(Boolean showMarkSelectedBookCollectionDialog) {
        mShowMarkSelectedBookCollectionDialogObservable.setValue(showMarkSelectedBookCollectionDialog);
    }
    public Boolean getShowDownloadSelectedBookCollectionDialog() {
        return mShowDownloadSelectedBookCollectionDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowDownloadSelectedBookCollectionDialogObservable() {
        return mShowDownloadSelectedBookCollectionDialogObservable;
    }
    public void setShowDownloadSelectedBookCollectionDialog(Boolean showDownloadSelectedBookCollectionDialog) {
        mShowDownloadSelectedBookCollectionDialogObservable.setValue(showDownloadSelectedBookCollectionDialog);
    }

    public abstract void load();
    public abstract void loadBookCollectionList();
    public abstract Boolean hasNextBookCollectionList();
    public abstract void loadNextBookCollectionList();
    public abstract void addBookMark();
    public abstract void removeBookMark();
    public abstract Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight);
    public abstract Picasso getPicasso();
}
