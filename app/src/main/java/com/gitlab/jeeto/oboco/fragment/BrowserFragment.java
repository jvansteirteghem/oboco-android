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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.LocalBookReaderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class BrowserFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {
    private final static String STATE_CURRENT_DIR = "stateCurrentDir";

    private ListView mListView;
    private File mCurrentDir;
    private File mRootDir;
    private File[] mSubdirs;
    private TextView mDirTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRootDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        mSubdirs = new File[]{};

        if (savedInstanceState != null) {
            mCurrentDir = (File) savedInstanceState.getSerializable(STATE_CURRENT_DIR);
        }
        else {
            mCurrentDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }

        getActivity().setTitle(R.string.menu_browser);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mDirTextView = (TextView) breadcrumbLayout.findViewById(R.id.dir_textview);

        mListView = (ListView) view.findViewById(R.id.listview_browser);
        mListView.setAdapter(new DirectoryAdapter());
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        setCurrentDir(mCurrentDir);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_CURRENT_DIR, mCurrentDir);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        setCurrentDir(mCurrentDir);
    }

    @Override
    public void onDestroyView() {
        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumb = (ViewGroup) toolbar.findViewById(R.id.breadcrumb_layout);
        toolbar.removeView(breadcrumb);
        super.onDestroyView();
    }

    private void setCurrentDir(File dir) {
        mCurrentDir = dir;
        ArrayList<File> subdirs = new ArrayList<>();
        if (!mCurrentDir.getAbsolutePath().equals(mRootDir.getAbsolutePath())) {
            subdirs.add(mCurrentDir.getParentFile());
        }
        File[] files = mCurrentDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() || Utils.isArchive(f.getName())) {
                    subdirs.add(f);
                }
            }
        }
        Collections.sort(subdirs, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((File) o).getName();
            }
        });
        mSubdirs = subdirs.toArray(new File[subdirs.size()]);

        if (mListView != null) {
            mListView.invalidateViews();
        }

        mDirTextView.setText(dir.getAbsolutePath());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = mSubdirs[position];
        if (file.isDirectory()) {
            setCurrentDir(file);
        } else {
            Intent intent = new Intent(getActivity(), BookReaderActivity.class);
            intent.putExtra(BookReaderManager.PARAM_MODE, BookReaderManager.Mode.MODE_LOCAL);
            intent.putExtra(LocalBookReaderManager.PARAM_BOOK_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        File file = mSubdirs[position];

        if(!file.getAbsolutePath().equals(mRootDir.getAbsolutePath())) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Would you like to delete the books?")
                    .setMessage(file.getName())
                    .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteFile(file);

                            setCurrentDir(mCurrentDir);
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

    private void setIcon(View convertView, File file) {
        ImageView view = (ImageView) convertView.findViewById(R.id.directory_row_icon);
        int colorRes = R.color.circle_grey;
        if (file.isDirectory()) {
            view.setImageResource(R.drawable.ic_folder_white_24dp);
        }
        else {
            view.setImageResource(R.drawable.ic_file_document_box_white_24dp);

            String name = file.getName();
            if (Utils.isZip(name)) {
                colorRes = R.color.circle_green;
            }
            else if (Utils.isRar(name)) {
                colorRes = R.color.circle_red;
            }
        }

        GradientDrawable shape = (GradientDrawable) view.getBackground();
        shape.setColor(getResources().getColor(colorRes));
    }

    private final class DirectoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSubdirs.length;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mSubdirs[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.row_directory, parent, false);
            }

            File file = mSubdirs[position];
            TextView textView = (TextView) convertView.findViewById(R.id.directory_row_text);

            if (position == 0 && !mCurrentDir.getAbsolutePath().equals(mRootDir.getAbsolutePath())) {
                textView.setText("..");
            }
            else {
                textView.setText(file.getName());
            }

            setIcon(convertView, file);

            return convertView;
        }
    }
}
