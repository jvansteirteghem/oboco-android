package com.gitlab.jeeto.oboco.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
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
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.DownloadBookWorker;

import java.util.List;
import java.util.Objects;

public class BookBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final int ITEM_VIEW_TYPE_BOOK = 1;

    private RecyclerView mBookListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;
    private Menu mMenu;
    private String mFilterType;

    private ActivityResultLauncher<Intent> mBookReaderActivityResultLauncher;

    private BookBrowserViewModel mViewModel;

    private PopupMenu mSelectedBookMenu;
    private AlertDialog mMarkSelectedBookDialog;
    private AlertDialog mDownloadSelectedBookDialog;

    public static BookBrowserFragment create(Long bookCollectionId, String filterType) {
        BookBrowserFragment fragment = new BookBrowserFragment();
        Bundle args = new Bundle();
        args.putLong(BookBrowserViewModel.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        args.putString(BookBrowserViewModel.PARAM_FILTER_TYPE, filterType);
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
        if(mSelectedBookMenu != null) {
            mSelectedBookMenu.dismiss();
        }

        if(mMarkSelectedBookDialog != null) {
            mMarkSelectedBookDialog.dismiss();
        }

        if(mDownloadSelectedBookDialog != null) {
            mDownloadSelectedBookDialog.dismiss();
        }

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mRefreshView.setColorSchemeResources(R.color.darker);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.bookBrowserNotEmpty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.bookBrowserEmpty);
        mEmptyView.setVisibility(View.GONE);

        mViewModel.getTitleObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String title) {
                getActivity().setTitle(title);
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
                                .setTitle(R.string.book_browser_dialog_mark)
                                .setItems(R.array.book_browser_dialog_mark_as_array, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0) {
                                            BookMarkDto bookMarkDto = new BookMarkDto();
                                            bookMarkDto.setPage(0);

                                            mViewModel.addBookMark(bookMarkDto);
                                        } else if(which == 1) {
                                            BookMarkDto bookMarkDto = new BookMarkDto();
                                            bookMarkDto.setPage(-1);

                                            mViewModel.addBookMark(bookMarkDto);
                                        } else if(which == 2) {
                                            mViewModel.removeBookMark();
                                        }

                                        mViewModel.setShowMarkSelectedBookDialog(false);
                                    }
                                })
                                .setNegativeButton(R.string.book_browser_dialog_mark_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowMarkSelectedBookDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowMarkSelectedBookDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
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
                                .setTitle(R.string.book_browser_dialog_download)
                                .setMessage(mViewModel.getSelectedBook().getName())
                                .setPositiveButton(R.string.book_browser_dialog_download_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WorkRequest downloadWorkRequest = DownloadBookWorker.createDownloadWorkRequest(mViewModel.getSelectedBook().getId(), mViewModel.getSelectedBook().getName());

                                        WorkManager
                                                .getInstance(getContext().getApplicationContext())
                                                .enqueue(downloadWorkRequest);

                                        mViewModel.setShowDownloadSelectedBookDialog(false);
                                    }
                                })
                                .setNegativeButton(R.string.book_browser_dialog_download_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDownloadSelectedBookDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDownloadSelectedBookDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
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

        return view;
    }

    @Override
    public void onRefresh() {
        if(mViewModel.getBookCollection() == null) {
            mViewModel.load();
        } else {
            mViewModel.loadBookList();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.book_browser, menu);

        mMenu = menu;

        MenuItem menuItem = mMenu.findItem(R.id.menu_book_browser_filter_type_all);
        menuItem.setChecked(true);

        mFilterType = "ALL";

        mViewModel.getFilterTypeObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String filterType) {
                if(!Objects.equals(mFilterType, filterType)) {
                    mFilterType = filterType;

                    int menuItemId;
                    if ("ALL".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_all;
                    } else if ("NEW".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_new;
                    } else if ("TO_READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_to_read;
                    } else if ("LATEST_READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_latest_read;
                    } else if ("READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_read;
                    } else if ("READING".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_reading;
                    } else if ("UNREAD".equals(filterType)) {
                        menuItemId = R.id.menu_book_browser_filter_type_unread;
                    } else {
                        menuItemId = R.id.menu_book_browser_filter_type_all;
                    }
                    MenuItem menuItem = mMenu.findItem(menuItemId);
                    menuItem.setChecked(true);
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.menu_book_browser_filter_type_all:
            case R.id.menu_book_browser_filter_type_new:
            case R.id.menu_book_browser_filter_type_to_read:
            case R.id.menu_book_browser_filter_type_latest_read:
            case R.id.menu_book_browser_filter_type_read:
            case R.id.menu_book_browser_filter_type_reading:
            case R.id.menu_book_browser_filter_type_unread:
                menuItem.setChecked(true);

                if (menuItemId == R.id.menu_book_browser_filter_type_all) {
                    mFilterType = "ALL";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_new) {
                    mFilterType = "NEW";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_to_read) {
                    mFilterType = "TO_READ";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_latest_read) {
                    mFilterType = "LATEST_READ";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_read) {
                    mFilterType = "READ";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_reading) {
                    mFilterType = "READING";
                } else if (menuItemId == R.id.menu_book_browser_filter_type_unread) {
                    mFilterType = "UNREAD";
                } else {
                    mFilterType = "ALL";
                }

                mViewModel.setFilterType(mFilterType);

                mBookListView.scrollToPosition(0);

                mViewModel.loadBookList();
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
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

    private class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView mBookMarkTextView;
        private ImageView mBookImageView;
        private TextView mBookNameTextView;
        private TextView mBookNumberOfTextView;
        private ImageView mBookMenuImageView;

        public BookViewHolder(View itemView) {
            super(itemView);
            mBookMarkTextView = (TextView) itemView.findViewById(R.id.bookMarkTextView);

            mBookImageView = (ImageView) itemView.findViewById(R.id.bookImageView);
            mBookImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int i = getAdapterPosition();
                    BookDto bookDto = getBookAtPosition(i);
                    openBook(bookDto);
                }
            });

            mBookNameTextView = (TextView) itemView.findViewById(R.id.bookNameTextView);

            mBookNumberOfTextView = (TextView) itemView.findViewById(R.id.bookNumberOfTextView);

            mBookMenuImageView = (ImageView) itemView.findViewById(R.id.bookMenuImageView);
            mBookMenuImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mSelectedBookMenu == null) {
                        Context contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.MyPopupMenuTheme);
                        mSelectedBookMenu = new PopupMenu(contextThemeWrapper, mBookMenuImageView, Gravity.NO_GRAVITY, 0, R.style.MyPopupMenuOverflowTheme);
                        mSelectedBookMenu.getMenuInflater().inflate(R.menu.book_browser_book, mSelectedBookMenu.getMenu());
                        mSelectedBookMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int i = getAdapterPosition();
                                BookDto selectedBookDto = getBookAtPosition(i);

                                mViewModel.setSelectedBook(selectedBookDto);

                                switch (menuItem.getItemId()){
                                    case R.id.menu_book_browser_book_mark:
                                        mViewModel.setShowMarkSelectedBookDialog(true);
                                        return true;
                                    case R.id.menu_book_browser_book_download:
                                        mViewModel.setShowDownloadSelectedBookDialog(true);
                                        return true;
                                }
                                return false;
                            }
                        });
                        mSelectedBookMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {
                                mSelectedBookMenu = null;
                            }
                        });
                        mSelectedBookMenu.show();
                    }
                }
            });
        }

        public void setupBook(BookDto bookDto) {
            mBookNameTextView.setText(bookDto.getName());

            mBookNumberOfTextView.setText(bookDto.getNumberOfPages().toString());

            BookMarkDto bookMarkDto = bookDto.getBookMark();
            if(bookMarkDto == null) {
                mBookMarkTextView.setVisibility(View.GONE);
            } else {
                mBookMarkTextView.setVisibility(View.VISIBLE);
                mBookMarkTextView.setText(bookMarkDto.getProgress().toString() + "%");
            }

            mBookImageView.setImageResource(android.R.color.transparent);

            Uri uri = mViewModel.getBookPageUri(bookDto, "DEFAULT", Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT);
            mViewModel.getPicasso().load(uri)
                    .resize(Constants.COVER_THUMBNAIL_WIDTH, Constants.COVER_THUMBNAIL_HEIGHT)
                    .centerCrop(Gravity.CENTER)
                    .into(mBookImageView);
        }
    }
}
