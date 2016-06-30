package nu.huw.clarity.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.RecursiveColumnUpdater;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.sync.Synchroniser;
import nu.huw.clarity.ui.adapters.ListAdapter;
import nu.huw.clarity.ui.fragments.ListFragment;
import nu.huw.clarity.ui.misc.ColorStateLists;
import nu.huw.clarity.ui.viewholders.ContextViewHolder;
import nu.huw.clarity.ui.viewholders.FolderViewHolder;
import nu.huw.clarity.ui.viewholders.NestedTaskViewHolder;
import nu.huw.clarity.ui.viewholders.ProjectViewHolder;
import nu.huw.clarity.ui.viewholders.TaskViewHolder;

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
    private       Toolbar        mToolbar;
    private       DrawerLayout   mDrawerLayout;
    private       NavigationView mDrawer;
    private       Fragment       newFragment;
    private       Fragment       currentFragment;
    private       boolean        isChangingFragment;
    private int menuID = R.id.nav_forecast;

    private void setupToolbar(Toolbar toolbar) {

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupNavDrawer(DrawerLayout drawerLayout, NavigationView navigationView) {

        if (navigationView != null && drawerLayout != null) {

            // Keep all icons as their original colours
            navigationView.setItemIconTintList(null);

            // Colour and set the checked item
            MenuItem firstItem = navigationView.getMenu().getItem(0);
            navigationView.setCheckedItem(firstItem.getItemId());
            setTitle(firstItem.getTitle());
            changeColors(firstItem.getItemId());

            navigationView.setNavigationItemSelectedListener(new NavigationViewListener());
            drawerLayout.addDrawerListener(new DrawerListener());
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        // Toolbar & Nav Drawer Setup
        setupToolbar((Toolbar) findViewById(R.id.toolbar_list));
        setupNavDrawer((DrawerLayout) findViewById(R.id.drawer_layout),
                       (NavigationView) findViewById(R.id.drawer));

        if (savedInstanceState != null) {
            return;
        }

        if (!AccountManagerHelper.doesAccountExist()) {

            // If there are no syncing accounts, sign in
            startActivityForResult(new Intent(this, LoginActivity.class), 1);
        }

        currentFragment = ListFragment.newInstance(R.id.nav_forecast);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, currentFragment)
                                   .commit();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Synchroniser.synchronise();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getDrawerLayout().openDrawer(GravityCompat.START);
                return true;
            case R.id.synchronise:
                Synchroniser.synchronise();
                return true;
            case R.id.reload_db:
                new RecursiveColumnUpdater().updateTree();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {

        DrawerLayout dl = getDrawerLayout();

        if (dl.isDrawerOpen(GravityCompat.START)) {
            dl.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override public void onListFragmentInteraction(ListAdapter.ViewHolder holder) {

        Entry item = holder.entry;

        if (holder instanceof TaskViewHolder || item.headerRow) {

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("ENTRY", holder.entry);
            intent.putExtra("MENU_ID", menuID);
            startActivity(intent);
            return;
        } else if (holder instanceof ProjectViewHolder) {

            menuID = R.id.nav_projects;
        } else if (holder instanceof NestedTaskViewHolder) {

            menuID = R.id.nav_projects;
        } else if (holder instanceof ContextViewHolder) {

            menuID = R.id.nav_contexts;
        } else if (holder instanceof FolderViewHolder) {

            menuID = R.id.nav_projects;
        } else {
            return;
        }

        newFragment = ListFragment.newInstance(menuID, item.id);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.fragment_container, newFragment);
        ft.addToBackStack(null);
        ft.commit();

        currentFragment = newFragment;
    }

    private Toolbar getToolbar() {

        if (mToolbar == null) {
            Toolbar tb = (Toolbar) findViewById(R.id.toolbar_list);

            if (tb == null) {
                throw new NullPointerException();
            }

            mToolbar = tb;
        }

        return mToolbar;
    }

    private DrawerLayout getDrawerLayout() {

        if (mDrawerLayout == null) {
            DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);

            if (dl == null) {
                throw new NullPointerException();
            }

            mDrawerLayout = dl;
        }

        return mDrawerLayout;
    }

    private NavigationView getDrawer() {

        if (mDrawer == null) {
            NavigationView nv = (NavigationView) findViewById(R.id.drawer);

            if (nv == null) {
                throw new NullPointerException();
            }

            mDrawer = nv;
        }

        return mDrawer;
    }

    private void changeColors(int menuItem) {

        // Get the current header colour
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorFrom = typedValue.data;

        // Depending on the menu item, we change the current theme (which defines a primary
        // colour), and then set the text colour in the sidebar (for the highlight).

        NavigationView drawer = getDrawer();

        switch (menuItem) {
            case R.id.nav_forecast:
                setTheme(R.style.AppTheme_Red);
                drawer.setItemTextColor(ColorStateLists.red);
                break;
            case R.id.nav_inbox:
                setTheme(R.style.AppTheme_BlueGrey);
                drawer.setItemTextColor(ColorStateLists.blueGrey);
                break;
            case R.id.nav_projects:
                setTheme(R.style.AppTheme_Blue);
                drawer.setItemTextColor(ColorStateLists.blue);
                break;
            case R.id.nav_contexts:
                setTheme(R.style.AppTheme);
                drawer.setItemTextColor(ColorStateLists.purple);
                break;
            case R.id.nav_flagged:
                setTheme(R.style.AppTheme_Orange);
                drawer.setItemTextColor(ColorStateLists.orange);
                break;
            case R.id.nav_nearby:
                setTheme(R.style.AppTheme_Green);
                drawer.setItemTextColor(ColorStateLists.green);
                break;
        }

        // Now to figure out the colour we're transitioning to, we get the _new_ primary theme
        // colour, which has been changed, and save it into a new value.

        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorTo = typedValue.data;

        // Animation tweens the two colours together according to Material Design principles.
        ValueAnimator toolbarAnimation =
                ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        toolbarAnimation.setDuration(300);

        toolbarAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animator) {

                Toolbar      tb = getToolbar();
                DrawerLayout dl = getDrawerLayout();

                // The update listener will give you a number between the two values you gave the
                // animation object initially. Every time it's ready to update, it runs this code.
                // By the way, you can just set the status bar background colour, and the program
                // will automatically tint it for you. Just make sure you call 'invalidate()'
                // afterward.

                tb.setBackgroundColor((int) animator.getAnimatedValue());
                dl.setStatusBarBackgroundColor((int) animator.getAnimatedValue());
                dl.invalidate();
            }
        });

        drawer.invalidate();

        toolbarAnimation.start();
    }

    /**
     * Show/hide the progress spinner for the fragment switcher
     */
    private void showProgress(final boolean show) {

        // This uses the animation controls to fade in the progress spinner.
        // It also fades out the UI first. If you're interested in following
        // the logic, the spinner doesn't need a `setVisibility()` immediately
        // because its initial state is blank.

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final View progressView = findViewById(R.id.fragment_progress);

        if (progressView != null) {
            progressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override public void onAnimationEnd(Animator animation) {

                                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                            }
                        });
        }
    }

    /**
     * The Navigation Item Selected listener determines what to do when we select a menu item,
     * and returns a reference to that menu item so we can do stuff with it.
     */
    class NavigationViewListener implements NavigationView.OnNavigationItemSelectedListener {

        @Override public boolean onNavigationItemSelected(MenuItem menuItem) {

            changeColors(menuItem.getItemId());
            setTitle(menuItem.getTitle());

            newFragment = ListFragment.newInstance(menuItem.getItemId());
            isChangingFragment = true;
            currentFragment = new Fragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
                                   R.anim.fade_out);
            ft.replace(R.id.fragment_container, currentFragment);
            ft.commit();

            showProgress(true);

            getDrawerLayout().closeDrawer(GravityCompat.START);
            return true;
        }
    }

    class DrawerListener implements android.support.v4.widget.DrawerLayout.DrawerListener {

        @Override public void onDrawerClosed(View view) {

            if (isChangingFragment) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
                                       R.anim.fade_out);
                ft.replace(R.id.fragment_container, newFragment);
                ft.commit();

                showProgress(false);

                currentFragment = newFragment;
                isChangingFragment = false;
            }
        }

        @Override public void onDrawerStateChanged(int newState) {}

        @Override public void onDrawerSlide(View drawerView, float slideOffset) {}

        @Override public void onDrawerOpened(View drawerView) {}
    }
}