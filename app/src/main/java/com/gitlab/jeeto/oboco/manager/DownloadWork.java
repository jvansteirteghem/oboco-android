package com.gitlab.jeeto.oboco.manager;

import androidx.work.WorkInfo;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class DownloadWork implements Comparable<DownloadWork> {
    private WorkInfo mWorkInfo;
    private DownloadWorkType mType;
    private Date mCreateDate;
    private Long mDownloadId;
    private String mDownloadName;

    public DownloadWork(WorkInfo workInfo) {
        mWorkInfo = workInfo;

        Set<String> tagList = workInfo.getTags();
        for(String tag: tagList) {
            if(tag.startsWith("type:")) {
                String type = tag.substring(5);

                mType = DownloadWorkType.valueOf(type);
            } else {
                if(tag.startsWith("createDate:")) {
                    String createDate = tag.substring(11);

                    mCreateDate = new Date(Long.valueOf(createDate));
                } else {
                    if(tag.startsWith("downloadId:")) {
                        String downloadId = tag.substring(11);

                        mDownloadId = Long.valueOf(downloadId);
                    } else {
                        if(tag.startsWith("downloadName:")) {
                            String downloadName = tag.substring(13);

                            mDownloadName = downloadName;
                        }
                    }
                }
            }
        }
    }
    public UUID getId() {
        return mWorkInfo.getId();
    }

    public WorkInfo.State getState() {
        return mWorkInfo.getState();
    }

    public DownloadWorkType getType() {
        return mType;
    }

    public Date getCreateDate() {
        return mCreateDate;
    }

    public Long getDownloadId() {
        return mDownloadId;
    }

    public String getDownloadName() {
        return mDownloadName;
    }

    @Override
    public int compareTo(DownloadWork downloadWork) {
        return mCreateDate.compareTo(downloadWork.getCreateDate()) * -1;
    }
}
