package com.gitlab.jeeto.oboco.manager;

import androidx.work.WorkInfo;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class DownloadWork implements Comparable<DownloadWork> {
    private WorkInfo mWorkInfo;
    private DownloadWorkType mType;
    private String mName;
    private Date mCreateDate;

    public DownloadWork(WorkInfo workInfo) {
        mWorkInfo = workInfo;

        Set<String> tagList = workInfo.getTags();
        for(String tag: tagList) {
            if(tag.startsWith("type:")) {
                String type = tag.substring(5);

                mType = DownloadWorkType.valueOf(type);
            } else {
                if(tag.startsWith("name:")) {
                    String name = tag.substring(5);

                    mName = name;
                } else {
                    if(tag.startsWith("createDate:")) {
                        String createDate = tag.substring(11);

                        mCreateDate = new Date(Long.valueOf(createDate));
                    }
                }
            }
        }
    }
    public UUID getId() {
        return mWorkInfo.getId();
    }

    public DownloadWorkType getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public Date getCreateDate() {
        return mCreateDate;
    }

    public WorkInfo.State getState() {
        return mWorkInfo.getState();
    }

    @Override
    public int compareTo(DownloadWork downloadWork) {
        return mCreateDate.compareTo(downloadWork.getCreateDate()) * -1;
    }
}
