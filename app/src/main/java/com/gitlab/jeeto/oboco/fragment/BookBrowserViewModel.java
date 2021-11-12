package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public abstract class BookBrowserViewModel extends BaseViewModel {
    public static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";
    public static final String PARAM_FILTER_TYPE = "PARAM_FILTER_TYPE";
    protected MutableLiveData<String> mTitleObservable;
    protected MutableLiveData<BookCollectionDto> mBookCollectionObservable;
    protected MutableLiveData<List<BookDto>> mBookListObservable;
    protected MutableLiveData<Integer> mBookListSizeObservable;
    protected MutableLiveData<String> mFilterTypeObservable;
    protected MutableLiveData<Boolean> mIsLoadingObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<BookDto> mSelectedBookObservable;
    protected MutableLiveData<Boolean> mShowMarkSelectedBookDialogObservable;
    protected MutableLiveData<Boolean> mShowDownloadSelectedBookDialogObservable;

    public BookBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mTitleObservable = new MutableLiveData<String>();
        mBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mBookListObservable= new MutableLiveData<List<BookDto>>();
        mBookListSizeObservable= new MutableLiveData<Integer>();
        mFilterTypeObservable = new MutableLiveData<String>();
        mIsLoadingObservable = new MutableLiveData<Boolean>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedBookObservable = new MutableLiveData<BookDto>();
        mShowMarkSelectedBookDialogObservable = new MutableLiveData<Boolean>();
        mShowDownloadSelectedBookDialogObservable = new MutableLiveData<Boolean>();
    }

    public String getTitle() {
        return mTitleObservable.getValue();
    }
    public LiveData<String> getTitleObservable() {
        return mTitleObservable;
    }
    public BookCollectionDto getBookCollection() {
        return mBookCollectionObservable.getValue();
    }
    public LiveData<BookCollectionDto> getBookCollectionObservable() {
        return mBookCollectionObservable;
    }
    public List<BookDto> getBookList() {
        return mBookListObservable.getValue();
    }
    public LiveData<List<BookDto>> getBookListObservable() {
        return mBookListObservable;
    }
    public Integer getBookListSize() {
        return mBookListSizeObservable.getValue();
    }
    public LiveData<Integer> getBookListSizeObservable() {
        return mBookListSizeObservable;
    }
    public String getFilterType() {
        return mFilterTypeObservable.getValue();
    }
    public LiveData<String> getFilterTypeObservable() {
        return mFilterTypeObservable;
    }
    public void setFilterType(String filterType) {
        mFilterTypeObservable.setValue(filterType);
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
    public BookDto getSelectedBook() {
        return mSelectedBookObservable.getValue();
    }
    public LiveData<BookDto> getSelectedBookObservable() {
        return mSelectedBookObservable;
    }
    public void setSelectedBook(BookDto book) {
        mSelectedBookObservable.setValue(book);
    }
    public Boolean getShowMarkSelectedBookDialog() {
        return mShowMarkSelectedBookDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowMarkSelectedBookDialogObservable() {
        return mShowMarkSelectedBookDialogObservable;
    }
    public void setShowMarkSelectedBookDialog(Boolean showMarkSelectedBookDialog) {
        mShowMarkSelectedBookDialogObservable.setValue(showMarkSelectedBookDialog);
    }
    public Boolean getShowDownloadSelectedBookDialog() {
        return mShowDownloadSelectedBookDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowDownloadSelectedBookDialogObservable() {
        return mShowDownloadSelectedBookDialogObservable;
    }
    public void setShowDownloadSelectedBookDialog(Boolean showDownloadSelectedBookDialog) {
        mShowDownloadSelectedBookDialogObservable.setValue(showDownloadSelectedBookDialog);
    }
    public void updateBookList(List<BookDto> updatedBookListDto) {
        if(updatedBookListDto.size() != 0) {
            List<BookDto> bookList = mBookListObservable.getValue();

            int index = 0;

            while (index < bookList.size()) {
                BookDto bookDto = bookList.get(index);

                for (BookDto updatedBookDto : updatedBookListDto) {
                    if (bookDto.equals(updatedBookDto)) {
                        bookList.set(index, updatedBookDto);
                    }
                }

                index = index + 1;
            }

            mBookListObservable.setValue(bookList);
        }
    }
    public abstract void load();
    public abstract void loadBookList();
    public abstract Boolean hasNextBookList();
    public abstract void loadNextBookList();
    public abstract void addBookMark(BookMarkDto bookMarkDto);
    public abstract void removeBookMark();
    public abstract Uri getBookPageUri(BookDto bookDto, String scaleType, int scaleWidth, int scaleHeight);
    public abstract Picasso getPicasso();
}
