package com.gitlab.jeeto.oboco.manager;

import android.os.Bundle;

public abstract class AccountLoginManager {
    public abstract void create(Bundle savedInstanceState);
    public abstract void destroy();
    public abstract void saveInstanceState(Bundle outState);
    public abstract void load();
    public abstract void login(String baseUrl, String name, String password);
}
