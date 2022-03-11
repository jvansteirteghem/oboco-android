package com.gitlab.jeeto.oboco.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.gitlab.jeeto.oboco.Constants;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.view.BookViewPager;
import com.gitlab.jeeto.oboco.view.PageImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BookReaderFragment extends Fragment implements View.OnTouchListener {
    private BookViewPager mViewPager;
    private LinearLayout mPageNavLayout;
    private SeekBar mPageNavSeekBar;
    private TextView mPageNavTextView;
    private SharedPreferences mPreferences;
    private GestureDetector mGestureDetector;

    private final static HashMap<Integer, Constants.PageViewMode> RESOURCE_VIEW_MODE;
    private Constants.PageViewMode mPageViewMode;
    private boolean mIsLeftToRight;

    private SparseArray<BookReaderTarget> mTargets = new SparseArray<>();

    private BookReaderViewModel.Mode mMode;

    private Dialog mOpenBookDialog;

    private BookReaderViewModel mViewModel;

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    public static BookReaderFragment create(Long bookId) {
        BookReaderFragment fragment = new BookReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookReaderViewModel.PARAM_MODE, BookReaderViewModel.Mode.MODE_REMOTE);
        args.putLong(BookReaderViewModel.PARAM_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    public static BookReaderFragment create(String bookPath) {
        BookReaderFragment fragment = new BookReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookReaderViewModel.PARAM_MODE, BookReaderViewModel.Mode.MODE_LOCAL);
        args.putString(BookReaderViewModel.PARAM_BOOK_PATH, bookPath);
        fragment.setArguments(args);
        return fragment;
    }

    public BookReaderFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = (BookReaderViewModel.Mode) getArguments().getSerializable(BookReaderViewModel.PARAM_MODE);

        if(mMode == BookReaderViewModel.Mode.MODE_REMOTE) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(RemoteBookReaderViewModel.class);
        } else if(mMode == BookReaderViewModel.Mode.MODE_LOCAL) {
            mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(LocalBookReaderViewModel.class);
        }
    }

    private void hideSystemBars() {
        Window window = getActivity().getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void showSystemBars() {
        Window window = getActivity().getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        controller.show(WindowInsetsCompat.Type.systemBars());
    }

    private void registerSystemBarsListener(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            private void setMargin(View view, int top, int bottom) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                marginLayoutParams.topMargin = top;
                marginLayoutParams.bottomMargin = bottom;
                view.setLayoutParams(marginLayoutParams);
            }

            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsets) {
                if(mViewModel.getIsFullscreen()) {
                    setMargin(getActivity().findViewById(R.id.book_reader_content_controls), 0, 0);
                } else {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    setMargin(getActivity().findViewById(R.id.book_reader_content_controls), insets.top, insets.bottom);
                }

                return WindowInsetsCompat.CONSUMED;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mViewModel.setIsFullscreen(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGestureDetector = new GestureDetector(getActivity(), new BookReaderTouchListener());

        mPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0);
        int viewModeInt = mPreferences.getInt(
                Constants.SETTINGS_PAGE_VIEW_MODE,
                Constants.PageViewMode.ASPECT_FIT.native_int);
        mPageViewMode = Constants.PageViewMode.values()[viewModeInt];
        mIsLeftToRight = mPreferences.getBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, true);

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_book_reader, container, false);

        mPageNavLayout = (LinearLayout) getActivity().findViewById(R.id.pageNavLayout);
        mPageNavSeekBar = (SeekBar) mPageNavLayout.findViewById(R.id.pageNavSeekBar);
        mPageNavSeekBar.setMax(0);
        mPageNavSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int page;
                    if (mIsLeftToRight) {
                        page = progress + 1;
                    } else {
                        page = mPageNavSeekBar.getMax() - progress + 1;
                    }

                    if(page != getCurrentPage()) {
                        setCurrentPage(page, true);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mViewModel.getPicasso().pauseTag(getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mViewModel.getPicasso().resumeTag(getActivity());
            }
        });
        mPageNavTextView = (TextView) mPageNavLayout.findViewById(R.id.pageNavTextView);
        mViewPager = (BookViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new BookReaderPagerAdapter());
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnTouchListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int page;
                if(mIsLeftToRight) {
                    page = position + 1;
                } else {
                    page = mViewPager.getAdapter().getCount() - position;
                }

                if(page != mViewModel.getSelectedBookPage()) {
                    setCurrentPage(page, true);
                }
            }
        });
        mViewPager.setOnSwipeOutListener(new BookViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                if (mIsLeftToRight) {
                    hitBeginning();
                } else {
                    hitEnding();
                }
            }

            @Override
            public void onSwipeOutAtEnd() {
                if (mIsLeftToRight) {
                    hitEnding();
                } else {
                    hitBeginning();
                }
            }
        });

        mViewModel.getBookObservable().observe(getViewLifecycleOwner(), new Observer<BookDto>() {
            @Override
            public void onChanged(BookDto bookDto) {
                mViewPager.getAdapter().notifyDataSetChanged();

                getActivity().setTitle(bookDto.getName());

                mPageNavSeekBar.setMax(bookDto.getNumberOfPages() - 1);

                updatePageNavSeekBar();

                int page = mViewModel.getSelectedBookPage();

                setCurrentPage(page, false);
            }
        });
        mViewModel.getBookMarkObservable().observe(getViewLifecycleOwner(), new Observer<BookMarkDto>() {
            @Override
            public void onChanged(BookMarkDto bookMarkDto) {
                ((BookReaderActivity) getActivity()).addUpdatedBook(mViewModel.getBook());
            }
        });
        mViewModel.getShowOpenSelectedBookDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showOpenSelectedBookDialog) {
                if(showOpenSelectedBookDialog) {
                    if(mOpenBookDialog == null) {
                        mViewModel.setIsFullscreen(false);

                        mOpenBookDialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.book_reader_dialog_open)
                                .setMessage(mViewModel.getSelectedBook().getName())
                                .setPositiveButton(R.string.book_reader_dialog_open_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BookReaderActivity activity = (BookReaderActivity) getActivity();
                                        if (mMode == BookReaderViewModel.Mode.MODE_REMOTE) {
                                            activity.setFragment(BookReaderFragment.create(mViewModel.getSelectedBook().getId()));
                                        } else if (mMode == BookReaderViewModel.Mode.MODE_LOCAL) {
                                            activity.setFragment(BookReaderFragment.create(mViewModel.getSelectedBook().getPath()));
                                        }
                                        mViewModel.setShowOpenSelectedBookDialog(false);
                                    }
                                })
                                .setNegativeButton(R.string.book_reader_dialog_open_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowOpenSelectedBookDialog(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowOpenSelectedBookDialog(false);
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        mOpenBookDialog = null;
                                    }
                                })
                                .create();

                        mOpenBookDialog.show();
                    }
                }
            }
        });
        mViewModel.getIsFullscreenObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isFullscreen) {
                ActionBar actionBar = getActionBar();

                if(isFullscreen) {
                    if (actionBar != null) {
                        actionBar.hide();
                    }

                    mPageNavLayout.setVisibility(View.INVISIBLE);

                    hideSystemBars();
                } else {
                    showSystemBars();

                    if (actionBar != null) {
                        actionBar.show();
                    }

                    mPageNavLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mViewModel.getShowMessageObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showMessage) {
                if(showMessage) {
                    mViewModel.setShowMessage(false);

                    ((BookReaderActivity) getActivity()).showMessage(mViewModel.getMessage());
                }
            }
        });

        registerSystemBarsListener(view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_reader, menu);

        switch (mPageViewMode) {
            case ASPECT_FILL:
                menu.findItem(R.id.view_mode_aspect_fill).setChecked(true);
                break;
            case ASPECT_FIT:
                menu.findItem(R.id.view_mode_aspect_fit).setChecked(true);
                break;
            case FIT_WIDTH:
                menu.findItem(R.id.view_mode_fit_width).setChecked(true);
                break;
        }

        if (mIsLeftToRight) {
            menu.findItem(R.id.reading_left_to_right).setChecked(true);
        } else {
            menu.findItem(R.id.reading_right_to_left).setChecked(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if(mOpenBookDialog != null) {
            mOpenBookDialog.dismiss();
        }

        super.onDestroyView();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = mPreferences.edit();
        switch(item.getItemId()) {
            case R.id.view_mode_aspect_fill:
            case R.id.view_mode_aspect_fit:
            case R.id.view_mode_fit_width:
                item.setChecked(true);
                mPageViewMode = RESOURCE_VIEW_MODE.get(item.getItemId());
                editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mPageViewMode.native_int);
                editor.apply();
                updatePageViews(mViewPager);
                break;
            case R.id.reading_left_to_right:
            case R.id.reading_right_to_left:
                item.setChecked(true);
                int page = getCurrentPage();
                mIsLeftToRight = (item.getItemId() == R.id.reading_left_to_right);
                editor.putBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, mIsLeftToRight);
                editor.apply();
                setCurrentPage(page, false);
                mViewPager.getAdapter().notifyDataSetChanged();
                updatePageNavSeekBar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public int getCurrentPage() {
        if(mIsLeftToRight) {
            return mViewPager.getCurrentItem() + 1;
        } else {
            return mViewPager.getAdapter().getCount() - mViewPager.getCurrentItem();
        }
    }

    private void setCurrentPage(int page, boolean animated) {
        if(mIsLeftToRight) {
            mViewPager.setCurrentItem(page - 1, animated);
            mPageNavSeekBar.setProgress(page - 1);
        } else {
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - page, animated);
            mPageNavSeekBar.setProgress(mViewPager.getAdapter().getCount() - page);
        }

        String navPage = new StringBuilder()
                .append(page).append("/").append(mViewModel.getBook().getNumberOfPages())
                .toString();

        mPageNavTextView.setText(navPage);

        mViewModel.setSelectedBookPage(page);
        mViewModel.addBookMark();
    }

    private class BookReaderPagerAdapter extends PagerAdapter {
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if(mViewModel.getBook() == null) {
                return 0;
            } else {
                return mViewModel.getBook().getNumberOfPages();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = (LayoutInflater)getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.fragment_book_reader_page, container, false);

            PageImageView pageImageView = (PageImageView) layout.findViewById(R.id.pageImageView);
            if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL) {
                pageImageView.setTranslateToRightEdge(!mIsLeftToRight);
            }
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnTouchListener(BookReaderFragment.this);

            container.addView(layout);

            BookReaderTarget t = new BookReaderTarget(layout, position);
            loadImage(t);
            mTargets.put(position, t);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;

            mViewModel.getPicasso().cancelRequest(mTargets.get(position));

            mTargets.delete(position);
            container.removeView(layout);
        }
    }

    private void loadImage(BookReaderTarget t) {
        int page;
        if (mIsLeftToRight) {
            page = t.position + 1;
        } else {
            page = mViewPager.getAdapter().getCount() - t.position;
        }

        Uri uri = mViewModel.getBookPageUri(page);
        mViewModel.getPicasso().load(uri)
                .tag(getActivity())
                .resize(Constants.MAX_PAGE_WIDTH, Constants.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .into(t);
    }

    public class BookReaderTarget implements Target, View.OnClickListener {
        private WeakReference<View> mLayout;
        public final int position;

        public BookReaderTarget(View layout, int position) {
            mLayout = new WeakReference<>(layout);
            this.position = position;
        }

        private void setVisibility(int imageView, int progressBar, int reloadButton) {
            View layout = mLayout.get();
            layout.findViewById(R.id.pageImageView).setVisibility(imageView);
            layout.findViewById(R.id.pageProgressBar).setVisibility(progressBar);
            layout.findViewById(R.id.reloadButton).setVisibility(reloadButton);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            View layout = mLayout.get();
            if (layout == null) {
                return;
            }

            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            View layout = mLayout.get();
            if (layout == null) {
                return;
            }

            setVisibility(View.GONE, View.GONE, View.VISIBLE);

            ImageButton ib = (ImageButton) layout.findViewById(R.id.reloadButton);
            ib.setOnClickListener(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

        @Override
        public void onClick(View v) {
            View layout = mLayout.get();
            if (layout == null) {
                return;
            }

            setVisibility(View.GONE, View.VISIBLE, View.GONE);
            loadImage(this);
        }
    }

    private class BookReaderTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(!mViewModel.getIsFullscreen()) {
                mViewModel.setIsFullscreen(true);
                return true;
            }

            float x = e.getX();

            // tap left edge
            if (x < (float) mViewPager.getWidth() / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1) {
                        hitBeginning();
                    } else {
                        setCurrentPage(getCurrentPage() - 1, true);
                    }
                } else {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount()) {
                        hitEnding();
                    } else {
                        setCurrentPage(getCurrentPage() + 1, true);
                    }
                }
            }
            // tap right edge
            else if (x > (float) mViewPager.getWidth() / 3 * 2) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount()) {
                        hitEnding();
                    } else {
                        setCurrentPage(getCurrentPage() + 1, true);
                    }
                } else {
                    if (getCurrentPage() == 1) {
                        hitBeginning();
                    } else {
                        setCurrentPage(getCurrentPage() - 1, true);
                    }
                }
            } else {
                mViewModel.setIsFullscreen(false);
            }

            return true;
        }
    }

    private void updatePageViews(ViewGroup parentView) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            final View child = parentView.getChildAt(i);
            if (child instanceof ViewGroup) {
                updatePageViews((ViewGroup)child);
            } else if (child instanceof PageImageView) {
                PageImageView view = (PageImageView) child;
                if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL) {
                    view.setTranslateToRightEdge(!mIsLeftToRight);
                }
                view.setViewMode(mPageViewMode);
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    private void hitBeginning() {
        if (mViewModel.getBookLinkable() != null) {
            BookDto previousBookDto = mViewModel.getBookLinkable().getPreviousElement();

            if(previousBookDto != null) {
                mViewModel.setSelectedBook(previousBookDto);
                mViewModel.setShowOpenSelectedBookDialog(true);
            }
        }
    }

    private void hitEnding() {
        if (mViewModel.getBookLinkable() != null) {
            BookDto nextBookDto = mViewModel.getBookLinkable().getNextElement();

            if(nextBookDto != null) {
                mViewModel.setSelectedBook(nextBookDto);
                mViewModel.setShowOpenSelectedBookDialog(true);
            }
        }
    }

    private void updatePageNavSeekBar() {
        int seekRes = (mIsLeftToRight)
                ? R.drawable.reader_nav_progress
                : R.drawable.reader_nav_progress_inverse;

        Drawable d = ContextCompat.getDrawable(getActivity(), seekRes);
        Rect bounds = mPageNavSeekBar.getProgressDrawable().getBounds();
        mPageNavSeekBar.setProgressDrawable(d);
        mPageNavSeekBar.getProgressDrawable().setBounds(bounds);
    }
}
