package com.gitlab.jeeto.oboco.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.fragment.BookReaderFragment;
import com.gitlab.jeeto.oboco.fragment.BookReaderViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BookReaderActivity extends BaseActivity {
    private List<BookDto> mUpdatedBookListDto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        mUpdatedBookListDto = new ArrayList<BookDto>();

        setContentView(R.layout.layout_book_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pageToolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            BookReaderViewModel.Mode mode = (BookReaderViewModel.Mode) extras.getSerializable(BookReaderViewModel.PARAM_MODE);

            BookReaderFragment fragment = null;
            if (mode == BookReaderViewModel.Mode.MODE_REMOTE) {
                Long bookId = extras.getLong(BookReaderViewModel.PARAM_BOOK_ID);

                fragment = BookReaderFragment.create(bookId);
            } else if (mode == BookReaderViewModel.Mode.MODE_LOCAL) {
                String bookPath = extras.getString(BookReaderViewModel.PARAM_BOOK_PATH);

                fragment = BookReaderFragment.create(bookPath);
            }
            setFragment(fragment);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus) {
            Window window = getWindow();
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.book_reader_content, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra("updatedBookList", (Serializable) mUpdatedBookListDto);

                setResult(Activity.RESULT_OK, data);

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // do not call super.onBackPressed(), the resultCode will be Activity.RESULT_CANCELED
        //super.onBackPressed();
        Intent data = new Intent();
        data.putExtra("updatedBookList", (Serializable) mUpdatedBookListDto);

        setResult(Activity.RESULT_OK, data);

        finish();
    }

    public void addUpdatedBook(BookDto bookDto) {
        int index = 0;

        while(index < mUpdatedBookListDto.size()) {
            BookDto updatedBookDto = mUpdatedBookListDto.get(index);
            if(updatedBookDto.equals(bookDto)) {
                mUpdatedBookListDto.set(index, bookDto);

                break;
            }

            index = index + 1;
        }

        if(index == mUpdatedBookListDto.size()) {
            mUpdatedBookListDto.add(bookDto);
        }
    }

    public View getMessageView() {
        return findViewById(R.id.book_reader_content2);
    }
}
