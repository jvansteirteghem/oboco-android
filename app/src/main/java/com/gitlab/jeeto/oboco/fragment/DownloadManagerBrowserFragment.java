package com.gitlab.jeeto.oboco.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.BookCollectionDto;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
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

    private OnErrorListener mOnErrorListener;

    private List<DownloadWork> mDownloadWorkList;

    private WorkManager mWorkManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWorkManager = WorkManager.getInstance(getContext().getApplicationContext());

        mDownloadWorkList = new ArrayList<DownloadWork>();

        getActivity().setTitle(R.string.menu_download_manager_browser);
    }

    @Override
    public void onDestroy() {
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
        if(mOnErrorListener != null) {
            mOnErrorListener.onError(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_browser, container, false);

        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumbLayout = (ViewGroup) inflater.inflate(R.layout.browser_breadcrumb, toolbar, false);
        toolbar.addView(breadcrumbLayout);
        mTitleTextView = (TextView) breadcrumbLayout.findViewById(R.id.browser_breadcrumb_title_textview);
        mTitleTextView.setText(R.string.menu_download_manager_browser);
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

                String numberOfDownloadsText = getResources().getQuantityString(R.plurals.number_of_downloads, numberOfDownloads, numberOfDownloads);

                mSubtitleTextView.setText(numberOfDownloadsText);
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        ViewGroup toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        ViewGroup breadcrumb = (ViewGroup) toolbar.findViewById(R.id.browser_breadcrumb_layout);
        toolbar.removeView(breadcrumb);
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

            textView.setText(downloadWork.getName());

            if(downloadWork.getState().equals(WorkInfo.State.SUCCEEDED) || downloadWork.getState().equals(WorkInfo.State.FAILED) || downloadWork.getState().equals(WorkInfo.State.CANCELLED)) {
                imageView.setImageResource(android.R.color.transparent);
                imageView.setOnClickListener(null);
            } else {
                imageView.setImageResource(R.drawable.outline_clear_black_24);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DownloadWork downloadWork = mDownloadWorkList.get(position);

                        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                                .setTitle("Would you like to stop the download?")
                                .setMessage(downloadWork.getName())
                                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mWorkManager.cancelWorkById(downloadWork.getId());
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
                });
            }

            setIcon(convertView, downloadWork);

            return convertView;
        }
    }
}
