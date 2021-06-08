package com.gitlab.jeeto.oboco.fragment;

import android.app.Activity;
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
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.api.PageableListDto;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.BookBrowserManager;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.DownloadBookWorker;
import com.gitlab.jeeto.oboco.manager.RemoteBookBrowserManager;
import com.gitlab.jeeto.oboco.manager.RemoteBookReaderManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class BookBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final int ITEM_VIEW_TYPE_BOOK = 1;

    private int mFilterRead = R.id.menu_book_browser_filter_all;

    private RecyclerView mBookListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;

    private Picasso mPicasso;

    private BookCollectionDto mBookCollectionDto;
    private List<BookDto> mBookListDto;

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
        mRefreshView.setRefreshing(false);

        mOnErrorListener.onError(e);
    }

    public static BookBrowserFragment create(Long bookCollectionId) {
        BookBrowserFragment fragment = new BookBrowserFragment();
        Bundle args = new Bundle();
        args.putLong(RemoteBookBrowserManager.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public BookBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookCollectionDto = null;
        mBookListDto = new ArrayList<BookDto>();

        mBookBrowserManager = new RemoteBookBrowserManager(this);
        mBookBrowserManager.create(savedInstanceState);

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mBookBrowserManager)
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mBookBrowserManager.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    public void onLoad(BookCollectionDto bookCollectionDto, PageableListDto<BookDto> bookPageableListDto) {
        mRefreshView.setRefreshing(false);

        mBookCollectionDto = bookCollectionDto;

        mPage = bookPageableListDto.getPage() == null? 0: bookPageableListDto.getPage();
        mNextPage = bookPageableListDto.getNextPage() == null? 0: bookPageableListDto.getNextPage();

        mBookListDto = bookPageableListDto.getElements();

        mBookListView.getAdapter().notifyDataSetChanged();

        onLoad();
    }

    public void onLoad() {
        FragmentActivity fragmentActivity = getActivity();

        if(fragmentActivity != null) {
            fragmentActivity.setTitle(mBookCollectionDto.getName());

            if(mBookListDto.size() != 0) {
                mNotEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mNotEmptyView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onLoadBookPageableList(PageableListDto<BookDto> bookPageableListDto) {
        mRefreshView.setRefreshing(false);

        if(mBookListDto != null) {
            mPage = bookPageableListDto.getPage() == null? 0: bookPageableListDto.getPage();
            mNextPage = bookPageableListDto.getNextPage() == null? 0: bookPageableListDto.getNextPage();

            mBookListDto.addAll(bookPageableListDto.getElements());

            mBookListView.getAdapter().notifyDataSetChanged();
        }
    }

    public void onAddBookMark(BookDto bookDto, BookMarkDto bookMarkDto) {
        bookDto.setBookMark(bookMarkDto);

        mBookListView.getAdapter().notifyDataSetChanged();
    }

    public void onRemoveBookMark(BookDto bookDto) {
        bookDto.setBookMark(null);

        mBookListView.getAdapter().notifyDataSetChanged();
    }

    private String getBookMarkStatus() {
        String bookMarkStatus = null;
        if (mFilterRead != R.id.menu_book_browser_filter_all) {
            if (mFilterRead == R.id.menu_book_browser_filter_read) {
                bookMarkStatus = "READ";
            }
            if (mFilterRead == R.id.menu_book_browser_filter_unread) {
                bookMarkStatus = "UNREAD";
            }
            if (mFilterRead == R.id.menu_book_browser_filter_reading) {
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

        final View view = inflater.inflate(R.layout.fragment_book_browser, container, false);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mBookListView = (RecyclerView) view.findViewById(R.id.bookBrowserGrid);
        mBookListView.setHasFixedSize(true);
        mBookListView.setLayoutManager(layoutManager);
        mBookListView.setAdapter(new BookGridAdapter());
        mBookListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));
        mBookListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                        String bookMarkStatus = getBookMarkStatus();

                        mBookBrowserManager.loadBookPageableList(bookMarkStatus, mNextPage, mPageSize);
                    }
                }
            }
        });

        mRefreshView = view.findViewById(R.id.bookBrowserRefresh);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.bookBrowserNotEmpty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.bookBrowserEmpty);
        mEmptyView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBookCollectionDto == null) {
            mRefreshView.setRefreshing(true);

            String bookMarkStatus = getBookMarkStatus();

            mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
        } else {
            onLoad();
        }
    }

    @Override
    public void onRefresh() {
        mRefreshView.setRefreshing(true);

        String bookMarkStatus = getBookMarkStatus();

        mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.book_browser, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_book_browser_filter_all:
            case R.id.menu_book_browser_filter_read:
            case R.id.menu_book_browser_filter_unread:
            case R.id.menu_book_browser_filter_reading:
                item.setChecked(true);
                mFilterRead = item.getItemId();

                mRefreshView.setRefreshing(true);

                String bookMarkStatus = getBookMarkStatus();

                mBookBrowserManager.load(bookMarkStatus, 1, mPageSize);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            List<BookDto> updatedBookListDto = (List<BookDto>) data.getSerializableExtra("updatedBookList");

            if(updatedBookListDto.size() != 0) {
                int index = 0;

                while (index < mBookListDto.size()) {
                    BookDto bookDto = mBookListDto.get(index);

                    for (BookDto updatedBookDto : updatedBookListDto) {
                        if (bookDto.equals(updatedBookDto)) {
                            mBookListDto.set(index, updatedBookDto);
                        }
                    }

                    index = index + 1;
                }

                mBookListView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    public void openBook(BookDto bookDto) {
        Intent intent = new Intent(getActivity(), BookReaderActivity.class);
        intent.putExtra(BookReaderManager.PARAM_MODE, BookReaderManager.Mode.MODE_REMOTE);
        intent.putExtra(RemoteBookReaderManager.PARAM_BOOK_ID, bookDto.getId());
        startActivityForResult(intent, 1);
    }

    private BookDto getBookAtPosition(int position) {
        BookDto bookDto = mBookListDto.get(position);
        return bookDto;
    }

    private int getItemViewTypeAtPosition(int position) {
        return ITEM_VIEW_TYPE_BOOK;
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
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_BOOK)
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


    private final class BookGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return mBookListDto.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_book, viewGroup, false);
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_BOOK) {
                BookDto bookDto = getBookAtPosition(i);
                BookViewHolder holder = (BookViewHolder) viewHolder;
                holder.setupBook(bookDto);
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

    private class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView mBookImageView;
        private TextView mBookTextView;
        private TextView mBookBookMarkTextView;
        private ImageView mBookBookMarkImageView;
        private ImageView mBookDownloadImageView;

        public BookViewHolder(View itemView) {
            super(itemView);
            mBookImageView = (ImageView) itemView.findViewById(R.id.bookImageView);
            mBookImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto bookDto = getBookAtPosition(i);
                    openBook(bookDto);
                }
            });

            mBookTextView = (TextView) itemView.findViewById(R.id.bookTextView);
            mBookBookMarkTextView = (TextView) itemView.findViewById(R.id.bookBookMarkTextView);
            mBookBookMarkImageView = (ImageView) itemView.findViewById(R.id.bookBookMarkImageView);

            mBookBookMarkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto selectedBookDto = getBookAtPosition(i);

                    String message = selectedBookDto.getName();

                    AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setTitle("Would you like to update the bookmark to the last page?")
                            .setMessage(message)
                            .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBookBrowserManager.addBookMark(selectedBookDto);
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
                                    mBookBrowserManager.removeBookMark(selectedBookDto);
                                }
                            })
                            .create();
                    dialog.show();
                }
            });

            mBookDownloadImageView = (ImageView) itemView.findViewById(R.id.bookDownloadImageView);

            mBookDownloadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto selectedBookDto = getBookAtPosition(i);

                    String message = selectedBookDto.getName();

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
                                                                    .putLong("bookId",  selectedBookDto.getId())
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

        public void setupBook(BookDto bookDto) {
            mBookTextView.setText(bookDto.getName());

            if(bookDto.getBookMark() == null) {
                mBookBookMarkTextView.setText("0/" + bookDto.getNumberOfPages());
            } else {
                mBookBookMarkTextView.setText(bookDto.getBookMark().getPage() + "/" + bookDto.getNumberOfPages());
            }

            mBookImageView.setImageResource(android.R.color.transparent);

            Uri uri = mBookBrowserManager.getBookPageUri(bookDto, "DEFAULT", Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            mPicasso.load(uri)
                    .into(mBookImageView);
        }
    }
}
