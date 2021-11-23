package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookCollectionMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public abstract class BookCollectionBrowserViewModel extends BaseViewModel {
    public static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";
    public static final String PARAM_FILTER_TYPE = "PARAM_FILTER_TYPE";
    protected MutableLiveData<String> mTitleObservable;
    protected MutableLiveData<Long> mBookCollectionIdObservable;
    protected MutableLiveData<BookCollectionDto> mBookCollectionObservable;
    protected MutableLiveData<List<BookCollectionDto>> mBookCollectionListObservable;
    protected MutableLiveData<Integer> mBookCollectionListSizeObservable;
    protected MutableLiveData<String> mSearchTypeObservable;
    protected MutableLiveData<String> mSearchObservable;
    protected MutableLiveData<String> mFilterTypeObservable;
    protected MutableLiveData<Boolean> mIsLoadingObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<BookCollectionDto> mSelectedBookCollectionObservable;
    protected MutableLiveData<Boolean> mShowMarkSelectedBookCollectionDialogObservable;
    protected MutableLiveData<Boolean> mShowDownloadSelectedBookCollectionDialogObservable;

    public BookCollectionBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mTitleObservable = new MutableLiveData<String>();
        mBookCollectionIdObservable = new MutableLiveData<Long>();
        mBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mBookCollectionListObservable= new MutableLiveData<List<BookCollectionDto>>();
        mBookCollectionListSizeObservable= new MutableLiveData<Integer>();
        mSearchTypeObservable = new MutableLiveData<String>();
        mSearchObservable = new MutableLiveData<String>();
        mFilterTypeObservable = new MutableLiveData<String>();
        mIsLoadingObservable = new MutableLiveData<Boolean>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedBookCollectionObservable = new MutableLiveData<BookCollectionDto>();
        mShowMarkSelectedBookCollectionDialogObservable = new MutableLiveData<Boolean>();
        mShowDownloadSelectedBookCollectionDialogObservable = new MutableLiveData<Boolean>();
    }

    public String getTitle() {
        return mTitleObservable.getValue();
    }
    public LiveData<String> getTitleObservable() {
        return mTitleObservable;
    }
    public Long getBookCollectionId() {
        return mBookCollectionIdObservable.getValue();
    }
    public LiveData<Long> getBookCollectionIdObservable() {
        return mBookCollectionIdObservable;
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
    public Integer getBookCollectionListSize() {
        return mBookCollectionListSizeObservable.getValue();
    }
    public LiveData<Integer> getBookCollectionListSizeObservable() {
        return mBookCollectionListSizeObservable;
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
    public void updateBookCollectionList(List<BookCollectionDto> updatedBookCollectionListDto) {
        if(updatedBookCollectionListDto.size() != 0) {
            List<BookCollectionDto> bookCollectionList = mBookCollectionListObservable.getValue();

            int index = 0;

            while (index < bookCollectionList.size()) {
                BookCollectionDto bookCollectionDto = bookCollectionList.get(index);

                for (BookCollectionDto updatedBookCollectionDto : updatedBookCollectionListDto) {
                    if (bookCollectionDto.equals(updatedBookCollectionDto)) {
                        bookCollectionList.set(index, updatedBookCollectionDto);
                    }
                }

                index = index + 1;
            }

            mBookCollectionListObservable.setValue(bookCollectionList);
        }
    }
    public abstract void load();
    public abstract void loadBookCollectionList();
    public abstract Boolean hasNextBookCollectionList();
    public abstract void loadNextBookCollectionList();
    public abstract void addBookMark(BookCollectionMarkDto bookCollectionMarkDto);
    public abstract void removeBookMark();
    public abstract Uri getBookCollectionPageUri(BookCollectionDto bookCollectionDto, String scaleType, int scaleWidth, int scaleHeight);
    public abstract Picasso getPicasso();
}
