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
import nu.huw.clarity.sync.Synchroniser;
import nu.huw.clarity.ui.adapters.ContextViewHolder;
import nu.huw.clarity.ui.adapters.ListAdapter;
import nu.huw.clarity.ui.adapters.NestedTaskViewHolder;
import nu.huw.clarity.ui.adapters.ProjectViewHolder;
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
    private       DrawerLayout   drawerLayout;
    private       NavigationView navigationView;
    private       Toolbar        toolbar;
    private       Fragment       newFragment;
    private       Fragment       currentFragment;
    private       boolean        isChangingFragment;

    private void setupToolbar(Toolbar toolbar) {

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            this.toolbar = toolbar;
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupNavDrawer(DrawerLayout drawerLayout, NavigationView navigationView) {

        if (navigationView != null && drawerLayout != null) {

            this.drawerLayout = drawerLayout;
            this.navigationView = navigationView;

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
        setupToolbar((Toolbar) findViewById(R.id.toolbar));
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
                drawerLayout.openDrawer(GravityCompat.START);
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

        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override public void onListFragmentInteraction(ListAdapter.ViewHolder holder) {

        if (holder instanceof ProjectViewHolder) {

            String holderID = ((ProjectViewHolder) holder).project.id;
            newFragment = ListFragment.newInstance(R.id.nav_projects, holderID);
        } else if (holder instanceof NestedTaskViewHolder) {

            String holderID = ((NestedTaskViewHolder) holder).task.id;
            newFragment = ListFragment.newInstance(R.id.nav_projects, holderID);
        } else if (holder instanceof ContextViewHolder) {

            String holderID = ((ContextViewHolder) holder).context.id;
            newFragment = ListFragment.newInstance(R.id.nav_contexts, holderID);
        } else {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.fragment_container, newFragment);
        ft.addToBackStack(null);
        ft.commit();

        currentFragment = newFragment;
    }

    private void changeColors(int menuItem) {

        // Get the current header colour
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int colorFrom = typedValue.data;

        // Depending on the menu item, we change the current theme (which defines a primary
        // colour), and then set the text colour in the sidebar (for the highlight).

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

                // The update listener will give you a number between the two values you gave the
                // animation object initially. Every time it's ready to update, it runs this code.
                // By the way, you can just set the status bar background colour, and the program
                // will automatically tint it for you. Just make sure you call 'invalidate()'
                // afterward.

                toolbar.setBackgroundColor((int) animator.getAnimatedValue());
                drawerLayout.setStatusBarBackgroundColor((int) animator.getAnimatedValue());
                drawerLayout.invalidate();
            }
        });

        navigationView.invalidate();

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

            drawerLayout.closeDrawer(GravityCompat.START);
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