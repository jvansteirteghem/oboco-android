package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.WorkRequest;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.common.BaseViewModelProviderFactory;
import com.gitlab.jeeto.oboco.manager.DownloadBookCollectionWorker;
import com.gitlab.jeeto.oboco.manager.DownloadBookWorker;
import com.gitlab.jeeto.oboco.manager.DownloadWork;
import com.gitlab.jeeto.oboco.manager.DownloadWorkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DownloadManagerBrowserFragment extends Fragment {
    private ListView mListView;
    private TextView mTitleTextView;
    private TextView mSubtitleTextView;

    private DownloadManagerBrowserViewModel mViewModel;

    private PopupMenu mSelectedDownloadWorkMenu;
    private AlertDialog mStartSelectedDownloadWorkDialog;
    private AlertDialog mStopSelectedDownloadWorkDialog;

    private List<DownloadWork> mDownloadWorkList;

    private WorkManager mWorkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new BaseViewModelProviderFactory(getActivity().getApplication(), getArguments())).get(DownloadManagerBrowserViewModel.class);

        mWorkManager = WorkManager.getInstance(getContext().getApplicationContext());

        mDownloadWorkList = new ArrayList<DownloadWork>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        getActivity().setTitle(R.string.drawer_menu_download_manager_browser);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.browser_breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mTitleTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_title_textview);
        mTitleTextView.setText(R.string.drawer_menu_download_manager_browser);
        mSubtitleTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_subtitle_textview);

        mListView = (ListView) view.findViewById(R.id.browser_listview);
        mListView.setAdapter(new BrowserAdapter());

        WorkQuery workQuery = WorkQuery.Builder
                .fromTags(Arrays.asList("download"))
                .build();

        mWorkManager.getWorkInfosLiveData(workQuery).observe(getViewLifecycleOwner(), new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfoList) {
                mDownloadWorkList = new ArrayList<DownloadWork>();
                for(WorkInfo workInfo: workInfoList) {
                    DownloadWork downloadWork = new DownloadWork(workInfo);

                    mDownloadWorkList.add(downloadWork);
                }

                Collections.sort(mDownloadWorkList);

                if (mListView != null) {
                    mListView.invalidateViews();
                }

                Integer numberOfDownloads = mDownloadWorkList.size();

                String numberOfDownloadsText = getResources().getQuantityString(R.plurals.download_manager_browser_number_of_downloads, numberOfDownloads, numberOfDownloads);

                mSubtitleTextView.setText(numberOfDownloadsText);
            }
        });

        mViewModel.getShowStartSelectedDownloadWorkDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showStartSelectedDownloadWorkDialog) {
                if(mStartSelectedDownloadWorkDialog == null) {
                    mStartSelectedDownloadWorkDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setTitle(R.string.download_manager_browser_dialog_download_start)
                            .setMessage(mViewModel.getSelectedDownloadWork().getDownloadName())
                            .setPositiveButton(R.string.download_manager_browser_dialog_download_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    WorkRequest downloadWorkRequest;

                                    if (mViewModel.getSelectedDownloadWork().getType().equals(DownloadWorkType.BOOK)) {
                                        downloadWorkRequest = DownloadBookWorker.createDownloadWorkRequest(mViewModel.getSelectedDownloadWork().getDownloadId(), mViewModel.getSelectedDownloadWork().getDownloadName());
                                    } else {
                                        downloadWorkRequest = DownloadBookCollectionWorker.createDownloadWorkRequest(mViewModel.getSelectedDownloadWork().getDownloadId(), mViewModel.getSelectedDownloadWork().getDownloadName());
                                    }

                                    WorkManager
                                            .getInstance(getContext().getApplicationContext())
                                            .enqueue(downloadWorkRequest);

                                    mViewModel.setShowStartSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setNegativeButton(R.string.download_manager_browser_dialog_download_negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mViewModel.setShowStartSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mViewModel.setShowStartSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    mStartSelectedDownloadWorkDialog = null;
                                }
                            })
                            .create();
                    mStartSelectedDownloadWorkDialog.show();
                }
            }
        });
        mViewModel.getShowStopSelectedDownloadWorkDialogObservable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showStopSelectedDownloadWorkDialog) {
                if(mStopSelectedDownloadWorkDialog == null) {
                    mStopSelectedDownloadWorkDialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setTitle(R.string.download_manager_browser_dialog_download_stop)
                            .setMessage(mViewModel.getSelectedDownloadWork().getDownloadName())
                            .setPositiveButton(R.string.download_manager_browser_dialog_download_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mWorkManager.cancelWorkById(mViewModel.getSelectedDownloadWork().getId());

                                    mViewModel.setShowStopSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setNegativeButton(R.string.download_manager_browser_dialog_download_negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mViewModel.setShowStopSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mViewModel.setShowStopSelectedDownloadWorkDialog(false);
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    mStopSelectedDownloadWorkDialog = null;
                                }
                            })
                            .create();
                    mStopSelectedDownloadWorkDialog.show();
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

        if(mSelectedDownloadWorkMenu != null) {
            mSelectedDownloadWorkMenu.dismiss();
        }

        if(mStartSelectedDownloadWorkDialog != null) {
            mStartSelectedDownloadWorkDialog.dismiss();
        }

        if(mStopSelectedDownloadWorkDialog != null) {
            mStopSelectedDownloadWorkDialog.dismiss();
        }

        super.onDestroyView();
    }

    private void setIcon(View convertView, DownloadWork downloadWork) {
        ImageView view = (ImageView) convertView.findViewById(R.id.browser_row_icon);

        int imageRes = R.drawable.ic_file_document_box_white_24dp;

        if(downloadWork.getType().equals(DownloadWorkType.BOOK_COLLECTION)) {
            imageRes = R.drawable.ic_folder_white_24dp;
        }

        view.setImageResource(imageRes);

        int colorRes = R.color.circle_grey;

        if(downloadWork.getState().equals(WorkInfo.State.SUCCEEDED)) {
            colorRes = R.color.circle_green;
        } else if(downloadWork.getState().equals(WorkInfo.State.FAILED)) {
            colorRes = R.color.circle_red;
        } else if(downloadWork.getState().equals(WorkInfo.State.CANCELLED)) {
            colorRes = R.color.circle_yellow;
        } else if(downloadWork.getState().equals(WorkInfo.State.RUNNING)) {
            colorRes = R.color.circle_blue;
        }

        GradientDrawable shape = (GradientDrawable) view.getBackground();
        shape.setColor(ContextCompat.getColor(getContext(), colorRes));
    }

    private final class BrowserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDownloadWorkList.size();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return mDownloadWorkList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.browser_row, parent, false);
            }

            DownloadWork downloadWork = mDownloadWorkList.get(position);

            TextView textView = (TextView) convertView.findViewById(R.id.browser_row_text);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.browser_row_delete_icon);

            textView.setText(downloadWork.getDownloadName());

            if(downloadWork.getState().equals(WorkInfo.State.SUCCEEDED)) {
                imageView.setImageResource(android.R.color.transparent);
                imageView.setOnClickListener(null);
            } else {
                imageView.setImageResource(R.drawable.ic_dots_vertical_black_24dp);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(mSelectedDownloadWorkMenu == null) {
                            DownloadWork downloadWork = mDownloadWorkList.get(position);

                            Context contextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.MyPopupMenuTheme);
                            mSelectedDownloadWorkMenu = new PopupMenu(contextThemeWrapper, imageView, Gravity.NO_GRAVITY, 0, R.style.MyPopupMenuOverflowTheme);
                            if(downloadWork.getState().equals(WorkInfo.State.FAILED) || downloadWork.getState().equals(WorkInfo.State.CANCELLED)) {
                                mSelectedDownloadWorkMenu.getMenu().add(Menu.NONE, R.id.menu_download_manager_browser_download_start, 1, R.string.download_manager_browser_menu_download_start);
                            } else {
                                mSelectedDownloadWorkMenu.getMenu().add(Menu.NONE, R.id.menu_download_manager_browser_download_stop, 1, R.string.download_manager_browser_menu_download_stop);
                            }
                            mSelectedDownloadWorkMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    mViewModel.setSelectedDownloadWork(downloadWork);

                                    switch (menuItem.getItemId()){
                                        case R.id.menu_download_manager_browser_download_start:
                                            mViewModel.setShowStartSelectedDownloadWorkDialog(true);
                                            return true;
                                        case R.id.menu_download_manager_browser_download_stop:
                                            mViewModel.setShowStopSelectedDownloadWorkDialog(true);
                                            return true;
                                    }

                                    return false;
                                }
                            });
                            mSelectedDownloadWorkMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                                @Override
                                public void onDismiss(PopupMenu menu) {
                                    mSelectedDownloadWorkMenu = null;
                                }
                            });
                            mSelectedDownloadWorkMenu.show();
                        }
                    }
                });
            }

            setIcon(convertView, downloadWork);

            return convertView;
        }
    }
}
