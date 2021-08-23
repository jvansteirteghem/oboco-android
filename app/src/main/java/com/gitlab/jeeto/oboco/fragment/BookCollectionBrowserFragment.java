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
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.OnErrorListener;
import com.gitlab.jeeto.oboco.client.PageableListDto;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.BookCollectionBrowserManager;
import com.gitlab.jeeto.oboco.manager.DownloadBookCollectionWorker;
import com.gitlab.jeeto.oboco.manager.DownloadWorkType;
import com.gitlab.jeeto.oboco.manager.RemoteBookCollectionBrowserManager;
import com.gitlab.jeeto.oboco.manager.RemoteLatestBookCollectionBrowserManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookCollectionBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    final int ITEM_VIEW_TYPE_BOOK_COLLECTION = 1;

    private Picasso mPicasso;
    private RecyclerView mBookCollectionListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;

    private String mFilterSearch = "";

    private BookCollectionDto mBookCollectionDto;
    private List<BookCollectionDto> mBookCollectionListDto;

    private OnErrorListener mOnErrorListener;

    private int mPage = 0;
    private int mPageSize = 100;
    private int mNextPage = 0;

    private BookCollectionBrowserManager mBookCollectionBrowserManager;
    private BookCollectionBrowserManager.Mode mMode;

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
        mRefreshView.setRefreshing(false);

        if(mOnErrorListener != null) {
            mOnErrorListener.onError(e);
        }
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

    public void onAddBookMark(BookCollectionDto bookCollectionDto) {

    }

    public void onRemoveBookMark(BookCollectionDto bookCollectionDto) {

    }

    private String getBookCollectionName() {
        String name = null;
        if (mFilterSearch.length() > 0) {
            name = mFilterSearch;
        }
        return name;
    }

    public static BookCollectionBrowserFragment create(Long bookCollectionId) {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserManager.PARAM_MODE, BookCollectionBrowserManager.Mode.MODE_REMOTE);
        args.putLong(RemoteBookCollectionBrowserManager.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment create() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserManager.PARAM_MODE, BookCollectionBrowserManager.Mode.MODE_REMOTE_LATEST);
        fragment.setArguments(args);
        return fragment;
    }

    public BookCollectionBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookCollectionListDto = new ArrayList<BookCollectionDto>();

        Bundle bundle = getArguments();
        mMode = (BookCollectionBrowserManager.Mode) bundle.getSerializable(BookCollectionBrowserManager.PARAM_MODE);

        if(mMode == BookCollectionBrowserManager.Mode.MODE_REMOTE) {
            mBookCollectionBrowserManager = new RemoteBookCollectionBrowserManager(this);
        } else if(mMode == BookCollectionBrowserManager.Mode.MODE_REMOTE_LATEST) {
            mBookCollectionBrowserManager = new RemoteLatestBookCollectionBrowserManager(this);
        }
        mBookCollectionBrowserManager.create(savedInstanceState);

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mBookCollectionBrowserManager)
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

    public void onLoad(BookCollectionDto bookCollectionDto, PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
        mRefreshView.setRefreshing(false);

        mBookCollectionDto = bookCollectionDto;

        mPage = bookCollectionPageableListDto.getPage() == null? 0: bookCollectionPageableListDto.getPage();
        mNextPage = bookCollectionPageableListDto.getNextPage() == null? 0: bookCollectionPageableListDto.getNextPage();

        mBookCollectionListDto = bookCollectionPageableListDto.getElements();

        mBookCollectionListView.getAdapter().notifyDataSetChanged();

        onLoad();
    }

    public void onLoad() {
        FragmentActivity fragmentActivity = getActivity();

        if(fragmentActivity != null) {
            if(mFilterSearch.equals("")) {
                fragmentActivity.setTitle(mBookCollectionDto.getName());
            } else {
                fragmentActivity.setTitle(mBookCollectionDto.getName() + ": " + mFilterSearch);
            }

            if(mBookCollectionListDto.size() != 0) {
                mNotEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mNotEmptyView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onLoadBookCollectionPageableList(PageableListDto<BookCollectionDto> bookCollectionPageableListDto) {
        mRefreshView.setRefreshing(false);

        mPage = bookCollectionPageableListDto.getPage() == null? 0: bookCollectionPageableListDto.getPage();
        mNextPage = bookCollectionPageableListDto.getNextPage() == null? 0: bookCollectionPageableListDto.getNextPage();

        mBookCollectionListDto.addAll(bookCollectionPageableListDto.getElements());

        mBookCollectionListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBookCollectionDto == null) {
            mRefreshView.setRefreshing(true);

            String bookCollectionName = getBookCollectionName();

            mBookCollectionBrowserManager.load(bookCollectionName, 1, mPageSize);
        } else {
            onLoad();
        }
    }

    @Override
    public void onRefresh() {
        mRefreshView.setRefreshing(true);

        String bookCollectionName = getBookCollectionName();

        mBookCollectionBrowserManager.load(bookCollectionName, 1, mPageSize);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = getActivity();
        fragmentActivity.setTitle("");

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_book_collection_browser, container, false);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mBookCollectionListView = (RecyclerView) view.findViewById(R.id.bookCollectionBrowserGrid);
        mBookCollectionListView.setHasFixedSize(true);
        mBookCollectionListView.setLayoutManager(layoutManager);
        mBookCollectionListView.setAdapter(new BookCollectionGridAdapter());
        mBookCollectionListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));
        mBookCollectionListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        mRefreshView.setRefreshing(true);

                        String bookCollectionName = getBookCollectionName();

                        mBookCollectionBrowserManager.loadBookCollectionPageableList(bookCollectionName, mNextPage, mPageSize);
                    }
                }
            }
        });

        mRefreshView = view.findViewById(R.id.bookCollectionBrowserRefresh);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.bookCollectionBrowserNotEmpty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.bookCollectionBrowserEmpty);
        mEmptyView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.book_collection_browser, menu);

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

            mRefreshView.setRefreshing(true);

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
        mBookCollectionBrowserManager.saveInstanceState(outState);

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

    private final class BookCollectionGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return mBookCollectionListDto.size();
        }

        @Override
        public int getItemViewType(int position) {
            return ITEM_VIEW_TYPE_BOOK_COLLECTION;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_book_collection, viewGroup, false);
            return new BookCollectionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_BOOK_COLLECTION) {
                BookCollectionDto bookCollectionDto = mBookCollectionListDto.get(i);
                BookCollectionViewHolder holder = (BookCollectionViewHolder) viewHolder;
                holder.setupBookCollection(bookCollectionDto);
            }
        }
    }

    private class BookCollectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mBookCollectionImageView;
        private TextView mBookCollectionTextView;
        private TextView mBookCollectionNumberOfBooksTextView;
        private ImageView mBookCollectionBookMarkImageView;
        private ImageView mBookCollectionDownloadImageView;

        public BookCollectionViewHolder(View itemView) {
            super(itemView);
            if(mBookCollectionListDto.size() > 0) {
                mBookCollectionImageView = (ImageView) itemView.findViewById(R.id.bookCollectionImageView);
                mBookCollectionImageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto bookCollectionDto = mBookCollectionListDto.get(i);

                        BookBrowserFragment fragment = BookBrowserFragment.create(bookCollectionDto.getId());
                        ((MainActivity) getActivity()).pushFragment(fragment);
                    }
                });

                mBookCollectionTextView = (TextView) itemView.findViewById(R.id.bookCollectionTextView);

                mBookCollectionNumberOfBooksTextView = (TextView) itemView.findViewById(R.id.bookCollectionNumberOfBooksTextView);

                mBookCollectionBookMarkImageView = (ImageView) itemView.findViewById(R.id.bookCollectionBookMarkImageView);

                mBookCollectionBookMarkImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollectionDto = mBookCollectionListDto.get(i);

                        String message = selectedBookCollectionDto.getName();

                        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to update the bookmarks to the last page?")
                                .setMessage(message)
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mBookCollectionBrowserManager.addBookMark(selectedBookCollectionDto);
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
                                        mBookCollectionBrowserManager.removeBookMark(selectedBookCollectionDto);
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                });

                mBookCollectionDownloadImageView = (ImageView) itemView.findViewById(R.id.bookCollectionDownloadImageView);

                mBookCollectionDownloadImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollectionDto = mBookCollectionListDto.get(i);

                        String message = selectedBookCollectionDto.getName();

                        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to download the book collection?")
                                .setMessage(message)
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WorkRequest downloadWorkRequest = DownloadBookCollectionWorker.createDownloadWorkRequest(selectedBookCollectionDto.getId(), selectedBookCollectionDto.getName());

                                        WorkManager
                                                .getInstance(getContext().getApplicationContext())
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

        public void setupBookCollection(BookCollectionDto bookCollectionDto) {
            mBookCollectionTextView.setText(bookCollectionDto.getName());

            if(bookCollectionDto.getNumberOfBooks() != 0) {
                mBookCollectionImageView.setVisibility(View.VISIBLE);
                mBookCollectionNumberOfBooksTextView.setVisibility(View.VISIBLE);
                mBookCollectionBookMarkImageView.setVisibility(View.VISIBLE);
                mBookCollectionDownloadImageView.setVisibility(View.VISIBLE);

                mBookCollectionImageView.setImageResource(android.R.color.transparent);

                Uri uri = mBookCollectionBrowserManager.getBookCollectionPageUri(bookCollectionDto, "DEFAULT", Constants.COVER_THUMBNAIL_HEIGHT, Constants.COVER_THUMBNAIL_WIDTH);
                mPicasso.load(uri)
                        .tag(getActivity())
                        .into(mBookCollectionImageView);

                mBookCollectionNumberOfBooksTextView.setText(bookCollectionDto.getNumberOfBooks().toString());
            } else {
                mBookCollectionImageView.setVisibility(View.GONE);
                mBookCollectionNumberOfBooksTextView.setVisibility(View.GONE);
                mBookCollectionBookMarkImageView.setVisibility(View.GONE);
                mBookCollectionDownloadImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int i = getAdapterPosition();
            BookCollectionDto bookCollectionDto = mBookCollectionListDto.get(i);

            BookCollectionBrowserFragment fragment = BookCollectionBrowserFragment.create(bookCollectionDto.getId());
            ((MainActivity)getActivity()).pushFragment(fragment);
        }
    }
}
