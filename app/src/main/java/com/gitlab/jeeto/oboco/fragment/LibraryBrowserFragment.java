package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.gitlab.jeeto.oboco.activity.ReaderActivity;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.BookBrowserManager;
import com.gitlab.jeeto.oboco.manager.BookBrowserRequestHandler;
import com.gitlab.jeeto.oboco.manager.DownloadBookWorker;
import com.gitlab.jeeto.oboco.manager.RemoteBookBrowserManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class LibraryBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String PARAM_BOOK_COLLECTION_ID = "PARAM_BOOK_COLLECTION_ID";

    final int ITEM_VIEW_TYPE_COMIC = 1;

    private int mFilterRead = R.id.menu_browser_filter_all;

    private RecyclerView mComicListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;

    private Picasso mPicasso;

    private BookCollectionDto mBookCollection;
    private List<BookDto> mBookList;

    private int mPage = 0;
    private int mPageSize = 100;
    private int mNextPage = 0;

    private BookBrowserManager mBookBrowserManager;

    private OnErrorListener mOnErrorListener;

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

    public static LibraryBrowserFragment create(Long bookCollectionId) {
        LibraryBrowserFragment fragment = new LibraryBrowserFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookCollection = null;
        mBookList = new ArrayList<BookDto>();

        Long bookCollectionId = getArguments().getLong(PARAM_BOOK_COLLECTION_ID);

        mBookBrowserManager = new RemoteBookBrowserManager(bookCollectionId);
        mBookBrowserManager.create(this);

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(new BookBrowserRequestHandler(mBookBrowserManager))
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

    @Override
    public void onDestroy() {
        mBookBrowserManager.destroy();

        super.onDestroy();
    }

    public void onLoad(BookCollectionDto bookCollection, PageableListDto<BookDto> bookPageableList) {
        mBookCollection = bookCollection;

        mPage = bookPageableList.getPage() == null? 0: bookPageableList.getPage();
        mNextPage = bookPageableList.getNextPage() == null? 0: bookPageableList.getNextPage();

        mBookList = bookPageableList.getElements();

        mComicListView.getAdapter().notifyDataSetChanged();

        onLoad();
    }

    public void onLoad() {
        FragmentActivity fragmentActivity = getActivity();

        if(fragmentActivity != null) {
            fragmentActivity.setTitle(mBookCollection.getName());

            if(mBookList.size() != 0) {
                mNotEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mNotEmptyView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onLoadBookPageableList(PageableListDto<BookDto> bookPageableList) {
        if(mBookList != null) {
            mPage = bookPageableList.getPage() == null? 0: bookPageableList.getPage();
            mNextPage = bookPageableList.getNextPage() == null? 0: bookPageableList.getNextPage();

            mBookList.addAll(bookPageableList.getElements());

            mComicListView.getAdapter().notifyDataSetChanged();
        }
    }

    public void onAddBookMark(BookDto book, BookMarkDto bookMark) {
        book.setBookMark(bookMark);

        mComicListView.getAdapter().notifyDataSetChanged();
    }

    public void onRemoveBookMark(BookDto book) {
        book.setBookMark(null);

        mComicListView.getAdapter().notifyDataSetChanged();
    }

    private String getBookMarkStatus() {
        String bookMarkStatus = null;
        if (mFilterRead != R.id.menu_browser_filter_all) {
            if (mFilterRead == R.id.menu_browser_filter_read) {
                bookMarkStatus = "READ";
            }
            if (mFilterRead == R.id.menu_browser_filter_unread) {
                bookMarkStatus = "UNREAD";
            }
            if (mFilterRead == R.id.menu_browser_filter_reading) {
                bookMarkStatus = "READING";
            }
        }
        return bookMarkStatus;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = getActivity();
        fragmentActivity.setTitle("");

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_librarybrowser, container, false);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mComicListView = (RecyclerView) view.findViewById(R.id.library_grid);
        mComicListView.setHasFixedSize(true);
        mComicListView.setLayoutManager(layoutManager);
        mComicListView.setAdapter(new ComicGridAdapter());
        mComicListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));
        mComicListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        String bookMarkStatus = getBookMarkStatus();

                        mBookBrowserManager.loadBookPageableList(bookMarkStatus, mNextPage, mPageSize);
                    }
                }
            }
        });

        mRefreshView = view.findViewById(R.id.librarybrowser_refresh);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.librarybrowser_not_empty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.librarybrowser_empty);
        mEmptyView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBookCollection == null) {
            String bookMarkStatus = getBookMarkStatus();

            mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
        } else {
            onLoad();
        }
    }

    @Override
    public void onRefresh() {
        String bookMarkStatus = getBookMarkStatus();

        mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.librarybrowser, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
            case R.id.menu_browser_filter_read:
            case R.id.menu_browser_filter_unread:
            case R.id.menu_browser_filter_reading:
                item.setChecked(true);
                mFilterRead = item.getItemId();

                String bookMarkStatus = getBookMarkStatus();

                mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openComic(BookDto book) {
        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_MODE, ReaderFragment.Mode.MODE_REMOTE);
        intent.putExtra(ReaderFragment.PARAM_BOOK_ID, book.getId());
        startActivity(intent);
    }

    private BookDto getBookAtPosition(int position) {
        BookDto book = mBookList.get(position);
        return book;
    }

    private int getItemViewTypeAtPosition(int position) {
        return ITEM_VIEW_TYPE_COMIC;
    }

    private int calculateNumColumns() {
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_comic_column_width);

        return Math.round((float) deviceWidth / columnWidth);
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        final int numColumns = calculateNumColumns();

        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC)
                    return 1;
                return numColumns;
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


    private final class ComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return mBookList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_comic, viewGroup, false);
            return new ComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_COMIC) {
                BookDto book = getBookAtPosition(i);
                ComicViewHolder holder = (ComicViewHolder) viewHolder;
                holder.setupComic(book);
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }

        public void setTitle(int titleRes) {
            ((TextView) itemView).setText(titleRes);
        }
    }

    private class ComicViewHolder extends RecyclerView.ViewHolder {
        private ImageView mBookImageView;
        private TextView mBookTextView;
        private TextView mBookMarkTextView;
        private ImageView mBookMarkUpdateImageView;
        private ImageView mBookDownloadImageView;

        public ComicViewHolder(View itemView) {
            super(itemView);
            mBookImageView = (ImageView) itemView.findViewById(R.id.comicBookImageView);
            mBookImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto book = getBookAtPosition(i);
                    openComic(book);
                }
            });

            mBookTextView = (TextView) itemView.findViewById(R.id.comicBookTextView);
            mBookMarkTextView = (TextView) itemView.findViewById(R.id.comicBookMarkTextView);
            mBookMarkUpdateImageView = (ImageView) itemView.findViewById(R.id.comicBookMarkUpdateImageView);

            mBookMarkUpdateImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto selectedBook = getBookAtPosition(i);

                    String message = selectedBook.getName();

                    AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setTitle("Would you like to update the bookmark to the last page?")
                            .setMessage(message)
                            .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBookBrowserManager.addBookMark(selectedBook);
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
                                    mBookBrowserManager.removeBookMark(selectedBook);
                                }
                            })
                            .create();
                    dialog.show();
                }
            });

            mBookDownloadImageView = (ImageView) itemView.findViewById(R.id.comicBookDownloadImageView);

            mBookDownloadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto selectedBook = getBookAtPosition(i);

                    String message = selectedBook.getName();

                    AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setTitle("Would you like to download the book?")
                            .setMessage(message)
                            .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Constraints constraints = new Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .setRequiresStorageNotLow(true)
                                            .build();

                                    WorkRequest downloadWorkRequest =
                                            new OneTimeWorkRequest.Builder(DownloadBookWorker.class)
                                                    .setConstraints(constraints)
                                                    .addTag("download")
                                                    .setInputData(
                                                            new Data.Builder()
                                                                    .putLong("bookId",  selectedBook.getId())
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

        public void setupComic(BookDto book) {
            mBookTextView.setText(book.getName());

            if(book.getBookMark() == null) {
                mBookMarkTextView.setText("0/" + book.getNumberOfPages());
            } else {
                mBookMarkTextView.setText(book.getBookMark().getPage() + "/" + book.getNumberOfPages());
            }

            mBookImageView.setImageResource(android.R.color.transparent);

            Uri uri = BookBrowserRequestHandler.getBookPage(book, "DEFAULT", Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            mPicasso.load(uri)
                    .into(mBookImageView);
        }
    }
}
