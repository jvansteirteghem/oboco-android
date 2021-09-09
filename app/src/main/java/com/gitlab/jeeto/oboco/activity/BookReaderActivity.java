package com.gitlab.jeeto.oboco.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.fragment.BookReaderFragment;
import com.gitlab.jeeto.oboco.fragment.BookReaderViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BookReaderActivity extends AppCompatActivity {
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
}
