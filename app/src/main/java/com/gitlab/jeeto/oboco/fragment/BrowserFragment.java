package com.gitlab.jeeto.oboco.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.BookDto;
import com.gitlab.jeeto.oboco.api.BookMarkDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.manager.BookReaderManager;
import com.gitlab.jeeto.oboco.manager.BrowserManager;
import com.gitlab.jeeto.oboco.manager.LocalBookReaderManager;
import com.gitlab.jeeto.oboco.manager.LocalBrowserManager;

import java.util.ArrayList;
import java.util.List;

public class BrowserFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView mListView;
    private TextView mFileTextView;

    private BrowserManager mBrowserManager;
    private OnErrorListener mOnErrorListener;

    private BookCollectionDto mCurrentBookCollectionDto;
    private List<Object> mObjectList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBrowserManager = new LocalBrowserManager(this);
        mBrowserManager.create(savedInstanceState);

        mObjectList = new ArrayList<Object>();

        getActivity().setTitle(R.string.menu_browser);
    }

    @Override
    public void onDestroy() {
        mBrowserManager.destroy();

        super.onDestroy();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.browser_breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mFileTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_textview);

        mListView = (ListView) view.findViewById(R.id.browser_listview);
        mListView.setAdapter(new BrowserAdapter());
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        mBrowserManager.load();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mBrowserManager.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumb = (ViewGroup) toolbar.findViewById(R.id.browser_breadcrumb_layout);
        toolbar.removeView(breadcrumb);
        super.onDestroyView();
    }

    public void onLoad(BookCollectionDto currentBookCollectionDto) {
        mCurrentBookCollectionDto = currentBookCollectionDto;

        mObjectList = new ArrayList<Object>();

        if(mCurrentBookCollectionDto.getParentBookCollection() != null) {
            mObjectList.add(mCurrentBookCollectionDto.getParentBookCollection());
        }

        for(BookCollectionDto bookCollectionDto: mCurrentBookCollectionDto.getBookCollections()) {
            mObjectList.add(bookCollectionDto);
        }

        for(BookDto bookDto: mCurrentBookCollectionDto.getBooks()) {
            mObjectList.add(bookDto);
        }

        if (mListView != null) {
            mListView.invalidateViews();
        }

        mFileTextView.setText(mCurrentBookCollectionDto.getPath());
    }

    public void onDeleteBook(BookDto bookDto) {
        mCurrentBookCollectionDto.getBooks().remove(bookDto);

        onLoad(mCurrentBookCollectionDto);
    }

    public void onDeleteBookCollection(BookCollectionDto bookCollectionDto) {
        mCurrentBookCollectionDto.getBookCollections().remove(bookCollectionDto);

        onLoad(mCurrentBookCollectionDto);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = mObjectList.get(position);

        if (object instanceof BookCollectionDto) {
            BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

            mBrowserManager.load(bookCollectionDto);
        } else {
            BookDto bookDto = (BookDto) object;

            Intent intent = new Intent(getActivity(), BookReaderActivity.class);
            intent.putExtra(BookReaderManager.PARAM_MODE, BookReaderManager.Mode.MODE_LOCAL);
            intent.putExtra(LocalBookReaderManager.PARAM_BOOK_PATH, bookDto.getPath());
            startActivityForResult(intent, 1);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            List<BookDto> updatedBookListDto = (List<BookDto>) data.getSerializableExtra("updatedBookList");

            if(updatedBookListDto.size() != 0) {
                List<BookDto> bookListDto = mCurrentBookCollectionDto.getBooks();

                int index = 0;

                while (index < bookListDto.size()) {
                    BookDto bookDto = bookListDto.get(index);

                    for (BookDto updatedBookDto : updatedBookListDto) {
                        if (bookDto.equals(updatedBookDto)) {
                            bookListDto.set(index, updatedBookDto);
                        }
                    }

                    index = index + 1;
                }

                onLoad(mCurrentBookCollectionDto);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = mObjectList.get(position);

        if(object instanceof BookCollectionDto) {
            BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

            if(!(position == 0 && mCurrentBookCollectionDto.getParentBookCollection() != null)) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Would you like to delete the books?")
                        .setMessage(bookCollectionDto.getName())
                        .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBrowserManager.deleteBookCollection(bookCollectionDto);
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
        } else {
            BookDto bookDto = (BookDto) object;

            AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Would you like to delete the book?")
                    .setMessage(bookDto.getName())
                    .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBrowserManager.deleteBook(bookDto);
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

    private void setIcon(View convertView, Object object) {
        ImageView view = (ImageView) convertView.findViewById(R.id.browser_row_icon);
        int colorRes = R.color.circle_grey;
        if (object instanceof BookCollectionDto) {
            view.setImageResource(R.drawable.ic_folder_white_24dp);
        }
        else {
            view.setImageResource(R.drawable.ic_file_document_box_white_24dp);

            BookDto bookDto = (BookDto) object;
            BookMarkDto bookMarkDto = bookDto.getBookMark();

            if(bookMarkDto != null) {
                if(bookMarkDto.getPage() > 1 && bookMarkDto.getPage() < bookDto.getNumberOfPages()) {
                    colorRes = R.color.circle_green;
                } else if(bookMarkDto.getPage() == bookDto.getNumberOfPages()) {
                    colorRes = R.color.circle_red;
                }
            }
        }

        GradientDrawable shape = (GradientDrawable) view.getBackground();
        shape.setColor(ContextCompat.getColor(getContext(), colorRes));
    }

    private final class BrowserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mObjectList.size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mObjectList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.browser_row, parent, false);
            }

            Object object = mObjectList.get(position);

            TextView textView = (TextView) convertView.findViewById(R.id.browser_row_text);

            if(position == 0 && mCurrentBookCollectionDto.getParentBookCollection() != null) {
                textView.setText("..");
            } else {
                if(object instanceof BookCollectionDto) {
                    BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

                    textView.setText(bookCollectionDto.getName());
                } else {
                    BookDto bookDto = (BookDto) object;

                    textView.setText(bookDto.getName());
                }
            }

            setIcon(convertView, object);

            return convertView;
        }
    }
}
