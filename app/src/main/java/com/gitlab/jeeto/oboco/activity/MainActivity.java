package com.gitlab.jeeto.oboco.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.common.Utils;
import com.gitlab.jeeto.oboco.fragment.AboutFragment;
import com.gitlab.jeeto.oboco.fragment.AccountLoginFragment;
import com.gitlab.jeeto.oboco.fragment.AccountLogoutFragment;
import com.gitlab.jeeto.oboco.fragment.BookCollectionBrowserFragment;
import com.gitlab.jeeto.oboco.fragment.DownloadBrowserFragment;
import com.gitlab.jeeto.oboco.fragment.DownloadManagerBrowserFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

public class MainActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener {
    private final static String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentNavItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            String accessToken = preferences.getString("accessToken", "");

            if(accessToken.equals("")) {
                navigationView.getMenu().findItem(R.id.drawer_menu_book_collection_browser).setVisible(false);

                setFragment(new AccountLoginFragment());

                mCurrentNavItem = R.id.drawer_menu_account;
            } else {
                navigationView.getMenu().findItem(R.id.drawer_menu_book_collection_browser).setVisible(true);

                setFragment(BookCollectionBrowserFragment.create(-1L, "LATEST_READ"));

                mCurrentNavItem = R.id.drawer_menu_book_collection_browser;
            }
        } else {
            onBackStackChanged();  // force-call method to ensure indicator is shown properly
            mCurrentNavItem = savedInstanceState.getInt(STATE_CURRENT_MENU_ITEM);
        }

        navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
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

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .addToBackStack(null)
                .commit();
    }

    public boolean popFragment() {
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
                        SharedPreferences sp = getSharedPreferences("application", Context.MODE_PRIVATE);
                        String accessToken = sp.getString("accessToken", "");

                        if(accessToken.equals("")) {
                            setFragment(new AccountLoginFragment());
                        } else {
                            setFragment(new AccountLogoutFragment());
                        }
                        break;
                    case R.id.drawer_menu_book_collection_browser:
                        setFragment(BookCollectionBrowserFragment.create(-1L, "ROOT"));
                        break;
                    case R.id.drawer_menu_download_browser:
                        setFragment(new DownloadBrowserFragment());
                        break;
                    case R.id.drawer_menu_download_manager_browser:
                        setFragment(new DownloadManagerBrowserFragment());
                        break;
                    case R.id.drawer_menu_about:
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

    public void navigateToAccountLogoutView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.drawer_menu_book_collection_browser);
        menuItem.setVisible(true);

        setFragment(new AccountLogoutFragment());

        menuItem = navigationView.getMenu().findItem(R.id.drawer_menu_account);
        menuItem.setChecked(true);

        mCurrentNavItem = menuItem.getItemId();
    }

    public void navigateToAccountLoginView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.drawer_menu_book_collection_browser);
        menuItem.setVisible(false);

        setFragment(new AccountLoginFragment());

        menuItem = navigationView.getMenu().findItem(R.id.drawer_menu_account);
        menuItem.setChecked(true);

        mCurrentNavItem = menuItem.getItemId();
    }

    public View getMessageView() {
        return findViewById(R.id.main_content);
    }
}
