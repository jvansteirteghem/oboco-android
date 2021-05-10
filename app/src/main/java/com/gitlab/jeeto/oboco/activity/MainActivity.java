package com.gitlab.jeeto.oboco.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gitlab.jeeto.oboco.MainApplication;
import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.OnErrorListener;
import com.gitlab.jeeto.oboco.fragment.AboutFragment;
import com.gitlab.jeeto.oboco.fragment.AccountLoginFragment;
import com.gitlab.jeeto.oboco.fragment.AccountLogoutFragment;
import com.gitlab.jeeto.oboco.fragment.LibraryFragment;
import com.gitlab.jeeto.oboco.managers.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, AccountLoginFragment.OnLoginListener, AccountLogoutFragment.OnLogoutListener, OnErrorListener {
    private final static String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentNavItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        setContentView(R.layout.layout_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (Utils.isLollipopOrLater()) {
            toolbar.setElevation(8);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupNavigationView(navigationView);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        SharedPreferences preferences = getSharedPreferences("application", Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            setTitle(R.string.menu_account);

            Intent intent = getIntent();
            Uri data = intent.getData();
            if (data != null) {
                String baseUrl = "";
                if(data.getScheme() != null) {
                    if(data.getScheme().equals("oboco")) {
                        baseUrl = "http://";
                    } else if(data.getScheme().equals("obocos")) {
                        baseUrl = "https://";
                    }
                }
                if(data.getHost() != null) {
                    baseUrl = baseUrl + data.getHost();
                }
                if(data.getPort() != -1) {
                    baseUrl = baseUrl + ":" + data.getPort();
                }
                if(data.getPathSegments() != null) {
                    for(String pathSegment : data.getPathSegments()) {
                        baseUrl = baseUrl + "/" + pathSegment;
                    }
                }
                String name = "";
                String password = "";
                String userInfo = data.getUserInfo();
                if(userInfo != null) {
                    String[] userNamePassword = userInfo.split(":");
                    if(userNamePassword.length == 2) {
                        name = userNamePassword[0];
                        password = userNamePassword[1];
                    }
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("baseUrl", baseUrl);
                editor.putString("name", name);
                editor.putString("password", password);
                editor.putString("accessToken", "");
                editor.putString("refreshToken", "");
                editor.commit();

                navigationView.getMenu().findItem(R.id.drawer_menu_library).setVisible(false);

                setFragment(new AccountLoginFragment());

                mCurrentNavItem = R.id.drawer_menu_account;
            } else {
                String accessToken = preferences.getString("accessToken", "");

                if(accessToken.equals("")) {
                    navigationView.getMenu().findItem(R.id.drawer_menu_library).setVisible(false);

                    setFragment(new AccountLoginFragment());

                    mCurrentNavItem = R.id.drawer_menu_account;
                } else {
                    navigationView.getMenu().findItem(R.id.drawer_menu_library).setVisible(true);

                    setFragment(new LibraryFragment());

                    mCurrentNavItem = R.id.drawer_menu_library;
                }
            }

            navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        }
        else {
            onBackStackChanged();  // force-call method to ensure indicator is shown properly
            mCurrentNavItem = savedInstanceState.getInt(STATE_CURRENT_MENU_ITEM);
            navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDrawerLayout.removeDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_MENU_ITEM, mCurrentNavItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    private void setupNavigationView(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (mCurrentNavItem == menuItem.getItemId()) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }

                switch (menuItem.getItemId()) {
                    case R.id.drawer_menu_account:
                        setTitle(R.string.menu_account);

                        SharedPreferences sp = getSharedPreferences("application", Context.MODE_PRIVATE);
                        String accessToken = sp.getString("accessToken", "");

                        if(accessToken.equals("")) {
                            setFragment(new AccountLoginFragment());
                        } else {
                            setFragment(new AccountLogoutFragment());
                        }
                        break;
                    case R.id.drawer_menu_library:
                        setFragment(new LibraryFragment());
                        break;
                    case R.id.drawer_menu_about:
                        setTitle(R.string.menu_about);
                        setFragment(new AboutFragment());
                        break;
                }

                mCurrentNavItem = menuItem.getItemId();
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mDrawerToggle.setDrawerIndicatorEnabled(getSupportFragmentManager().getBackStackEntryCount() == 0);
    }

    @Override
    public void onBackPressed() {
        if (!popFragment()) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onLogin() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().findItem(R.id.drawer_menu_library).setVisible(true);

        setFragment(new LibraryFragment());

        navigationView.getMenu().findItem(R.id.drawer_menu_library).setChecked(true);
    }

    @Override
    public void onLogout() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().findItem(R.id.drawer_menu_library).setVisible(false);

        setFragment(new AccountLoginFragment());

        navigationView.getMenu().findItem(R.id.drawer_menu_account).setChecked(true);
    }

    @Override
    public void onError(Throwable e) {
        MainApplication.handleError(this, e);
    }
}
