package com.gitlab.jeeto.oboco.fragment;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gitlab.jeeto.oboco.BuildConfig;
import com.gitlab.jeeto.oboco.Constants;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.MainActivity;
import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationInterceptor;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.managers.Utils;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.DOWNLOAD_SERVICE;


public class LibraryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    final int ITEM_VIEW_TYPE_GROUP = 1;
    private Picasso mPicasso;
    private RecyclerView mGroupListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;

    private String mFilterSearch = "";

    private static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";
    private static final String STATE_CURRENT_BOOK_COLLECTION_ID = "STATE_CURRENT_BOOK_COLLECTION_ID";
    private Long mBookCollectionId;
    private BookCollectionDto mBookCollection;
    private List<BookCollectionDto> mBookCollectionList;
    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    private OnErrorListener mOnErrorListener;

    private int mPage = 0;
    private int mPageSize = 100;
    private int mNextPage = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnErrorListener) {
            mOnErrorListener = (OnErrorListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnErrorListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mAuthenticationManagerDisposable.dispose();
        super.onDestroy();
    }

    public static LibraryFragment create(Long bookCollectionId) {
        LibraryFragment fragment = new LibraryFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mBookCollectionId = savedInstanceState.getLong(STATE_CURRENT_BOOK_COLLECTION_ID);
        }
        else {
            if(getArguments() != null) {
                mBookCollectionId = getArguments().getLong(PARAM_BOOK_COLLECTION_ID);
            } else {
                mBookCollectionId = null;
            }
        }

        mBookCollectionList = new ArrayList<BookCollectionDto>();

        SharedPreferences sp = getContext().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mOnErrorListener.onError(e);
            }
        });

        mApplicationService = new ApplicationService(getContext(), mBaseUrl, mAuthenticationManager);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(mAuthenticationManager));

        if(BuildConfig.DEBUG) {
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC));
        }

        OkHttpClient client = builder.build();

        mPicasso = new Picasso.Builder(getActivity())
                .downloader(new OkHttp3Downloader(client))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        mOnErrorListener.onError(exception);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();
    }

    private void setCurrentBookCollection(Long bookCollectionId) {
        mRefreshView.setRefreshing(true);

        Single<BookCollectionDto> single = new Single<BookCollectionDto>() {
            @Override
            protected void subscribeActual(SingleObserver<? super BookCollectionDto> observer) {
                try {
                    if(bookCollectionId == null) {
                        BookCollectionDto bookCollection = mApplicationService.getRootBookCollection("(parentBookCollection)").blockingGet();

                        observer.onSuccess(bookCollection);
                    } else {
                        BookCollectionDto bookCollection = mApplicationService.getBookCollection(bookCollectionId, "(parentBookCollection)").blockingGet();

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
                    Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
                        @Override
                        protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                            try {
                                String name = null;
                                if (mFilterSearch.length() > 0) {
                                    name = mFilterSearch;
                                }

                                PageableListDto<BookCollectionDto> bookCollectionPageableList = mApplicationService.getBookCollections(bookCollection.getId(), name, 1, mPageSize, "(bookCollections,books)").blockingGet();

                                observer.onSuccess(bookCollectionPageableList);
                            } catch(Exception e) {
                                observer.onError(e);
                            }
                        }
                    };
                    single = single.observeOn(AndroidSchedulers.mainThread());
                    single = single.subscribeOn(Schedulers.io());
                    single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                            if(bookCollectionPageableList != null) {
                                mPage = bookCollectionPageableList.getPage() == null? 0: bookCollectionPageableList.getPage();
                                mNextPage = bookCollectionPageableList.getNextPage() == null? 0: bookCollectionPageableList.getNextPage();

                                setCurrentBookCollection(bookCollection, bookCollectionPageableList.getElements());
                            }
                            mRefreshView.setRefreshing(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            mOnErrorListener.onError(e);
                            mRefreshView.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                mOnErrorListener.onError(e);
                mRefreshView.setRefreshing(false);
            }
        });
    }

    public void setCurrentBookCollection(BookCollectionDto bookCollection, List<BookCollectionDto> bookCollectionList) {
        mBookCollectionId = bookCollection.getId();
        mBookCollection = bookCollection;
        mBookCollectionList = bookCollectionList;

        mGroupListView.getAdapter().notifyDataSetChanged();

        setCurrentBookCollection();
    }

    public void setCurrentBookCollection() {
        FragmentActivity fragmentActivity = getActivity();

        if(fragmentActivity != null) {
            if(mFilterSearch.equals("")) {
                fragmentActivity.setTitle(mBookCollection.getName());
            } else {
                fragmentActivity.setTitle(mBookCollection.getName() + ": " + mFilterSearch);
            }

            if(mBookCollectionList.size() != 0) {
                mNotEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mNotEmptyView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBookCollection == null) {
            setCurrentBookCollection(mBookCollectionId);
        } else {
            setCurrentBookCollection();
        }
    }

    @Override
    public void onRefresh() {
        setCurrentBookCollection(mBookCollectionId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = getActivity();
        fragmentActivity.setTitle("");

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_library, container, false);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mGroupListView = (RecyclerView) view.findViewById(R.id.fragmentLibraryLayout);
        mGroupListView.setHasFixedSize(true);
        mGroupListView.setLayoutManager(layoutManager);
        mGroupListView.setAdapter(new GroupGridAdapter());
        mGroupListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));
        mGroupListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!mRefreshView.isRefreshing() && (mPage < mNextPage)) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= mPageSize) {
                        loadNextPage();
                    }
                }
            }
        });

        mRefreshView = view.findViewById(R.id.library_refresh);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.library_not_empty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.library_empty);
        mEmptyView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.library, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQuery(mFilterSearch, false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(mFilterSearch.equals(s) == false) {
            mFilterSearch = s;
            setCurrentBookCollection(mBookCollectionId);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    private void loadNextPage() {
        mRefreshView.setRefreshing(true);

        Single<PageableListDto<BookCollectionDto>> single = new Single<PageableListDto<BookCollectionDto>>() {
            @Override
            protected void subscribeActual(SingleObserver<? super PageableListDto<BookCollectionDto>> observer) {
                try {
                    String name = null;
                    if (mFilterSearch.length() > 0) {
                        name = mFilterSearch;
                    }

                    PageableListDto<BookCollectionDto> bookCollectionPageableList = mApplicationService.getBookCollections(mBookCollection.getId(), name, mNextPage, mPageSize, "(bookCollections,books)").blockingGet();

                    observer.onSuccess(bookCollectionPageableList);
                } catch(Exception e) {
                    observer.onError(e);
                }
            }
        };
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<PageableListDto<BookCollectionDto>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
                if(mBookCollectionList != null) {
                    mPage = bookCollectionPageableList.getPage() == null? 0: bookCollectionPageableList.getPage();
                    mNextPage = bookCollectionPageableList.getNextPage() == null? 0: bookCollectionPageableList.getNextPage();

                    mBookCollectionList.addAll(bookCollectionPageableList.getElements());

                    mGroupListView.getAdapter().notifyDataSetChanged();
                }
                mRefreshView.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mOnErrorListener.onError(e);
                mRefreshView.setRefreshing(false);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_CURRENT_BOOK_COLLECTION_ID, mBookCollectionId);
        super.onSaveInstanceState(outState);
    }

    private int calculateNumColumns() {
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_group_column_width);

        return Math.round((float) deviceWidth / columnWidth);
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        };
    }

    private final class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }

    private final class GroupGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return mBookCollectionList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return ITEM_VIEW_TYPE_GROUP;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_group, viewGroup, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_GROUP) {
                BookCollectionDto bookCollection = mBookCollectionList.get(i);
                GroupViewHolder holder = (GroupViewHolder) viewHolder;
                holder.setupGroup(bookCollection);
            }
        }
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView groupImageView;
        private TextView bookCollectionTextView;
        private TextView bookCollectionNumberOfBooksTextView;
        private ImageView bookMarkUpdateImageView;
        private ImageView bookDownloadImageView;

        public GroupViewHolder(View itemView) {
            super(itemView);
            if(mBookCollectionList.size() > 0) {
                groupImageView = (ImageView) itemView.findViewById(R.id.card_group_imageview);
                groupImageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto bookCollection = mBookCollectionList.get(i);

                        LibraryBrowserFragment fragment = LibraryBrowserFragment.create(bookCollection.getId());
                        ((MainActivity) getActivity()).pushFragment(fragment);
                    }
                });

                bookCollectionTextView = (TextView) itemView.findViewById(R.id.comic_group_folder_bookCollectionTextView);

                bookCollectionNumberOfBooksTextView = (TextView) itemView.findViewById(R.id.comic_group_folder_bookCollectionNumberOfBooksTextView);

                bookMarkUpdateImageView = (ImageView) itemView.findViewById(R.id.comic_group_folder_bookMarkUpdateImageView);

                bookMarkUpdateImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollection = mBookCollectionList.get(i);

                        String message = selectedBookCollection.getName();

                        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to update the bookmarks to the last page?")
                                .setMessage(message)
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Completable completable = new Completable() {
                                            @Override
                                            protected void subscribeActual(CompletableObserver observer) {
                                                try {
                                                    mApplicationService.createOrUpdateBookMarks(selectedBookCollection.getId()).blockingGet();

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

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                mOnErrorListener.onError(e);
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Completable completable = new Completable() {
                                            @Override
                                            protected void subscribeActual(CompletableObserver observer) {
                                                try {
                                                    mApplicationService.deleteBookMarks(selectedBookCollection.getId()).blockingGet();

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

                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                mOnErrorListener.onError(e);
                                            }
                                        });
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                });

                bookDownloadImageView = (ImageView) itemView.findViewById(R.id.comic_group_folder_bookDownloadImageView);

                bookDownloadImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollection = mBookCollectionList.get(i);

                        String message = selectedBookCollection.getName();

                        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to download the books?")
                                .setMessage(message)
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Completable completable = new Completable() {
                                            @Override
                                            protected void subscribeActual(CompletableObserver observer) {
                                                try {
                                                    mAuthenticationManager.refresh().blockingAwait();

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
                                                Completable completable = new Completable() {
                                                    @Override
                                                    protected void subscribeActual(CompletableObserver observer) {
                                                        try {
                                                            DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);

                                                            Integer page = 1;
                                                            Integer pageSize = 100;

                                                            do {
                                                                PageableListDto<BookDto> selectedBookPageableList = mApplicationService.getBooks(selectedBookCollection.getId(), page, pageSize, "(bookMark)").blockingGet();
                                                                for(BookDto selectedBook: selectedBookPageableList.getElements()) {
                                                                    String path = "Oboco" + File.separator + selectedBookCollection.getName() + File.separator  + selectedBook.getName() +  ".cbz";

                                                                    String url = mBaseUrl + "/api/v1/books/" + selectedBook.getId() + ".cbz";

                                                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                                                                            .addRequestHeader("Authorization", "Bearer " + mAuthenticationManager.getAccessToken())
                                                                            .setTitle(selectedBook.getName() + ".cbz")
                                                                            .setDescription(selectedBook.getName() + ".cbz")
                                                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                                                                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);

                                                                    downloadManager.enqueue(request);
                                                                }

                                                                page = selectedBookPageableList.getNextPage();
                                                            } while(page != null);

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

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        mOnErrorListener.onError(e);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }
                                        });
                                    }
                                })
                                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                });
            }

            itemView.setOnClickListener(this);
        }

        public void setupGroup(BookCollectionDto bookCollection) {
            bookCollectionTextView.setText(bookCollection.getName());

            if(bookCollection.getNumberOfBooks() != 0) {
                groupImageView.setVisibility(View.VISIBLE);
                bookCollectionNumberOfBooksTextView.setVisibility(View.VISIBLE);
                bookMarkUpdateImageView.setVisibility(View.VISIBLE);
                bookDownloadImageView.setVisibility(View.VISIBLE);

                groupImageView.setImageResource(android.R.color.transparent);

                String url = mBaseUrl + "/api/v1/bookCollections/" + bookCollection.getId() + "/books/FIRST/pages/1.jpg?scaleType=DEFAULT&scaleWidth=" + Constants.COVER_THUMBNAIL_HEIGHT + "&scaleHeight=" + Constants.COVER_THUMBNAIL_WIDTH;
                mPicasso.load(url)
                        .tag(getActivity())
                        .into(groupImageView);

                bookCollectionNumberOfBooksTextView.setText(bookCollection.getNumberOfBooks().toString());
            } else {
                groupImageView.setVisibility(View.GONE);
                bookCollectionNumberOfBooksTextView.setVisibility(View.GONE);
                bookMarkUpdateImageView.setVisibility(View.GONE);
                bookDownloadImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int i = getAdapterPosition();
            BookCollectionDto bookCollection = mBookCollectionList.get(i);

            LibraryFragment fragment = LibraryFragment.create(bookCollection.getId());
            ((MainActivity)getActivity()).pushFragment(fragment);
        }
    }
}
