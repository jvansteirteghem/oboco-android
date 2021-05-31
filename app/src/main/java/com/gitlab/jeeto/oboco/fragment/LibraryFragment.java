package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.gitlab.jeeto.oboco.Constants;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.MainActivity;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.BookCollectionBrowserManager;
import com.gitlab.jeeto.oboco.manager.BookCollectionBrowserRequestHandler;
import com.gitlab.jeeto.oboco.manager.DownloadBookCollectionWorker;
import com.gitlab.jeeto.oboco.manager.RemoteBookCollectionBrowserManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

    private OnErrorListener mOnErrorListener;

    private int mPage = 0;
    private int mPageSize = 100;
    private int mNextPage = 0;

    private BookCollectionBrowserManager mBookCollectionBrowserManager;

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

    public void onError(Throwable e) {
        mOnErrorListener.onError(e);
    }

    public void setRefreshing(boolean refreshing) {
        mRefreshView.setRefreshing(refreshing);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mBookCollectionBrowserManager.destroy();

        super.onDestroy();
    }

    public void onAddBookMark(BookCollectionDto bookCollection) {

    }

    public void onRemoveBookMark(BookCollectionDto bookCollection) {

    }

    private String getBookCollectionName() {
        String name = null;
        if (mFilterSearch.length() > 0) {
            name = mFilterSearch;
        }
        return name;
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
        } else {
            if(getArguments() != null) {
                mBookCollectionId = getArguments().getLong(PARAM_BOOK_COLLECTION_ID);
            } else {
                mBookCollectionId = null;
            }
        }

        mBookCollectionList = new ArrayList<BookCollectionDto>();

        mBookCollectionBrowserManager = new RemoteBookCollectionBrowserManager(mBookCollectionId);
        mBookCollectionBrowserManager.create(this);

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(new BookCollectionBrowserRequestHandler(mBookCollectionBrowserManager))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        onError(exception);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();
    }

    public void onLoad(BookCollectionDto bookCollection, PageableListDto<BookCollectionDto> bookCollectionPageableList) {
        mBookCollectionId = bookCollection.getId();
        mBookCollection = bookCollection;

        mPage = bookCollectionPageableList.getPage() == null? 0: bookCollectionPageableList.getPage();
        mNextPage = bookCollectionPageableList.getNextPage() == null? 0: bookCollectionPageableList.getNextPage();

        mBookCollectionList = bookCollectionPageableList.getElements();

        mGroupListView.getAdapter().notifyDataSetChanged();

        onLoad();
    }

    public void onLoad() {
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

    public void onLoadBookCollectionPageableList(PageableListDto<BookCollectionDto> bookCollectionPageableList) {
        mPage = bookCollectionPageableList.getPage() == null? 0: bookCollectionPageableList.getPage();
        mNextPage = bookCollectionPageableList.getNextPage() == null? 0: bookCollectionPageableList.getNextPage();

        mBookCollectionList.addAll(bookCollectionPageableList.getElements());

        mGroupListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBookCollection == null) {
            String bookCollectionName = getBookCollectionName();

            mBookCollectionBrowserManager.load(bookCollectionName, 1, mPageSize);
        } else {
            onLoad();
        }
    }

    @Override
    public void onRefresh() {
        String bookCollectionName = getBookCollectionName();

        mBookCollectionBrowserManager.load(bookCollectionName, 1, mPageSize);
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
                        String bookCollectionName = getBookCollectionName();

                        mBookCollectionBrowserManager.loadBookCollectionPageableList(bookCollectionName, mNextPage, mPageSize);
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

            String bookCollectionName = getBookCollectionName();

            mBookCollectionBrowserManager.load(bookCollectionName, 1, mPageSize);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
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
                                        mBookCollectionBrowserManager.addBookMark(selectedBookCollection);
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
                                        mBookCollectionBrowserManager.removeBookMark(selectedBookCollection);
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
                                        Constraints constraints = new Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .setRequiresStorageNotLow(true)
                                                .build();

                                        WorkRequest downloadWorkRequest =
                                                new OneTimeWorkRequest.Builder(DownloadBookCollectionWorker.class)
                                                        .setConstraints(constraints)
                                                        .addTag("download")
                                                        .setInputData(
                                                                new Data.Builder()
                                                                        .putLong("bookCollectionId",  selectedBookCollection.getId())
                                                                        .build()
                                                        )
                                                        .build();

                                        WorkManager
                                                .getInstance(getContext())
                                                .enqueue(downloadWorkRequest);
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

                Uri uri = BookCollectionBrowserRequestHandler.getBookCollectionPage(bookCollection, "DEFAULT", Constants.COVER_THUMBNAIL_HEIGHT, Constants.COVER_THUMBNAIL_WIDTH);
                mPicasso.load(uri)
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
