package com.gitlab.jeeto.oboco.reader;

import java.io.IOException;
import java.io.InputStream;

public interface BookReader {
    public void create() throws IOException;
    public void destroy() throws IOException;
    public String getType();
    public InputStream getPage(int pageNumber) throws IOException;
    public Integer getNumberOfPages();
}
