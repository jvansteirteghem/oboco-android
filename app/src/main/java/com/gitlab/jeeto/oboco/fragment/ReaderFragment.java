package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.gitlab.jeeto.oboco.Constants;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.ReaderActivity;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.manager.LocalBookReaderManager;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.RemoteBookReaderManager;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.view.ComicViewPager;
import com.gitlab.jeeto.oboco.view.PageImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class ReaderFragment extends Fragment implements View.OnTouchListener {
    public static final String PARAM_MODE = "PARAM_MODE";
    public static final String PARAM_BOOK_ID = "PARAM_BOOK_ID";
    public static final String PARAM_BOOK_FILE = "PARAM_BOOK_FILE";
    public static final String STATE_FULLSCREEN = "STATE_FULLSCREEN";
    public static final String STATE_BOOK_PAGE = "STATE_BOOK_PAGE";

    private ComicViewPager mViewPager;
    private LinearLayout mPageNavLayout;
    private SeekBar mPageSeekBar;
    private TextView mPageNavTextView;
    private SharedPreferences mPreferences;
    private GestureDetector mGestureDetector;

    private final static HashMap<Integer, Constants.PageViewMode> RESOURCE_VIEW_MODE;
    private boolean mIsFullscreen;
    private int mCurrentPage;
    private Constants.PageViewMode mPageViewMode;
    private boolean mIsLeftToRight;

    private Picasso mPicasso;
    private SparseArray<ReaderTarget> mTargets = new SparseArray<>();

    private BookReaderManager mBookReaderManager;
    private OnErrorListener mOnErrorListener;

    private Mode mMode;
    private BookDto mBook;
    private List<BookDto> mBookList;

    public enum Mode {
        MODE_REMOTE,
        MODE_LOCAL;
    }

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    public static ReaderFragment create(Long bookId) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_REMOTE);
        args.putLong(PARAM_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReaderFragment create(File bookFile) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_LOCAL);
        args.putSerializable(PARAM_BOOK_FILE, bookFile);
        fragment.setArguments(args);
        return fragment;
    }

    public ReaderFragment() {
        super();
    }

    public BookDto getBook() {
        return mBook;
    }

    public void setBook(BookDto book) {
        this.mBook = book;
    }

    public List<BookDto> getBookList() {
        return mBookList;
    }

    public void setBookList(List<BookDto> bookList) {
        this.mBookList = bookList;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        mMode = (Mode) bundle.getSerializable(PARAM_MODE);

        if(mMode == Mode.MODE_REMOTE) {
            Long bookId = bundle.getLong(ReaderFragment.PARAM_BOOK_ID);

            mBookReaderManager = new RemoteBookReaderManager(bookId);
        } else if(mMode == Mode.MODE_LOCAL) {
            File bookFile = (File) bundle.getSerializable(ReaderFragment.PARAM_BOOK_FILE);

            mBookReaderManager = new LocalBookReaderManager(bookFile);
        }
        mBookReaderManager.create(this);

        mCurrentPage = 1;

        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mBookReaderManager.getBookPageRequestHandler())
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

    public void loadBook() {
        mViewPager.getAdapter().notifyDataSetChanged();

        FragmentActivity fragmentActivity = getActivity();

        if(fragmentActivity != null) {
            fragmentActivity.setTitle(mBook.getName());

            mPageSeekBar.setMax(mBook.getNumberOfPages() - 1);

            updateSeekBar();

            setCurrentPage(mCurrentPage);

            if (mBook.getBookMark() != null && mBook.getBookMark().getPage() != mCurrentPage) {
                AlertDialog dialog = new AlertDialog.Builder(fragmentActivity, R.style.AppCompatAlertDialogStyle)
                        .setTitle("Would you like to switch to the bookmarked page?")
                        .setMessage(mBook.getBookMark().getPage().toString())
                        .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setCurrentPage(mBook.getBookMark().getPage());
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGestureDetector = new GestureDetector(getActivity(), new ReaderTouchListener());

        mPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0);
        int viewModeInt = mPreferences.getInt(
                Constants.SETTINGS_PAGE_VIEW_MODE,
                Constants.PageViewMode.ASPECT_FIT.native_int);
        mPageViewMode = Constants.PageViewMode.values()[viewModeInt];
        mIsLeftToRight = mPreferences.getBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, true);

        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mPageNavLayout = (LinearLayout) getActivity().findViewById(R.id.pageNavLayout);
        mPageSeekBar = (SeekBar) mPageNavLayout.findViewById(R.id.pageSeekBar);
        mPageSeekBar.setMax(0);
        mPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int page;
                    if (mIsLeftToRight) {
                        page = progress + 1;
                    } else {
                        page = mPageSeekBar.getMax() - progress + 1;
                    }

                    if(page != mCurrentPage) {
                        setCurrentPage(page);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPicasso.pauseTag(getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPicasso.resumeTag(getActivity());
            }
        });
        mPageNavTextView = (TextView) mPageNavLayout.findViewById(R.id.pageNavTextView);
        mViewPager = (ComicViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new ComicPagerAdapter());
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

                if(page != mCurrentPage) {
                    setCurrentPage(page);
                }
            }
        });
        mViewPager.setOnSwipeOutListener(new ComicViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                if (mIsLeftToRight)
                    hitBeginning();
                else
                    hitEnding();
            }

            @Override
            public void onSwipeOutAtEnd() {
                if (mIsLeftToRight)
                    hitEnding();
                else
                    hitBeginning();
            }
        });

        if (savedInstanceState != null) {
            boolean fullscreen = savedInstanceState.getBoolean(STATE_FULLSCREEN);
            setFullscreen(fullscreen);

            mCurrentPage = savedInstanceState.getInt(STATE_BOOK_PAGE);
        }
        else {
            setFullscreen(true);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);

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
        }
        else {
            menu.findItem(R.id.reading_right_to_left).setChecked(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_FULLSCREEN, isFullscreen());
        outState.putInt(STATE_BOOK_PAGE, mCurrentPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mBook == null) {
            mBookReaderManager.loadBook();
        } else {
            loadBook();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // https://github.com/square/picasso/issues/445
        //mPicasso.shutdown();

        mBookReaderManager.destroy();

        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public int getCurrentPage() {
        if (mIsLeftToRight)
            return mViewPager.getCurrentItem() + 1;
        else
            return mViewPager.getAdapter().getCount() - mViewPager.getCurrentItem();
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
                updateSeekBar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCurrentPage(int page) {
        setCurrentPage(page, true);
    }

    private void setCurrentPage(int page, boolean animated) {
        mCurrentPage = page;

        if (mIsLeftToRight) {
            mViewPager.setCurrentItem(page - 1);
            mPageSeekBar.setProgress(page - 1);
        }
        else {
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - page, animated);
            mPageSeekBar.setProgress(mViewPager.getAdapter().getCount() - page);
        }

        String navPage = new StringBuilder()
                .append(page).append("/").append(mBook.getNumberOfPages())
                .toString();

        mPageNavTextView.setText(navPage);

        if(page > 1) {
            mBookReaderManager.saveBookMark(getCurrentPage());
        }
    }

    private class ComicPagerAdapter extends PagerAdapter {
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if(mBook == null) {
                return 0;
            } else {
                return mBook.getNumberOfPages();
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

            View layout = inflater.inflate(R.layout.fragment_reader_page, container, false);

            PageImageView pageImageView = (PageImageView) layout.findViewById(R.id.pageImageView);
            if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL)
                pageImageView.setTranslateToRightEdge(!mIsLeftToRight);
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnTouchListener(ReaderFragment.this);

            container.addView(layout);

            ReaderTarget t = new ReaderTarget(layout, position);
            loadImage(t);
            mTargets.put(position, t);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;

            mPicasso.cancelRequest(mTargets.get(position));

            mTargets.delete(position);
            container.removeView(layout);

            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bm = bd.getBitmap();
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
    }

    private void loadImage(ReaderTarget t) {
        int page;
        if (mIsLeftToRight) {
            page = t.position + 1;
        }
        else {
            page = mViewPager.getAdapter().getCount() - t.position;
        }

        Uri uri = mBookReaderManager.getBookPageRequestHandler().getPageUri(page);
        mPicasso.load(uri)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(getActivity())
                .resize(Constants.MAX_PAGE_WIDTH, Constants.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .into(t);
    }

    public class ReaderTarget implements Target, View.OnClickListener {
        private WeakReference<View> mLayout;
        public final int position;

        public ReaderTarget(View layout, int position) {
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
            if (layout == null)
                return;

            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            View layout = mLayout.get();
            if (layout == null)
                return;

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
            if (layout == null)
                return;

            setVisibility(View.GONE, View.VISIBLE, View.GONE);
            loadImage(this);
        }
    }

    private class ReaderTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isFullscreen()) {
                setFullscreen(true, true);
                return true;
            }

            float x = e.getX();

            // tap left edge
            if (x < (float) mViewPager.getWidth() / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1)
                        hitBeginning();
                    else
                        setCurrentPage(getCurrentPage() - 1);
                }
                else {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount())
                        hitEnding();
                    else
                        setCurrentPage(getCurrentPage() + 1);
                }
            }
            // tap right edge
            else if (x > (float) mViewPager.getWidth() / 3 * 2) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount())
                        hitEnding();
                    else
                        setCurrentPage(getCurrentPage() + 1);
                }
                else {
                    if (getCurrentPage() == 1)
                        hitBeginning();
                    else
                        setCurrentPage(getCurrentPage() - 1);
                }
            }
            else
                setFullscreen(false, true);

            return true;
        }
    }

    private void updatePageViews(ViewGroup parentView) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            final View child = parentView.getChildAt(i);
            if (child instanceof ViewGroup) {
                updatePageViews((ViewGroup)child);
            }
            else if (child instanceof PageImageView) {
                PageImageView view = (PageImageView) child;
                if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL)
                    view.setTranslateToRightEdge(!mIsLeftToRight);
                view.setViewMode(mPageViewMode);
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    private void setFullscreen(boolean fullscreen) {
        setFullscreen(fullscreen, false);
    }

    private void setFullscreen(boolean fullscreen, boolean animated) {
        mIsFullscreen = fullscreen;

        ActionBar actionBar = getActionBar();

        if (fullscreen) {
            if (actionBar != null) actionBar.hide();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.INVISIBLE);
        }
        else {
            if (actionBar != null) actionBar.show();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.VISIBLE);

            // status bar & navigation bar background won't show in some cases
            if (Utils.isLollipopOrLater()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Window w = getActivity().getWindow();
                        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    }
                }, 300);
            }
        }
    }

    private boolean isFullscreen() {
        return mIsFullscreen;
    }

    private int getBookListIndex() {
        int index = -1;
        for(int i = 0; i < mBookList.size(); i = i + 1) {
            BookDto book = mBookList.get(i);
            if(mBook.getId().equals(book.getId())) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void hitBeginning() {
        if (mBook != null) {
            int newIndex = getBookListIndex() - 1;
            if(newIndex >= 0) {
                BookDto newBook = mBookList.get(newIndex);

                confirmSwitch(newBook, R.string.switch_prev_comic);
            }
        }
    }

    private void hitEnding() {
        if (mBook != null) {
            int newIndex = getBookListIndex() + 1;
            if(newIndex < mBookList.size()) {
                BookDto newBook = mBookList.get(newIndex);

                confirmSwitch(newBook, R.string.switch_next_comic);
            }
        }
    }

    private void confirmSwitch(BookDto newBook, int titleRes) {
        if (newBook == null)
            return;

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(newBook.getName())
                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReaderActivity activity = (ReaderActivity) getActivity();
                        activity.setFragment(ReaderFragment.create(newBook.getId()));
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

    private void updateSeekBar() {
        int seekRes = (mIsLeftToRight)
                ? R.drawable.reader_nav_progress
                : R.drawable.reader_nav_progress_inverse;

        Drawable d = ContextCompat.getDrawable(getActivity(), seekRes);
        Rect bounds = mPageSeekBar.getProgressDrawable().getBounds();
        mPageSeekBar.setProgressDrawable(d);
        mPageSeekBar.getProgressDrawable().setBounds(bounds);
    }
}
