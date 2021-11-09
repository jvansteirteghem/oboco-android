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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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
import com.gitlab.jeeto.oboco.activity.MainActivity;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookCollectionMarkDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.DownloadBookCollectionWorker;

import java.util.List;
import java.util.Objects;

public class BookCollectionBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final int ITEM_VIEW_TYPE_BOOK_COLLECTION = 1;

    private RecyclerView mBookCollectionListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;
    private SearchView mSearchView;
    private String mSearch;

    private BookCollectionBrowserViewModel.Mode mMode;

    private BookCollectionBrowserViewModel mViewModel;

    private AlertDialog mMarkSelectedBookCollectionDialog;
    private AlertDialog mDownloadSelectedBookCollectionDialog;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static BookCollectionBrowserFragment create(Long bookCollectionId) {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE);
        args.putLong(BookCollectionBrowserViewModel.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createAll() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_ALL);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createNew() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_NEW);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createToRead() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_TO_READ);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createLatestRead() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_LATEST_READ);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createRead() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READ);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createReading() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READING);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookCollectionBrowserFragment createUnread() {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookCollectionBrowserViewModel.PARAM_MODE, BookCollectionBrowserViewModel.Mode.MODE_REMOTE_UNREAD);
        fragment.setArguments(args);
        return fragment;
    }

    public BookCollectionBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = (BookCollectionBrowserViewModel.Mode) getArguments().getSerializable(BookCollectionBrowserViewModel.PARAM_MODE);

        if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_ALL.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_NEW.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_TO_READ.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_LATEST_READ.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READ.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READING.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_UNREAD.equals(mMode)) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteFilteredBookCollectionBrowserViewModel.class);
        }
    }

    @Override
    public void onRefresh() {
        if(mViewModel.getBookCollection() == null) {
            mViewModel.load();
        } else {
            mViewModel.loadBookCollectionList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String title;
        if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_ALL.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_all);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_NEW.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_new);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_TO_READ.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_to_read);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_LATEST_READ.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_latest_read);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READ.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_read);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READING.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_reading);
        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_UNREAD.equals(mMode)) {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser_unread);
        } else {
            title = getResources().getString(R.string.drawer_menu_book_collection_browser);
        }

        getActivity().setTitle(title);

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
                int itemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!mViewModel.getIsLoading() && mViewModel.hasNextBookCollectionList()) {
                    if (firstVisibleItemPosition >= 0 && (firstVisibleItemPosition + visibleItemCount) >= itemCount) {
                        mViewModel.loadNextBookCollectionList();
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

        mViewModel.getBookCollectionObservable().observe(getViewLifecycleOwner(), new Observer<BookCollectionDto>() {
            @Override
            public void onChanged(BookCollectionDto bookCollection) {
                if(bookCollection.getParentBookCollection() != null) {
                    getActivity().setTitle(bookCollection.getName());
                }
            }
        });
        mViewModel.getBookCollectionListObservable().observe(getViewLifecycleOwner(), new Observer<List<BookCollectionDto>>() {
            @Override
            public void onChanged(List<BookCollectionDto> bookCollectionList) {
                if(bookCollectionList.size() != 0) {
                    mNotEmptyView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                } else {
                    mNotEmptyView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }

                mBookCollectionListView.getAdapter().notifyDataSetChanged();
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
        mViewModel.getShowMarkSelectedBookCollectionDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showMarkSelectedBookCollectionDialog) {
                if(showMarkSelectedBookCollectionDialog) {
                    if(mMarkSelectedBookCollectionDialog == null) {
                        mMarkSelectedBookCollectionDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle(R.string.book_collection_browser_dialog_mark)
                                .setItems(R.array.book_collection_browser_dialog_mark_as_array, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == 0) {
                                            BookCollectionMarkDto bookCollectionMarkDto = new BookCollectionMarkDto();
                                            bookCollectionMarkDto.setBookPage(0);

                                            mViewModel.addBookMark(bookCollectionMarkDto);
                                        } else if(which == 1) {
                                            BookCollectionMarkDto bookCollectionMarkDto = new BookCollectionMarkDto();
                                            bookCollectionMarkDto.setBookPage(-1);

                                            mViewModel.addBookMark(bookCollectionMarkDto);
                                        } else if(which == 2) {
                                            mViewModel.removeBookMark();
                                        }

                                        mViewModel.setShowMarkSelectedBookCollectionDialog(false);

                                        mMarkSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.book_collection_browser_dialog_mark_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowMarkSelectedBookCollectionDialog(false);

                                        mMarkSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowMarkSelectedBookCollectionDialog(false);

                                        mMarkSelectedBookCollectionDialog = null;
                                    }
                                })
                                .create();
                        mMarkSelectedBookCollectionDialog.show();
                    }
                }
            }
        });
        mViewModel.getShowDownloadSelectedBookCollectionDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showDownloadSelectedBookCollectionDialog) {
                if(showDownloadSelectedBookCollectionDialog) {
                    if(mDownloadSelectedBookCollectionDialog == null) {
                        mDownloadSelectedBookCollectionDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle(R.string.book_collection_browser_dialog_download)
                                .setMessage(mViewModel.getSelectedBookCollection().getName())
                                .setPositiveButton(R.string.book_collection_browser_dialog_download_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        WorkRequest downloadWorkRequest = DownloadBookCollectionWorker.createDownloadWorkRequest(mViewModel.getSelectedBookCollection().getId(), mViewModel.getSelectedBookCollection().getName());

                                        WorkManager
                                                .getInstance(getContext().getApplicationContext())
                                                .enqueue(downloadWorkRequest);

                                        mViewModel.setShowDownloadSelectedBookCollectionDialog(false);

                                        mDownloadSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.book_collection_browser_dialog_download_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDownloadSelectedBookCollectionDialog(false);

                                        mDownloadSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDownloadSelectedBookCollectionDialog(false);

                                        mDownloadSelectedBookCollectionDialog = null;
                                    }
                                })
                                .create();
                        mDownloadSelectedBookCollectionDialog.show();
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
    public void onDestroyView() {
        if(mMarkSelectedBookCollectionDialog != null) {
            mMarkSelectedBookCollectionDialog.dismiss();
            mMarkSelectedBookCollectionDialog = null;
        }

        if(mDownloadSelectedBookCollectionDialog != null) {
            mDownloadSelectedBookCollectionDialog.dismiss();
            mDownloadSelectedBookCollectionDialog = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.book_collection_browser, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.search);

        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String search) {
                mSearch = search;

                mViewModel.setSearch(mSearch);
                mViewModel.loadBookCollectionList();

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                mSearchView.clearFocus();

                return true;
            }
        });

        mSearch = "";

        mViewModel.getSearchObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String search) {
                if(!Objects.equals(mSearch, search)) {
                    mSearch = search;

                    mSearchView.setQuery(mSearch, false);
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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
            return mViewModel.getBookCollectionList().size();
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
                BookCollectionDto bookCollectionDto = mViewModel.getBookCollectionList().get(i);
                BookCollectionViewHolder holder = (BookCollectionViewHolder) viewHolder;
                holder.setupBookCollection(bookCollectionDto);
            }
        }
    }

    private class BookCollectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mBookCollectionImageView;
        private TextView mBookCollectionTextView;
        private TextView mBookCollectionBookMarkTextView;
        private ImageView mBookCollectionBookMarkImageView;
        private ImageView mBookCollectionDownloadImageView;

        public BookCollectionViewHolder(View itemView) {
            super(itemView);
            if(mViewModel.getBookCollectionList().size() > 0) {
                mBookCollectionImageView = (ImageView) itemView.findViewById(R.id.bookCollectionImageView);
                mBookCollectionImageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto bookCollectionDto = mViewModel.getBookCollectionList().get(i);

                        String filterType;
                        if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE.equals(mMode)) {
                            filterType = "ALL";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_ALL.equals(mMode)) {
                            filterType = "ALL";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_NEW.equals(mMode)) {
                            filterType = "NEW";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_TO_READ.equals(mMode)) {
                            filterType = "TO_READ";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_LATEST_READ.equals(mMode)) {
                            filterType = "LATEST_READ";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READ.equals(mMode)) {
                            filterType = "READ";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_READING.equals(mMode)) {
                            filterType = "READING";
                        } else if(BookCollectionBrowserViewModel.Mode.MODE_REMOTE_UNREAD.equals(mMode)) {
                            filterType = "UNREAD";
                        } else {
                            filterType = "ALL";
                        }

                        BookBrowserFragment fragment = BookBrowserFragment.create(bookCollectionDto.getId(), filterType);
                        ((MainActivity) getActivity()).pushFragment(fragment);
                    }
                });

                mBookCollectionTextView = (TextView) itemView.findViewById(R.id.bookCollectionTextView);

                mBookCollectionBookMarkTextView = (TextView) itemView.findViewById(R.id.bookCollectionBookMarkTextView);

                mBookCollectionBookMarkImageView = (ImageView) itemView.findViewById(R.id.bookCollectionBookMarkImageView);

                mBookCollectionBookMarkImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollectionDto = mViewModel.getBookCollectionList().get(i);

                        mViewModel.setSelectedBookCollection(selectedBookCollectionDto);
                        mViewModel.setShowMarkSelectedBookCollectionDialog(true);
                    }
                });

                mBookCollectionDownloadImageView = (ImageView) itemView.findViewById(R.id.bookCollectionDownloadImageView);

                mBookCollectionDownloadImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getAdapterPosition();
                        BookCollectionDto selectedBookCollectionDto = mViewModel.getBookCollectionList().get(i);

                        mViewModel.setSelectedBookCollection(selectedBookCollectionDto);
                        mViewModel.setShowDownloadSelectedBookCollectionDialog(true);
                    }
                });
            }

            itemView.setOnClickListener(this);
        }

        public void setupBookCollection(BookCollectionDto bookCollectionDto) {
            mBookCollectionTextView.setText(bookCollectionDto.getName());

            if(bookCollectionDto.getNumberOfBooks() != 0) {
                mBookCollectionImageView.setVisibility(View.VISIBLE);
                mBookCollectionBookMarkTextView.setVisibility(View.VISIBLE);
                mBookCollectionBookMarkImageView.setVisibility(View.VISIBLE);
                mBookCollectionDownloadImageView.setVisibility(View.VISIBLE);

                mBookCollectionImageView.setImageResource(android.R.color.transparent);

                Uri uri = mViewModel.getBookCollectionPageUri(bookCollectionDto, "DEFAULT", Constants.COVER_THUMBNAIL_HEIGHT, Constants.COVER_THUMBNAIL_WIDTH);
                mViewModel.getPicasso().load(uri)
                        .tag(getActivity())
                        .into(mBookCollectionImageView);

                BookCollectionMarkDto bookCollectionMarkDto = bookCollectionDto.getBookCollectionMark();
                if(bookCollectionMarkDto == null) {
                    mBookCollectionBookMarkTextView.setText("0/0");
                } else {
                    mBookCollectionBookMarkTextView.setText(bookCollectionMarkDto.getBookPage() + "/" + bookCollectionMarkDto.getNumberOfBookPages());
                }
            } else {
                mBookCollectionImageView.setVisibility(View.GONE);
                mBookCollectionBookMarkTextView.setVisibility(View.GONE);
                mBookCollectionBookMarkImageView.setVisibility(View.GONE);
                mBookCollectionDownloadImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int i = getAdapterPosition();
            BookCollectionDto bookCollectionDto = mViewModel.getBookCollectionList().get(i);

            BookCollectionBrowserFragment fragment = BookCollectionBrowserFragment.create(bookCollectionDto.getId());
            ((MainActivity)getActivity()).pushFragment(fragment);
        }
    }
}
