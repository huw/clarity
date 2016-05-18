package nu.huw.clarity.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.sync.Synchroniser;
import nu.huw.clarity.ui.fragments.ListFragment;
import nu.huw.clarity.ui.misc.ColorStateLists;

public class MainActivity extends AppCompatActivity
        implements ListFragment.OnListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * Okay, this is really bad practice but from what I've found it's basically the only way
     * forward.
     *
     * You can't use contexts in a base class, which is problematic because I need to access an
     * AccountManager to create HttpClients. Instead, I've set a static variable here for the
     * application's context, so I can access AccountManager stuff. This is called from any class
     * which needs to get a basic context for the app.
     */
    public static Context        context;
    public        DrawerLayout   drawerLayout;
    private       NavigationView navigationView;

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Nav Drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.drawer);

        if (navigationView != null && drawerLayout != null) {

            // Keep all icons as their original colours
            navigationView.setItemIconTintList(null);
            changeColors(R.id.nav_forecast);

            navigationView.setNavigationItemSelectedListener(
                    new NavigationView.OnNavigationItemSelectedListener() {
                        @Override public boolean onNavigationItemSelected(MenuItem menuItem) {

                            changeColors(menuItem.getItemId());

                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        }
                    });
        }

        if (!AccountManagerHelper.doesAccountExist()) {

            // If there are no syncing accounts, sign in
            startActivityForResult(new Intent(this, LoginActivity.class), 1);
        } else {

            if (savedInstanceState != null) {
                return;
            }

            openFragments();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Synchroniser.synchronise();

        openFragments();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {

        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openFragments() {

        Fragment fragment = new ListFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment)
                                   .commit();
    }

    public void changeColors(int menuItem) {

        switch (menuItem) {
            case R.id.nav_forecast:
                setTheme(R.style.AppTheme_Red);
                navigationView.setItemTextColor(ColorStateLists.red);
                break;
            case R.id.nav_inbox:
                setTheme(R.style.AppTheme_BlueGrey);
                navigationView.setItemTextColor(ColorStateLists.blueGrey);
                break;
            case R.id.nav_projects:
                setTheme(R.style.AppTheme_Blue);
                navigationView.setItemTextColor(ColorStateLists.blue);
                break;
            case R.id.nav_contexts:
                setTheme(R.style.AppTheme);
                navigationView.setItemTextColor(ColorStateLists.purple);
                break;
            case R.id.nav_flagged:
                setTheme(R.style.AppTheme_Orange);
                navigationView.setItemTextColor(ColorStateLists.orange);
                break;
            case R.id.nav_nearby:
                setTheme(R.style.AppTheme_Green);
                navigationView.setItemTextColor(ColorStateLists.green);
                break;
        }
    }
}
