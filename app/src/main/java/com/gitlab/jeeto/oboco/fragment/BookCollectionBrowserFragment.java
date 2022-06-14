package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.gitlab.jeeto.oboco.activity.MainActivity;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookCollectionMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.DownloadBookCollectionWorker;

import java.util.List;
import java.util.Objects;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

public class BookCollectionBrowserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    final int ITEM_VIEW_TYPE_BOOK_COLLECTION = 1;

    private RecyclerView mBookCollectionListView;
    private View mEmptyView;
    private View mNotEmptyView;
    private SwipeRefreshLayout mRefreshView;
    private Menu mMenu;
    private AlertDialog mSearchDialog;
    private String mFilterType;

    private BookCollectionBrowserViewModel mViewModel;

    private PopupMenu mSelectedBookCollectionMenu;
    private AlertDialog mMarkSelectedBookCollectionDialog;
    private AlertDialog mDownloadSelectedBookCollectionDialog;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static BookCollectionBrowserFragment create(Long bookCollectionId, String filterType) {
        BookCollectionBrowserFragment fragment = new BookCollectionBrowserFragment();
        Bundle args = new Bundle();
        args.putLong(BookCollectionBrowserViewModel.PARAM_BOOK_COLLECTION_ID, bookCollectionId);
        args.putString(BookCollectionBrowserViewModel.PARAM_FILTER_TYPE, filterType);
        fragment.setArguments(args);
        return fragment;
    }

    public BookCollectionBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteBookCollectionBrowserViewModel.class);
    }

    @Override
    public void onRefresh() {
        if(mViewModel.getBookCollection() == null) {
            mViewModel.load();
        } else {
            mViewModel.loadBookCollectionList();
        }
    }

    private static int getDimensionFromAttribute(Context context, int attributeId) {
        final TypedValue value = new TypedValue();
        if(context.getTheme().resolveAttribute(attributeId, value, true)) {
            return TypedValue.complexToDimensionPixelSize(value.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_book_collection_browser, container, false);

        int margin = (int) getResources().getDimension(R.dimen.grid_margin);

        int deviceWidth = Utils.getDeviceWidth(getActivity());

        int columnWidth = getResources().getInteger(R.integer.grid_book_collection_column_width);

        int numberOfColumns = Math.round((float) deviceWidth / columnWidth);

        int bookPageWidth = Constants.BOOK_COLLECTION_PAGE_WIDTH;
        int bookPageHeight = Constants.BOOK_COLLECTION_PAGE_HEIGHT;

        int width = (int) Math.ceil((float) (deviceWidth - (margin * (numberOfColumns + 1))) / numberOfColumns);
        if(width > bookPageWidth) {
            bookPageWidth = bookPageWidth + (Constants.BOOK_COLLECTION_PAGE_WIDTH / 2);
            bookPageHeight = bookPageHeight + (Constants.BOOK_COLLECTION_PAGE_HEIGHT / 2);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numberOfColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mBookCollectionListView = (RecyclerView) view.findViewById(R.id.bookCollectionBrowserGrid);
        mBookCollectionListView.setHasFixedSize(true);
        mBookCollectionListView.setLayoutManager(layoutManager);
        mBookCollectionListView.setAdapter(new BookCollectionGridAdapter(bookPageWidth, bookPageHeight));
        mBookCollectionListView.addItemDecoration(new GridMarginItemDecoration(numberOfColumns, margin));
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
        mRefreshView.setColorSchemeResources(R.color.darkest_lightest);
        mRefreshView.setProgressBackgroundColorSchemeResource(R.color.lightest_darker);
        mRefreshView.setOnRefreshListener(this);
        mRefreshView.setEnabled(true);

        mNotEmptyView = view.findViewById(R.id.bookCollectionBrowserNotEmpty);
        mNotEmptyView.setVisibility(View.GONE);

        mEmptyView = view.findViewById(R.id.bookCollectionBrowserEmpty);
        mEmptyView.setVisibility(View.GONE);

        mViewModel.getTitleObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String title) {
                getActivity().setTitle(title);
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
        mViewModel.getShowSearchDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showSearchDialog) {
                if(showSearchDialog) {
                    if(mSearchDialog == null) {
                        if(mViewModel.getSearchDialogSearchType().equals("") && mViewModel.getSearchDialogSearch().equals("")) {
                            mViewModel.setSearchDialogSearchType("NAME");
                            mViewModel.setSearchDialogSearch("");
                        }

                        int searchDialogPadding = getDimensionFromAttribute(getActivity(), R.attr.dialogPreferredPadding);

                        LinearLayout searchDialogLinearLayout = new LinearLayout(getActivity());
                        searchDialogLinearLayout.setOrientation(LinearLayout.VERTICAL);
                        searchDialogLinearLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        searchDialogLinearLayout.setPadding(searchDialogPadding, 0, searchDialogPadding, 0);

                        EditText searchDialogSearchEditText = new EditText(getActivity());
                        searchDialogSearchEditText.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        searchDialogSearchEditText.setSingleLine(true);
                        searchDialogSearchEditText.setHint(R.string.book_collection_browser_dialog_search_hint);
                        searchDialogSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        searchDialogSearchEditText.setText(mViewModel.getSearchDialogSearch());
                        searchDialogSearchEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                // do nothing
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                // do nothing
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                mViewModel.setSearchDialogSearch(editable.toString());
                            }
                        });

                        searchDialogLinearLayout.addView(searchDialogSearchEditText);

                        mSearchDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.book_collection_browser_dialog_search)
                                .setView(searchDialogLinearLayout)
                                .setPositiveButton(R.string.book_collection_browser_dialog_search_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(mViewModel.getSearchType().equals(mViewModel.getSearchDialogSearchType()) == false || mViewModel.getSearch().equals(mViewModel.getSearchDialogSearch()) == false) {
                                            mViewModel.setSearchType(mViewModel.getSearchDialogSearchType());
                                            mViewModel.setSearch(mViewModel.getSearchDialogSearch());

                                            mBookCollectionListView.scrollToPosition(0);

                                            mViewModel.loadBookCollectionList();
                                        }

                                        mViewModel.setShowSearchDialog(false);
                                    }
                                })
                                .setNegativeButton(R.string.book_collection_browser_dialog_search_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setSearchDialogSearchType("");
                                        mViewModel.setSearchDialogSearch("");

                                        if(mViewModel.getSearchType().equals(mViewModel.getSearchDialogSearchType()) == false || mViewModel.getSearch().equals(mViewModel.getSearchDialogSearch()) == false) {
                                            mViewModel.setSearchType(mViewModel.getSearchDialogSearchType());
                                            mViewModel.setSearch(mViewModel.getSearchDialogSearch());

                                            mBookCollectionListView.scrollToPosition(0);

                                            mViewModel.loadBookCollectionList();
                                        }

                                        mViewModel.setShowSearchDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setSearchDialogSearchType(mViewModel.getSearchType());
                                        mViewModel.setSearchDialogSearch(mViewModel.getSearch());

                                        mViewModel.setShowSearchDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        mSearchDialog = null;
                                    }
                                })
                                .create();
                        mSearchDialog.show();
                    }
                }
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
                        mMarkSelectedBookCollectionDialog = new AlertDialog.Builder(getActivity())
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
                                    }
                                })
                                .setNegativeButton(R.string.book_collection_browser_dialog_mark_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowMarkSelectedBookCollectionDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowMarkSelectedBookCollectionDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
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
                        mDownloadSelectedBookCollectionDialog = new AlertDialog.Builder(getActivity())
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
                                    }
                                })
                                .setNegativeButton(R.string.book_collection_browser_dialog_download_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDownloadSelectedBookCollectionDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDownloadSelectedBookCollectionDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
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

                    ((MainActivity) getActivity()).showMessage(mViewModel.getMessage());
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        if(mSearchDialog != null) {
            mSearchDialog.dismiss();
        }

        if(mSelectedBookCollectionMenu != null) {
            mSelectedBookCollectionMenu.dismiss();
        }

        if(mMarkSelectedBookCollectionDialog != null) {
            mMarkSelectedBookCollectionDialog.dismiss();
        }

        if(mDownloadSelectedBookCollectionDialog != null) {
            mDownloadSelectedBookCollectionDialog.dismiss();
        }

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.book_collection_browser, menu);

        mMenu = menu;

        MenuItem searchMenuItem = menu.findItem(R.id.menu_book_collection_browser_search);
        searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mViewModel.setShowSearchDialog(true);
                return true;
            }
        });

        MenuItem menuItem = mMenu.findItem(R.id.menu_book_collection_browser_filter_type_root);
        menuItem.setChecked(true);

        mFilterType = "ROOT";

        mViewModel.getFilterTypeObservable().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String filterType) {
                if(!Objects.equals(mFilterType, filterType)) {
                    mFilterType = filterType;

                    int menuItemId;
                    if ("ROOT".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_root;
                    } else if ("ALL".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_all;
                    } else if ("NEW".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_new;
                    } else if ("TO_READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_to_read;
                    } else if ("LATEST_READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_latest_read;
                    } else if ("READ".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_read;
                    } else if ("READING".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_reading;
                    } else if ("UNREAD".equals(filterType)) {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_unread;
                    } else {
                        menuItemId = R.id.menu_book_collection_browser_filter_type_root;
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
            case R.id.menu_book_collection_browser_filter_type_root:
            case R.id.menu_book_collection_browser_filter_type_all:
            case R.id.menu_book_collection_browser_filter_type_new:
            case R.id.menu_book_collection_browser_filter_type_to_read:
            case R.id.menu_book_collection_browser_filter_type_latest_read:
            case R.id.menu_book_collection_browser_filter_type_read:
            case R.id.menu_book_collection_browser_filter_type_reading:
            case R.id.menu_book_collection_browser_filter_type_unread:
                menuItem.setChecked(true);

                String filterType;
                if (menuItemId == R.id.menu_book_collection_browser_filter_type_root) {
                    filterType = "ROOT";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_all) {
                    filterType = "ALL";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_new) {
                    filterType = "NEW";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_to_read) {
                    filterType = "TO_READ";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_latest_read) {
                    filterType = "LATEST_READ";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_read) {
                    filterType = "READ";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_reading) {
                    filterType = "READING";
                } else if (menuItemId == R.id.menu_book_collection_browser_filter_type_unread) {
                    filterType = "UNREAD";
                } else {
                    filterType = "ROOT";
                }

                if("ROOT".equals(mFilterType) || "ROOT".equals(filterType)) {
                    BookCollectionBrowserFragment fragment = BookCollectionBrowserFragment.create(-1L, filterType);
                    ((MainActivity)getActivity()).setFragment(fragment);
                } else {
                    mFilterType = filterType;

                    mViewModel.setFilterType(mFilterType);

                    mViewModel.setSearchType("");
                    mViewModel.setSearch("");
                    mViewModel.setSearchDialogSearchType(mViewModel.getSearchType());
                    mViewModel.setSearchDialogSearch(mViewModel.getSearch());

                    mBookCollectionListView.scrollToPosition(0);

                    mViewModel.loadBookCollectionList();
                }
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        };
    }

    private final class GridMarginItemDecoration extends RecyclerView.ItemDecoration {
        private int mNumberOfColumns;
        private int mMargin;

        public GridMarginItemDecoration(int numberOfColumns, int margin) {
            mNumberOfColumns = numberOfColumns;
            mMargin = margin;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            int column = position % mNumberOfColumns;

            outRect.left = mMargin - column * mMargin / mNumberOfColumns;
            outRect.right = (column + 1) * mMargin / mNumberOfColumns;

            if (position < mNumberOfColumns) {
                outRect.top = mMargin;
            }
            outRect.bottom = mMargin;
        }
    }

    private final class BookCollectionGridAdapter extends RecyclerView.Adapter {
        private int mBookPageWidth;
        private int mBookPageHeight;

        public BookCollectionGridAdapter(int bookPageWidth, int bookPageHeight) {
            mBookPageWidth = bookPageWidth;
            mBookPageHeight = bookPageHeight;
        }

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
            return new BookCollectionViewHolder(view, mBookPageWidth, mBookPageHeight);
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

    private class BookCollectionViewHolder extends RecyclerView.ViewHolder {
        private int mBookPageWidth;
        private int mBookPageHeight;
        private TextView mBookCollectionMarkTextView;
        private ImageView mBookCollectionImageView;
        private TextView mBookCollectionNameTextView;
        private TextView mBookCollectionNumberOfTextView;
        private ImageView mBookCollectionMenuImageView;

        public BookCollectionViewHolder(View itemView, int bookPageWidth, int bookPageHeight) {
            super(itemView);

            mBookPageWidth = bookPageWidth;
            mBookPageHeight = bookPageHeight;

            if(mViewModel.getBookCollectionList().size() > 0) {
                mBookCollectionMarkTextView = (TextView) itemView.findViewById(R.id.bookCollectionMarkTextView);

                mBookCollectionImageView = (ImageView) itemView.findViewById(R.id.bookCollectionImageView);
                mBookCollectionImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getBindingAdapterPosition();
                        BookCollectionDto bookCollectionDto = mViewModel.getBookCollectionList().get(i);

                        BookBrowserFragment fragment = BookBrowserFragment.create(bookCollectionDto.getId(), "ALL");
                        ((MainActivity) getActivity()).pushFragment(fragment);
                    }
                });

                mBookCollectionNameTextView = (TextView) itemView.findViewById(R.id.bookCollectionNameTextView);

                mBookCollectionNumberOfTextView = (TextView) itemView.findViewById(R.id.bookCollectionNumberOfTextView);

                mBookCollectionMenuImageView = (ImageView) itemView.findViewById(R.id.bookCollectionMenuImageView);
                mBookCollectionMenuImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mSelectedBookCollectionMenu == null) {
                            Context contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.AppThemePopupMenu);
                            mSelectedBookCollectionMenu = new PopupMenu(contextThemeWrapper, mBookCollectionMenuImageView, Gravity.NO_GRAVITY, 0, R.style.AppThemePopupMenuOverflow);
                            mSelectedBookCollectionMenu.getMenuInflater().inflate(R.menu.book_collection_browser_book_collection, mSelectedBookCollectionMenu.getMenu());
                            mSelectedBookCollectionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    int i = getBindingAdapterPosition();
                                    BookCollectionDto selectedBookCollectionDto = mViewModel.getBookCollectionList().get(i);

                                    mViewModel.setSelectedBookCollection(selectedBookCollectionDto);

                                    switch (menuItem.getItemId()){
                                        case R.id.menu_book_collection_browser_book_collection_mark:
                                            mViewModel.setShowMarkSelectedBookCollectionDialog(true);
                                            return true;
                                        case R.id.menu_book_collection_browser_book_collection_download:
                                            mViewModel.setShowDownloadSelectedBookCollectionDialog(true);
                                            return true;
                                    }
                                    return false;
                                }
                            });
                            mSelectedBookCollectionMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                                @Override
                                public void onDismiss(PopupMenu menu) {
                                    mSelectedBookCollectionMenu = null;
                                }
                            });
                            mSelectedBookCollectionMenu.show();
                        }
                    }
                });
            }

            if("ROOT".equals(mViewModel.getFilterType())) {
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                itemView.setForeground(getDrawable(getContext(), outValue.resourceId));
                itemView.setClickable(true);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int i = getBindingAdapterPosition();
                        BookCollectionDto bookCollectionDto = mViewModel.getBookCollectionList().get(i);

                        BookCollectionBrowserFragment fragment = BookCollectionBrowserFragment.create(bookCollectionDto.getId(), "ROOT");
                        ((MainActivity) getActivity()).pushFragment(fragment);
                    }
                });
            }
        }

        public void setupBookCollection(BookCollectionDto bookCollectionDto) {
            mBookCollectionNameTextView.setText(bookCollectionDto.getName());

            mBookCollectionNumberOfTextView.setText(bookCollectionDto.getNumberOfBookCollections().toString() + " - " + bookCollectionDto.getNumberOfBooks().toString() + " - " + bookCollectionDto.getNumberOfBookPages().toString());

            if(bookCollectionDto.getNumberOfBooks() != 0) {
                mBookCollectionImageView.setVisibility(View.VISIBLE);

                mBookCollectionImageView.setImageResource(android.R.color.transparent);

                Uri uri = mViewModel.getBookCollectionPageUri(bookCollectionDto, "DEFAULT", mBookPageWidth, mBookPageHeight);
                mViewModel.getPicasso().load(uri)
                        .tag(getActivity())
                        .fit()
                        .centerCrop(Gravity.TOP)
                        .into(mBookCollectionImageView);

                BookCollectionMarkDto bookCollectionMarkDto = bookCollectionDto.getBookCollectionMark();
                if(bookCollectionMarkDto == null) {
                    mBookCollectionMarkTextView.setVisibility(View.GONE);
                } else {
                    mBookCollectionMarkTextView.setVisibility(View.VISIBLE);
                    mBookCollectionMarkTextView.setText(bookCollectionMarkDto.getProgress().toString() + "%");
                }
            } else {
                mBookCollectionImageView.setVisibility(View.GONE);
                mBookCollectionMarkTextView.setVisibility(View.GONE);
            }
        }
    }
}
