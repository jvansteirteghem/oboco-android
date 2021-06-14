package com.gitlab.jeeto.oboco.manager;

import android.os.Bundle;

public abstract class AccountLogoutManager {
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load();
    public abstract void logout();
    public abstract void updatePassword(String password, String updatePassword);
}
