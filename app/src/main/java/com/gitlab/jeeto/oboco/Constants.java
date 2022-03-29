package com.gitlab.jeeto.oboco;

public class Constants {
    public static final int COVER_THUMBNAIL_HEIGHT = 300;
    public static final int COVER_THUMBNAIL_WIDTH = 200;

    public static final int MAX_PAGE_HEIGHT = 1600;
    public static final int MAX_PAGE_WIDTH = 2000;

    public static final String SETTINGS_NAME = "BOOK_READER";

    public static final String SETTINGS_VIEW_MODE = "VIEW_MODE";
    public static final String SETTINGS_DIRECTION_IS_LEFT_TO_RIGHT = "DIRECTION_IS_LEFT_TO_RIGHT";

    public enum ViewMode {
        ASPECT_FILL(0),
        ASPECT_FIT(1),
        FIT_WIDTH(2);

        private ViewMode(int n) {
            native_int = n;
        }
        public final int native_int;
    }
}
