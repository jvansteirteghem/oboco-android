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
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.gitlab.jeeto.oboco.Constants;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.DownloadBookWorker;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final int ITEM_VIEW_TYPE_BOOK = 1;

    private int mFilterRead = R.id.menu_book_browser_filter_all;

    private RecyclerView mBookListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;

    private Picasso mPicasso;

    private ActivityResultLauncher<Intent> mBookReaderActivityResultLauncher;

    private BookBrowserViewModel mViewModel;
    private BookBrowserRequestHandler mRequestHandler;

    private AlertDialog mMarkSelectedBookDialog;
    private AlertDialog mDownloadSelectedBookDialog;

    public static BookBrowserFragment create(Long bookCollectionId) {
        BookBrowserFragment fragment = new BookBrowserFragment();
        Bundle args = new Bundle();
        args.putLong(BookBrowserViewModel.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public BookBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteBookBrowserViewModel.class);

        mBookReaderActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK) {
                            List<BookDto> updatedBookListDto = (List<BookDto>) result.getData().getSerializableExtra("updatedBookList");

                            mViewModel.updateBookList(updatedBookListDto);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if(mMarkSelectedBookDialog != null) {
            mMarkSelectedBookDialog.dismiss();
            mMarkSelectedBookDialog = null;
        }

        if(mDownloadSelectedBookDialog != null) {
            mDownloadSelectedBookDialog.dismiss();
            mDownloadSelectedBookDialog = null;
        }

        super.onDestroyView();
    }

    private String getBookMarkStatus() {
        String bookMarkStatus = null;
        if (mFilterRead != R.id.menu_book_browser_filter_all) {
            if (mFilterRead == R.id.menu_book_browser_filter_read) {
                bookMarkStatus = "READ";
            } else if (mFilterRead == R.id.menu_book_browser_filter_unread) {
                bookMarkStatus = "UNREAD";
            } else if (mFilterRead == R.id.menu_book_browser_filter_reading) {
                bookMarkStatus = "READING";
            }
        }
        return bookMarkStatus;
    }

    private void setBookMarkStatus(String bookMarkStatus) {
        mFilterRead = R.id.menu_book_browser_filter_all;
        if(bookMarkStatus != null) {
            if (bookMarkStatus.equals("READ")) {
                mFilterRead = R.id.menu_book_browser_filter_read;
            } else if (bookMarkStatus.equals("UNREAD")) {
                mFilterRead = R.id.menu_book_browser_filter_unread;
            } else if (bookMarkStatus.equals("READING")) {
                mFilterRead = R.id.menu_book_browser_filter_reading;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle("");

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
                int itemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!mViewModel.getIsLoading() && mViewModel.hasNextBookList()) {
                    if (firstVisibleItemPosition >= 0 && (firstVisibleItemPosition + visibleItemCount) >= itemCount) {
                        mViewModel.loadNextBookList();
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

        mViewModel.getBookCollectionObservable().observe(getViewLifecycleOwner(), new Observer<BookCollectionDto>() {
            @Override
            public void onChanged(BookCollectionDto bookCollection) {
                getActivity().setTitle(bookCollection.getName());
            }
        });
        mViewModel.getBookListObservable().observe(getViewLifecycleOwner(), new Observer<List<BookDto>>() {
            @Override
            public void onChanged(List<BookDto> bookList) {
                if(bookList.size() != 0) {
                    mNotEmptyView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    mNotEmptyView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }

                mBookListView.getAdapter().notifyDataSetChanged();
            }
        });
        mViewModel.getBookMarkStatusObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String bookMarkStatus) {
                setBookMarkStatus(bookMarkStatus);
            }
        });
        mViewModel.getIsLoadingObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if(isLoading) {
                    mRefreshView.setRefreshing(true);
                } else {
                    mRefreshView.setRefreshing(false);
                }
            }
        });
        mViewModel.getShowMarkSelectedBookDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showMarkSelectedBookDialog) {
                if(showMarkSelectedBookDialog) {
                    if (mMarkSelectedBookDialog == null) {
                        mMarkSelectedBookDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to mark the book as READ?")
                                .setMessage(mViewModel.getSelectedBook().getName())
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.addBookMark();
                                        mViewModel.setShowMarkSelectedBookDialog(false);

                                        mMarkSelectedBookDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowMarkSelectedBookDialog(false);

                                        mMarkSelectedBookDialog = null;
                                    }
                                })
                                .setNeutralButton("UNREAD", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.removeBookMark();
                                        mViewModel.setShowMarkSelectedBookDialog(false);

                                        mMarkSelectedBookDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowMarkSelectedBookDialog(false);

                                        mMarkSelectedBookDialog = null;
                                    }
                                })
                                .create();
                        mMarkSelectedBookDialog.show();
                    }
                }
            }
        });
        mViewModel.getShowDownloadSelectedBookDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showDownloadSelectedBookDialog) {
                if(showDownloadSelectedBookDialog) {
                    if (mDownloadSelectedBookDialog == null) {
                        mDownloadSelectedBookDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to download the book?")
                                .setMessage(mViewModel.getSelectedBook().getName())
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WorkRequest downloadWorkRequest = DownloadBookWorker.createDownloadWorkRequest(mViewModel.getSelectedBook().getId(), mViewModel.getSelectedBook().getName());

                                        WorkManager
                                                .getInstance(getContext().getApplicationContext())
                                                .enqueue(downloadWorkRequest);

                                        mViewModel.setShowDownloadSelectedBookDialog(false);

                                        mDownloadSelectedBookDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDownloadSelectedBookDialog(false);

                                        mDownloadSelectedBookDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDownloadSelectedBookDialog(false);

                                        mDownloadSelectedBookDialog = null;
                                    }
                                })
                                .create();
                        mDownloadSelectedBookDialog.show();
                    }
                }
            }
        });
        mViewModel.getShowMessageObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showMessage) {
                if(showMessage) {
                    mViewModel.setShowMessage(false);

                    Toast toast = Toast.makeText(getContext(), mViewModel.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        mRequestHandler = mViewModel.getRequestHandler();

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mRequestHandler)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        mViewModel.setMessage(BaseViewModel.toMessage(exception));
                        mViewModel.setShowMessage(true);
                    }
                })
                //.loggingEnabled(true)
                //.indicatorsEnabled(true)
                .build();

        return view;
    }

    @Override
    public void onRefresh() {
        mViewModel.load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.book_browser, menu);

        MenuItem menuItem = menu.findItem(mFilterRead);
        menuItem.setChecked(true);

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

                String bookMarkStatus = getBookMarkStatus();

                mViewModel.setBookMarkStatus(bookMarkStatus);
                mViewModel.loadBookList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openBook(BookDto bookDto) {
        Intent intent = new Intent(getActivity(), BookReaderActivity.class);
        intent.putExtra(BookReaderViewModel.PARAM_MODE, BookReaderViewModel.Mode.MODE_REMOTE);
        intent.putExtra(BookReaderViewModel.PARAM_BOOK_ID, bookDto.getId());
        mBookReaderActivityResultLauncher.launch(intent);
    }

    private BookDto getBookAtPosition(int position) {
        BookDto bookDto = mViewModel.getBookList().get(position);
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
            return mViewModel.getBookList().size();
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

                    mViewModel.setSelectedBook(selectedBookDto);
                    mViewModel.setShowMarkSelectedBookDialog(true);
                }
            });

            mBookDownloadImageView = (ImageView) itemView.findViewById(R.id.bookDownloadImageView);

            mBookDownloadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto selectedBookDto = getBookAtPosition(i);

                    mViewModel.setSelectedBook(selectedBookDto);
                    mViewModel.setShowDownloadSelectedBookDialog(true);
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

            Uri uri = mRequestHandler.getBookPageUri(bookDto, "DEFAULT", Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            mPicasso.load(uri)
                    .into(mBookImageView);
        }
    }
}
