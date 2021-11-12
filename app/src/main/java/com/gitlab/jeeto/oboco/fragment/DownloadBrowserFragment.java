package com.gitlab.jeeto.oboco.fragment;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.activity.BookReaderActivity;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.BookMarkDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;

public class DownloadBrowserFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private TextView mTitleTextView;
    private TextView mSubtitleTextView;

    private List<Object> mObjectList;

    private ActivityResultLauncher<Intent> mBookReaderActivityResultLauncher;

    private DownloadBrowserViewModel mViewModel;

    private AlertDialog mDeleteSelectedBookCollectionDialog;
    private AlertDialog mDeleteSelectedBookDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(DownloadBrowserViewModel.class);

        mObjectList = new ArrayList<Object>();

        mBookReaderActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK) {
                            List<BookDto> updatedBookListDto = (List<BookDto>) result.getData().getSerializableExtra("updatedBookList");

                            if(updatedBookListDto.size() != 0) {
                                BookCollectionDto currentBookCollectionDto = mViewModel.getCurrentBookCollection();

                                List<BookDto> bookListDto = currentBookCollectionDto.getBooks();

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

                                mViewModel.setCurrentBookCollection(currentBookCollectionDto);
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        getActivity().setTitle(R.string.drawer_menu_download_browser);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.browser_breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mTitleTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_title_textview);
        mTitleTextView.setText(R.string.drawer_menu_download_browser);
        mSubtitleTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_subtitle_textview);

        mListView = (ListView) view.findViewById(R.id.browser_listview);
        mListView.setAdapter(new BrowserAdapter());
        mListView.setOnItemClickListener(this);

        mViewModel.getCurrentBookCollectionObservable().observe(getViewLifecycleOwner(), new Observer<BookCollectionDto>() {
            @Override
            public void onChanged(BookCollectionDto currentBookCollectionDto) {
                mObjectList = new ArrayList<Object>();

                if(currentBookCollectionDto.getParentBookCollection() != null) {
                    mObjectList.add(currentBookCollectionDto.getParentBookCollection());
                }

                for(BookCollectionDto bookCollectionDto: currentBookCollectionDto.getBookCollections()) {
                    mObjectList.add(bookCollectionDto);
                }

                for(BookDto bookDto: currentBookCollectionDto.getBooks()) {
                    mObjectList.add(bookDto);
                }

                if (mListView != null) {
                    mListView.invalidateViews();
                }

                mSubtitleTextView.setText(currentBookCollectionDto.getPath());
            }
        });
        mViewModel.getShowDeleteSelectedBookCollectionDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showDeleteSelectedBookCollectionDialog) {
                if(showDeleteSelectedBookCollectionDialog) {
                    if (mDeleteSelectedBookCollectionDialog == null) {
                        mDeleteSelectedBookCollectionDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle(R.string.download_browser_dialog_delete_book_collection)
                                .setMessage(mViewModel.getSelectedBookCollection().getName())
                                .setPositiveButton(R.string.download_browser_dialog_delete_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.deleteSelectedBookCollection();
                                        mViewModel.setShowDeleteSelectedBookCollectionDialog(false);

                                        mDeleteSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.download_browser_dialog_delete_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDeleteSelectedBookCollectionDialog(false);

                                        mDeleteSelectedBookCollectionDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDeleteSelectedBookCollectionDialog(false);

                                        mDeleteSelectedBookCollectionDialog = null;
                                    }
                                })
                                .create();
                        mDeleteSelectedBookCollectionDialog.show();
                    }
                }
            }
        });
        mViewModel.getShowDeleteSelectedBookDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showDeleteSelectedBookDialog) {
                if(showDeleteSelectedBookDialog) {
                    if(mDeleteSelectedBookCollectionDialog == null) {
                        mDeleteSelectedBookDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle(R.string.download_browser_dialog_delete_book)
                                .setMessage(mViewModel.getSelectedBook().getName())
                                .setPositiveButton(R.string.download_browser_dialog_delete_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.deleteSelectedBook();
                                        mViewModel.setShowDeleteSelectedBookDialog(false);

                                        mDeleteSelectedBookDialog = null;
                                    }
                                })
                                .setNegativeButton(R.string.download_browser_dialog_delete_negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mViewModel.setShowDeleteSelectedBookDialog(false);

                                        mDeleteSelectedBookDialog = null;
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mViewModel.setShowDeleteSelectedBookDialog(false);

                                        mDeleteSelectedBookDialog = null;
                                    }
                                })
                                .create();
                        mDeleteSelectedBookDialog.show();
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
        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumb = (ViewGroup) toolbar.findViewById(R.id.browser_breadcrumb_layout);
        toolbar.removeView(breadcrumb);

        if(mDeleteSelectedBookCollectionDialog != null) {
            mDeleteSelectedBookCollectionDialog.dismiss();
            mDeleteSelectedBookCollectionDialog = null;
        }

        if(mDeleteSelectedBookDialog != null) {
            mDeleteSelectedBookDialog.dismiss();
            mDeleteSelectedBookDialog = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = mObjectList.get(position);

        if (object instanceof BookCollectionDto) {
            BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

            mViewModel.loadBookCollection(bookCollectionDto);
        } else {
            BookDto bookDto = (BookDto) object;

            Intent intent = new Intent(getActivity(), BookReaderActivity.class);
            intent.putExtra(BookReaderViewModel.PARAM_MODE, BookReaderViewModel.Mode.MODE_LOCAL);
            intent.putExtra(BookReaderViewModel.PARAM_BOOK_PATH, bookDto.getPath());
            mBookReaderActivityResultLauncher.launch(intent);
        }
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
                    colorRes = R.color.circle_blue;
                } else if(bookMarkDto.getPage() == bookDto.getNumberOfPages()) {
                    colorRes = R.color.circle_green;
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
            ImageView imageView = (ImageView) convertView.findViewById(R.id.browser_row_delete_icon);

            if(position == 0 && mViewModel.getCurrentBookCollection().getParentBookCollection() != null) {
                textView.setText("..");

                imageView.setImageResource(android.R.color.transparent);
                imageView.setOnClickListener(null);
            } else {
                if(object instanceof BookCollectionDto) {
                    BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

                    textView.setText(bookCollectionDto.getName());
                } else {
                    BookDto bookDto = (BookDto) object;

                    textView.setText(bookDto.getName());
                }

                imageView.setImageResource(R.drawable.outline_remove_black_24);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Object object = mObjectList.get(position);

                        if(object instanceof BookCollectionDto) {
                            BookCollectionDto bookCollectionDto = (BookCollectionDto) object;

                            if(!(position == 0 && mViewModel.getCurrentBookCollection().getParentBookCollection() != null)) {
                                mViewModel.setSelectedBookCollection(bookCollectionDto);
                                mViewModel.setShowDeleteSelectedBookCollectionDialog(true);
                            }
                        } else {
                            BookDto bookDto = (BookDto) object;

                            mViewModel.setSelectedBook(bookDto);
                            mViewModel.setShowDeleteSelectedBookDialog(true);
                        }
                    }
                });
            }

            setIcon(convertView, object);

            return convertView;
        }
    }
}
