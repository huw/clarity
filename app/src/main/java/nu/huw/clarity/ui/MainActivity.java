package nu.huw.clarity.ui;

import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.ui.adapter.ListAdapter;
import nu.huw.clarity.ui.fragment.ListFragment;
import nu.huw.clarity.ui.viewholder.TaskViewHolder;

public class MainActivity extends AppCompatActivity
    implements ListFragment.OnListFragmentInteractionListener {

  private static final int LOG_IN_FIRST_REQUEST = 1;
  private static final String TAG = MainActivity.class.getSimpleName();
  private Toolbar toolbar;
  private DrawerLayout drawerLayout;
  private NavigationView drawer;
  private Fragment newFragment;
  private Fragment currentFragment;
  private boolean isChangingFragment;
  private Perspective perspective;
  private List<Perspective> perspectiveList;
  private IntentFilter syncIntentFilter;
  private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      refreshMenu(drawer);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Get some perspective
    DataModelHelper dmHelper = new DataModelHelper(getApplicationContext());
    perspective = dmHelper.getPlaceholderPerspective();
    perspectiveList = dmHelper.getPerspectives(false);

    // Register sync receiver
    syncIntentFilter =
        new IntentFilter(getApplicationContext().getString(R.string.sync_broadcast_intent));

    // Toolbar & Nav Drawer Setup
    setupToolbar(getToolbar());
    setupNavDrawer(getDrawerLayout(), getDrawer());

    if (savedInstanceState != null) {
      return;
    }

    if (!new AccountManagerHelper(getApplicationContext()).doesAccountExist()) {

      // If there are no syncing accounts, sign in
      startActivityForResult(new Intent(this, LoginActivity.class), LOG_IN_FIRST_REQUEST);
    } else {

      registerSyncs();
    }

    // Add list fragment

    currentFragment = ListFragment.newInstance(perspective, null);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.framelayout_main_container, currentFragment)
        .commit();
  }

  private void setupToolbar(Toolbar toolbar) {

    setSupportActionBar(toolbar);
    final ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupNavDrawer(DrawerLayout drawerLayout, NavigationView navigationView) {

    if (navigationView != null && drawerLayout != null) {

      // Keep all icons as their original colours
      navigationView.setItemIconTintList(null);

      refreshMenu(navigationView);

      // Colour and set the checked item
      Perspective oldPerspective = perspective;
      MenuItem firstItem = navigationView.getMenu().getItem(0);
      updatePerspective(firstItem.getItemId());

      navigationView.setCheckedItem(firstItem.getItemId());
      setTitle(firstItem.getTitle());
      changeColors(oldPerspective);

      navigationView.setNavigationItemSelectedListener(new NavigationViewListener());
      drawerLayout.addDrawerListener(new DrawerListener());
    }
  }

  private void updatePerspective(int menuID) {

    Perspective newPerspective = null;
    for (Perspective candidate : perspectiveList) {
      if (candidate.menuID == menuID) {
        newPerspective = candidate;
      }
    }

    if (newPerspective == null) {
      newPerspective = new DataModelHelper(getApplicationContext()).getForecastPerspective();
    }
    perspective = newPerspective;
  }

  private void refreshMenu(NavigationView navigationView) {

    // Build menu
    // We have a default menu for before the sync finishes, but as soon as that's done we
    // build the menu appropriately based on the user's own perspective choices.

    perspectiveList = new DataModelHelper(getApplicationContext()).getPerspectives(false);

    Menu menu = navigationView.getMenu();
    menu.clear();

    for (Perspective menuItem : perspectiveList) {
      menu.add(R.id.menugroup_main_drawer, menuItem.menuID, Menu.NONE, menuItem.name);
      menu.findItem(menuItem.menuID).setIcon(menuItem.icon).setCheckable(true);
    }

    // Re-select the current perspective in the menu
    drawer.setCheckedItem(perspective.menuID);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(R.menu.toolbar, menu);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == LOG_IN_FIRST_REQUEST) {

      if (resultCode == LoginActivity.RESULT_OK) {

        // OK to start syncing

        Account account = new AccountManagerHelper(getApplicationContext()).getAccount();
        String authority = getString(R.string.authority);
        Bundle extras = new Bundle();

        registerSyncs();    // Also requests a new sync

        if (currentFragment instanceof ListFragment) {
          ((ListFragment) currentFragment).checkForSyncs();
        }
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        getDrawerLayout().openDrawer(GravityCompat.START);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {

    DrawerLayout dl = getDrawerLayout();

    if (dl.isDrawerOpen(GravityCompat.START)) {
      dl.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onListFragmentInteraction(ListAdapter.ViewHolder holder) {

    Entry item = holder.entry;

    if (holder instanceof TaskViewHolder || item.headerRow) {

      Intent intent = new Intent(this, DetailActivity.class);
      intent.putExtra("ENTRY", item);
      intent.putExtra("THEME_ID", perspective.themeID);
      startActivity(intent);
      return;
    }

    newFragment = ListFragment.newInstance(perspective, item);

    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
    ft.replace(R.id.framelayout_main_container, newFragment);
    ft.addToBackStack(null);
    ft.commit();

    currentFragment = newFragment;
  }

  private Toolbar getToolbar() {

    if (toolbar == null) {
      Toolbar tb = (Toolbar) findViewById(R.id.toolbar_main);

      if (tb == null) {
        throw new NullPointerException();
      }

      toolbar = tb;
    }

    return toolbar;
  }

  private DrawerLayout getDrawerLayout() {

    if (drawerLayout == null) {
      DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawerlayout_main);

      if (dl == null) {
        throw new NullPointerException();
      }

      drawerLayout = dl;
    }

    return drawerLayout;
  }

  private NavigationView getDrawer() {

    if (drawer == null) {
      NavigationView nv = (NavigationView) findViewById(R.id.navigationview_main_drawer);

      if (nv == null) {
        throw new NullPointerException();
      }

      drawer = nv;
    }

    return drawer;
  }

  private void changeColors(Perspective oldPerspective) {

    // Get the current header colour
    int colorFrom = ResourcesCompat.getColor(getResources(), oldPerspective.color, null);

    // Set the drawer highlight color to the current perspective's
    getDrawer().setItemTextColor(ResourcesCompat.getColorStateList(getResources(),
        perspective.colorStateListID,
        null));

    // Now to figure out the colour we're transitioning to, we get the _new_ primary theme
    // colour, which has been changed, and save it into a new value.

    setTheme(perspective.themeID);
    int colorTo = ResourcesCompat.getColor(getResources(), perspective.color, null);

    // Animation tweens the two colours together according to Material Design principles.
    ValueAnimator toolbarAnimation =
        ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    toolbarAnimation.setDuration(300);

    toolbarAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        Toolbar tb = getToolbar();
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

  private void registerSyncs() {

    Account account = new AccountManagerHelper(getApplicationContext()).getAccount();
    String authority = getString(R.string.authority);

    ContentResolver.setSyncAutomatically(account, authority, true);

    if (ContentResolver.getPeriodicSyncs(account, authority).size() == 0) {

      long seconds = 60L * 60L;   // One hour

      ContentResolver.addPeriodicSync(account, authority, new Bundle(), seconds);
    }
  }

  /**
   * These functions help initialise the sync receiver
   */
  @Override
  public void onResume() {

    super.onResume();
    LocalBroadcastManager.getInstance(getApplicationContext())
        .registerReceiver(syncReceiver, syncIntentFilter);
  }

  @Override
  public void onPause() {

    super.onPause();
    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(syncReceiver);
  }

  /**
   * The Navigation Item Selected listener determines what to do when we select a menu item,
   * and returns a reference to that menu item so we can do stuff with it.
   */
  class NavigationViewListener implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

      // Get perspective for new item
      Perspective oldPerspective = perspective;
      updatePerspective(menuItem.getItemId());

      changeColors(oldPerspective);
      setTitle(menuItem.getTitle());

      newFragment = ListFragment.newInstance(perspective, null);
      isChangingFragment = true;
      currentFragment = new Fragment();

      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
          R.anim.fade_out);
      ft.replace(R.id.framelayout_main_container, currentFragment);
      ft.commit();

      getDrawerLayout().closeDrawer(GravityCompat.START);
      return true;
    }
  }

  class DrawerListener implements android.support.v4.widget.DrawerLayout.DrawerListener {

    @Override
    public void onDrawerClosed(View view) {

      if (isChangingFragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
            R.anim.fade_out);
        ft.replace(R.id.framelayout_main_container, newFragment);
        ft.commit();

        // Note: 'showProgess(false)' is called after the data has loaded in ListFragment

        currentFragment = newFragment;
        isChangingFragment = false;
      }
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }
  }
}