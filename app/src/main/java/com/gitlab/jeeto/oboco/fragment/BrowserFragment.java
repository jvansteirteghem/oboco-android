package com.gitlab.jeeto.oboco.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.database.AppDatabase;
import com.gitlab.jeeto.oboco.database.Book;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.LocalBookReaderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BrowserFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private final static String STATE_CURRENT_FILE = "STATE_CURRENT_FILE";

    private ListView mListView;
    private File mCurrentFile;
    private File mRootFile;
    private File[] mFiles;
    private TextView mFileTextView;

    private AppDatabase mAppDatabase;
    private Book[] mBooks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootFile = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        mFiles = new File[]{};

        if (savedInstanceState != null) {
            mCurrentFile = (File) savedInstanceState.getSerializable(STATE_CURRENT_FILE);
        }
        else {
            mCurrentFile = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }

        getActivity().setTitle(R.string.menu_browser);

        mAppDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), AppDatabase.class, "database").build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.browser_breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mFileTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_textview);

        mListView = (ListView) view.findViewById(R.id.browser_listview);
        mListView.setAdapter(new DirectoryAdapter());
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        setCurrentFile(mCurrentFile);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_CURRENT_FILE, mCurrentFile);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        setCurrentFile(mCurrentFile);
    }

    @Override
    public void onDestroyView() {
        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumb = (ViewGroup) toolbar.findViewById(R.id.browser_breadcrumb_layout);
        toolbar.removeView(breadcrumb);
        super.onDestroyView();
    }

    private void setCurrentFile(File currentFile) {
        mCurrentFile = currentFile;
        List<File> fileList = new ArrayList<File>();
        if (!mCurrentFile.getAbsolutePath().equals(mRootFile.getAbsolutePath())) {
            fileList.add(mCurrentFile.getParentFile());
        }
        File[] files = mCurrentFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || Utils.isArchive(file.getName())) {
                    fileList.add(file);
                }
            }
        }
        Collections.sort(fileList, new NaturalOrderComparator<File>() {
            @Override
            public String toString(File o) {
                return o.getName();
            }
        });
        mFiles = fileList.toArray(new File[fileList.size()]);

        mBooks = new Book[mFiles.length];

        Single<List<Book>> single = mAppDatabase.bookDao().findByBookCollectionPath(currentFile.getAbsolutePath());
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<List<Book>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<Book> bookList) {
                for(int index = 0; index < mFiles.length; index = index + 1) {
                    File file = mFiles[index];

                    mBooks[index] = null;
                    for(Book book: bookList) {
                        if(file.getAbsolutePath().equals(book.path)) {
                            mBooks[index] = book;

                            break;
                        }
                    }
                }

                if (mListView != null) {
                    mListView.invalidateViews();
                }

                mFileTextView.setText(currentFile.getAbsolutePath());
            }

            @Override
            public void onError(Throwable e) {
                if (mListView != null) {
                    mListView.invalidateViews();
                }

                mFileTextView.setText(currentFile.getAbsolutePath());
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = mFiles[position];

        if (file.isDirectory()) {
            setCurrentFile(file);
        } else {
            Intent intent = new Intent(getActivity(), BookReaderActivity.class);
            intent.putExtra(BookReaderManager.PARAM_MODE, BookReaderManager.Mode.MODE_LOCAL);
            intent.putExtra(LocalBookReaderManager.PARAM_BOOK_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File file = mFiles[position];

        if(!file.getAbsolutePath().equals(mRootFile.getAbsolutePath())) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Would you like to delete the books?")
                    .setMessage(file.getName())
                    .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteFile(file);

                            Book book = mBooks[position];

                            if(book != null) {
                                Completable complatable = mAppDatabase.bookDao().delete(book);
                                complatable = complatable.observeOn(AndroidSchedulers.mainThread());
                                complatable = complatable.subscribeOn(Schedulers.io());
                                complatable.subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(Disposable disposable) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        setCurrentFile(mCurrentFile);
                                    }

                                    @Override
                                    public void onError(Throwable throwable) {
                                        setCurrentFile(mCurrentFile);
                                    }
                                });
                            }
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

        return true;
    }

    private boolean deleteFile(File parentFile) {
        File[] files = parentFile.listFiles();
        if(files != null) {
            for(File file : files) {
                deleteFile(file);
            }
        }
        return parentFile.delete();
    }

    private void setIcon(View convertView, File file, Book book) {
        ImageView view = (ImageView) convertView.findViewById(R.id.browser_row_icon);
        int colorRes = R.color.circle_grey;
        if (file.isDirectory()) {
            view.setImageResource(R.drawable.ic_folder_white_24dp);
        }
        else {
            view.setImageResource(R.drawable.ic_file_document_box_white_24dp);

            if(book != null) {
                if(book.page > 1 && book.page < book.numberOfPages) {
                    colorRes = R.color.circle_green;
                } else if(book.page == book.numberOfPages) {
                    colorRes = R.color.circle_red;
                }
            }
        }

        GradientDrawable shape = (GradientDrawable) view.getBackground();
        shape.setColor(ContextCompat.getColor(getContext(), colorRes));
    }

    private final class DirectoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mFiles.length;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mFiles[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.browser_row, parent, false);
            }

            File file = mFiles[position];

            TextView textView = (TextView) convertView.findViewById(R.id.browser_row_text);

            if (position == 0 && !mCurrentFile.getAbsolutePath().equals(mRootFile.getAbsolutePath())) {
                textView.setText("..");
            }
            else {
                textView.setText(file.getName());
            }

            Book book = mBooks[position];

            setIcon(convertView, file, book);

            return convertView;
        }
    }
}
