package com.gitlab.jeeto.oboco.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.gitlab.jeeto.oboco.MainApplication;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.fragment.BookReaderFragment;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.LocalBookReaderManager;
import com.gitlab.jeeto.oboco.manager.RemoteBookReaderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BookReaderActivity extends AppCompatActivity implements OnErrorListener {
    private List<BookDto> mUpdatedBookListDto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpdatedBookListDto = new ArrayList<BookDto>();

        setContentView(R.layout.layout_book_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reader);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            BookReaderManager.Mode mode = (BookReaderManager.Mode) extras.getSerializable(BookReaderManager.PARAM_MODE);

            BookReaderFragment fragment = null;
            if (mode == BookReaderManager.Mode.MODE_REMOTE) {
                Long bookId = extras.getLong(RemoteBookReaderManager.PARAM_BOOK_ID);

                fragment = BookReaderFragment.create(bookId);
            } else if (mode == BookReaderManager.Mode.MODE_LOCAL) {
                String bookPath = extras.getString(LocalBookReaderManager.PARAM_BOOK_PATH);

                fragment = BookReaderFragment.create(bookPath);
            }
            setFragment(fragment);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame_reader, fragment)
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

    @Override
    public void onError(Throwable e) {
        MainApplication.handleError(this, e);
    }

    public void onAddBook(BookDto bookDto) {
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
}
