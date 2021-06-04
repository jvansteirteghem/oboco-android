package com.gitlab.jeeto.oboco.reader;

import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipBookReader implements BookReader {
    private File mFile;
    private ZipFile mZipFile;
    private ArrayList<ZipEntry> mZipEntries;

    public ZipBookReader(File file) {
        mFile = file;
    }

    @Override
    public void create() throws IOException {
        mZipFile = new ZipFile(mFile.getAbsolutePath());
        mZipEntries = new ArrayList<ZipEntry>();

        Enumeration<? extends ZipEntry> e = mZipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            if (!ze.isDirectory() && Utils.isImage(ze.getName())) {
                mZipEntries.add(ze);
            }
        }

        Collections.sort(mZipEntries, new NaturalOrderComparator<ZipEntry>() {
            @Override
            public String toString(ZipEntry o) {
                return o.getName();
            }
        });
    }

    @Override
    public Integer getNumberOfPages() {
        return mZipEntries.size();
    }

    @Override
    public InputStream getPage(int pageNumber) throws IOException {
        return mZipFile.getInputStream(mZipEntries.get(pageNumber - 1));
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public void destroy() throws IOException {
        mZipFile.close();
    }
}