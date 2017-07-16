package nu.huw.clarity.ui.activity;

import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager.TaskDescription;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.io.File;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.AppDatabase;
import nu.huw.clarity.db_old.model.DataModelHelper;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.ID;
import nu.huw.clarity.model_old.Entry;
import nu.huw.clarity.model_old.Perspective;
import nu.huw.clarity.notification.NotificationService;
import nu.huw.clarity.ui.fragment.DetailFragment;
import nu.huw.clarity.ui.fragment.ListFragment;
import org.threeten.bp.LocalDateTime;

public class MainActivity extends AppCompatActivity implements
    ListFragment.OnListFragmentInteractionListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int LOG_IN_FIRST_REQUEST = 1; // Used to launch LoginActivity

  @BindView(R.id.toolbar_main)
  Toolbar toolbar_main;
  @BindView(R.id.drawerlayout_main)
  DrawerLayout drawerlayout_main;
  @BindView(R.id.navigationview_main_drawer)
  NavigationView navigationview_main_drawer;

  private Perspective perspective;
  private Perspective backPerspective;
  private IntentFilter syncIntentFilter;
  private Fragment fragment;
  private boolean isChangingFragment;
  private List<Perspective> perspectiveList;
  private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      refreshNavigationDrawerMenu();
    }
  };

  /**
   * Essentially a constructor. Called every time a new instance of the activity is created—just
   * that sometimes new instances are created when doing non-creation things like rotating the
   * screen. Get it?
   */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {

    // Quickly testing database:
    AppDatabase db = Room.inMemoryDatabaseBuilder(getApplicationContext(), AppDatabase.class)
        .build();
    Attachment sent = new Attachment(new ID("eVJuS9Id_wJ"), LocalDateTime.now(),
        LocalDateTime.now(), "random", new ID("eVJuS9Id_wJ"), new File(""));
    db.attachmentDao().addAttachment(
        new Attachment(new ID("eVJuS9Id_wJ"), LocalDateTime.now(), LocalDateTime.now(), "random",
            new ID("eVJuS9Id_wJ"), new File("")));
    LiveData<Attachment> retrieved = db.attachmentDao().getAttachmentFromID(new ID("eVJuS9Id_wJ"));
    Log.d(TAG, "" + sent.getId() + " " + retrieved.getValue().getId());

    super.onCreate(savedInstanceState);

    // Register sync receiver

    syncIntentFilter = new IntentFilter(getString(R.string.sync_broadcastintent));

    // Determine whether to prompt a login
    // If there are no syncing accounts, then a login should be prompted
    // If there is a syncing account, then we should register the sync service

    AccountManagerHelper accountManagerHelper = new AccountManagerHelper(this);
    if (!accountManagerHelper.accountExists()) {
      Intent intent = new Intent(this, LoginActivity.class);
      intent.putExtra(LoginActivity.KEY_REQUEST, LoginActivity.VALUE_NOACCOUNT);
      startActivityForResult(intent, LOG_IN_FIRST_REQUEST);
    } else {
      registerSyncService(false);
    }

    // Initialise view

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // Setup toolbar

    setSupportActionBar(toolbar_main);
    final ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {

      // Set navigation menu burger menu button hamburger

      actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Setup navigation drawer (pt. 1)

    navigationview_main_drawer.setItemIconTintList(null);
    Perspective checkedDrawerItem = refreshNavigationDrawerMenu();
    backPerspective = checkedDrawerItem;

    // Add the list fragment based on the passed intent (if at all)

    Intent intent = getIntent();
    if (intent.hasExtra("PERSPECTIVE")) {
      // If the intent has a perspective, then set that
      perspective = intent.getParcelableExtra("PERSPECTIVE");
      if (perspective == null) perspective = checkedDrawerItem;
    } else if (savedInstanceState != null && savedInstanceState.containsKey("PERSPECTIVE")) {
      // If the saved instance state has a perspective, set that
      perspective = savedInstanceState.getParcelable("PERSPECTIVE");
    } else {
      // Change the perspective to the first menu item
      perspective = checkedDrawerItem;
    }

    // Check for a saved fragment from the instance state
    // If we have a saved state, that's great -- use the fragment
    // But if we didn't save that fragment for whatever reason, check to see if it's null and
    // replace it based on the current perspective

    if (savedInstanceState != null) {
      fragment = getSupportFragmentManager().getFragment(savedInstanceState, "FRAGMENT");
    }

    if (fragment == null) {
      if (intent.hasExtra("ENTRY") && (perspective.id.equals("ProcessProjects") || perspective.id
          .equals("ProcessContexts"))) {
        Entry entry = intent.getParcelableExtra("ENTRY");
        ListFragment fragment;

        // If the entry has children, then we should display a view where we can appropriately show
        // them (with the entry as a header).
        // If the entry doesn't have children, then display its parent entry.
        // But if the parent doesn't exist, then display the root view for that perspective.

        if (entry.hasChildren()) {
          fragment = ListFragment.newInstance(perspective, entry);
        } else {
          Entry parent = entry.getParent(this);
          if (parent != null) {
            fragment = ListFragment.newInstance(perspective, parent);
          } else {
            fragment = ListFragment.newInstance(perspective, null);
          }
        }
        fragment.setRetainInstance(true);
      } else if (intent.hasExtra("ENTRY") && perspective.id.equals("ProcessForecast")) {

        // We've received this from a notification
        // Open up the detail fragment with the correct perspective in the background

        Entry entry = intent.getParcelableExtra("ENTRY");
        DetailFragment detailFragment = DetailFragment.newInstance(perspective, entry);
        detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
        fragment = ListFragment.newInstance(perspective, null);

      } else {
        fragment = ListFragment.newInstance(perspective, null);
      }
    }

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.framelayout_main_container, fragment)
        .commit();

    // Setup navigation drawer (pt. 2)
    // This also involves setting the current checked item in the drawer, the toolbar title, and the
    // toolbar colour depending on which item is selected.

    navigationview_main_drawer.setCheckedItem(perspective.menuID);
    setTitle(perspective.name);
    changeColors(null, perspective);

    navigationview_main_drawer
        .setNavigationItemSelectedListener(new MainActivity.NavigationViewListener());
    drawerlayout_main.addDrawerListener(new MainActivity.DrawerListener());
  }

  @Override
  protected void onSaveInstanceState(Bundle out) {
    out.putParcelable("PERSPECTIVE", perspective);
    if (fragment.isAdded()) {
      getSupportFragmentManager().putFragment(out, "FRAGMENT", fragment);
    }
    super.onSaveInstanceState(out);
  }

  @Override
  public void onPause() {

    // Deegister the sync receiver

    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
  }

  @Override
  public void onResume() {

    // Register the sync receiver

    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, syncIntentFilter);

    // Register the notification service
    // Look for new notifications every 5 minutes
    // And check right as we open the app as well

    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    Intent intent = new Intent(this, NotificationService.class);
    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

    startService(intent);
    alarmManager.cancel(pendingIntent);
    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 5 * 1000, 5 * 60 * 1000, pendingIntent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == LOG_IN_FIRST_REQUEST && resultCode == LoginActivity.RESULT_OK) {
      registerSyncService(true);
      if (fragment != null && fragment instanceof ListFragment) {
        ((ListFragment) fragment).checkForSyncs();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    // Called when we click an item in the toolbar
    // If the item is the home menu burger menu hamburger
    // Then open the navigation drawer

    switch (item.getItemId()) {
      case android.R.id.home:
        drawerlayout_main.openDrawer(GravityCompat.START);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {

    // Close the navigation drawer if it's open
    // Or return to the default perspective if we're not there
    // Otherwise just close the activity

    if (drawerlayout_main.isDrawerOpen(GravityCompat.START)) {
      drawerlayout_main.closeDrawer(GravityCompat.START);
    } else if (getSupportFragmentManager().getBackStackEntryCount() == 0
        && !perspective.equals(backPerspective)) {
      changePerspectives(perspective, backPerspective);
      perspective = backPerspective;
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onItemListInteraction(@NonNull Entry entry, @Nullable Perspective perspective) {

    // We've clicked a list item and want to show its children
    // Create the new list fragment and add it to the back stack

    ListFragment newFragment = ListFragment.newInstance(perspective, entry);
    newFragment.setRetainInstance(true);

    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        .replace(R.id.framelayout_main_container, newFragment)
        .addToBackStack(null)
        .commit();

    fragment = newFragment;
  }

  @Override
  public void onItemDetailInteraction(@NonNull Entry entry, @NonNull Perspective perspective) {

    // We've clicked a list item and want to show its details
    // Create the detail fragment and show it

    DetailFragment detailFragment = DetailFragment.newInstance(perspective, entry);
    detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
  }

  /**
   * Using the account manager, register a periodic sync using the normal service if that sync
   * doesn't exist.
   */
  private void registerSyncService(boolean requestSync) {

    AccountManagerHelper accountManagerHelper = new AccountManagerHelper(this);
    Account account = accountManagerHelper.getAccount();

    String authority = getString(R.string.sync_authority);
    if (ContentResolver.getPeriodicSyncs(account, authority).isEmpty()) {

      long ONE_HOUR = 60L * 60L; // One hour
      ContentResolver.setSyncAutomatically(account, authority, true);
      ContentResolver.addPeriodicSync(account, authority, new Bundle(), ONE_HOUR);

    }

    // Immediately sync after logging in

    if (requestSync) {

      Bundle syncSettings = new Bundle();
      syncSettings.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
      syncSettings.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

      ContentResolver.requestSync(account, authority, syncSettings);

    }
  }

  /**
   * Given a Menu ID from the navigation drawer, return the matching perspective from the list of
   * available perspectives.
   */
  @NonNull
  private Perspective getPerspectiveForMenuID(int menuID) {

    DataModelHelper dataModelHelper = new DataModelHelper(this);
    Perspective newPerspective = null;

    // Go through the list of candidate perspectives and try to find one matching the Menu ID

    for (Perspective candidate : perspectiveList) {
      if (candidate.menuID == menuID) {
        newPerspective = candidate;
      }
    }

    // Otherwise make the new perspective the Forecast

    if (newPerspective == null) {
      newPerspective = dataModelHelper.getForecastPerspective();
    }

    return newPerspective;
  }

  /**
   * Refresh the navigation drawer menu by getting a new list of perspectives
   */
  @NonNull
  private Perspective refreshNavigationDrawerMenu() {

    // We have a default menu for before the sync finishes, but once it's done we should build a
    // menu based on the user's existing perspective choices and order.

    DataModelHelper dataModelHelper = new DataModelHelper(this);
    perspectiveList = dataModelHelper.getPerspectives(false);
    Menu menu = navigationview_main_drawer.getMenu();
    menu.clear();

    // For each perspective, add it to the menu by name
    // Then set its icon and make it checkable programmatically

    for (Perspective perspective : perspectiveList) {
      menu.add(R.id.menugroup_main_drawer, perspective.menuID, Menu.NONE, perspective.name);
      menu.findItem(perspective.menuID).setIcon(perspective.icon).setCheckable(true);
    }

    // Re-select the current perspective in the menu

    if (perspective == null || perspective.menuID == 0) {
      navigationview_main_drawer.setCheckedItem(perspectiveList.get(0).menuID);
      return perspectiveList.get(0);
    } else {
      navigationview_main_drawer.setCheckedItem(perspective.menuID);
      return perspective;
    }
  }

  /**
   * Given two perspectives, changes the toolbar and navigation drawer colours to suit.
   */
  private void changeColors(@Nullable Perspective fromPerspective,
      @NonNull Perspective toPerspective) {

    if (fromPerspective == null) fromPerspective = toPerspective;

    setTheme(toPerspective.themeID);

    // Get the current header colour

    @ColorRes int colorFrom = ContextCompat.getColor(this, fromPerspective.colorID);
    @ColorRes int colorTo = ContextCompat.getColor(this, toPerspective.colorID);

    // Set the navigationview_main_drawer highlight colour to the new perspective's

    navigationview_main_drawer
        .setItemTextColor(ContextCompat.getColorStateList(this, toPerspective.colorStateListID));

    // Animations tweens the two colours together according to Material's principles

    ValueAnimator toolbarAnimation = ValueAnimator
        .ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    toolbarAnimation.setDuration(300);

    toolbarAnimation.addUpdateListener(new AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animator) {

        // The update listener will give you a number between the two values you gave the
        // animation object initially. Every time it's ready to update, it runs this code.
        // By the way, you can just set the status bar background colour, and the program
        // will automatically tint it for you. Just make sure you call 'invalidate()'
        // afterward.

        toolbar_main.setBackgroundColor((int) animator.getAnimatedValue());
        drawerlayout_main.setStatusBarBackgroundColor((int) animator.getAnimatedValue());
        drawerlayout_main.invalidate();
      }
    });

    navigationview_main_drawer.invalidate();
    toolbarAnimation.start();

    // Change the colour of the title bar in the recents menu

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {

      String title = getString(R.string.app_name_short);
      Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

      TaskDescription taskDescription = new TaskDescription(title, appIcon, colorTo);
      setTaskDescription(taskDescription);
    }
  }

  /**
   * Changes between the two given perspectives, including selecting the correct menu item, colours,
   * and title.
   */
  private void changePerspectives(Perspective fromPerspective, Perspective toPerspective) {

    changeColors(fromPerspective, toPerspective);
    setTitle(toPerspective.name);
    navigationview_main_drawer.setCheckedItem(toPerspective.menuID);

    ListFragment newFragment = ListFragment.newInstance(toPerspective, null);
    newFragment.setRetainInstance(true);

    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        .replace(R.id.framelayout_main_container, newFragment)
        .commit();

    fragment = newFragment;

  }

  class NavigationViewListener implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

      // Get the perspective for the new item

      Perspective fromPerspective = perspective;
      Perspective toPerspective = getPerspectiveForMenuID(menuItem.getItemId());

      changeColors(fromPerspective, toPerspective);
      setTitle(toPerspective.name);

      // Begin the fragment transition by removing the current fragment
      // (in conjunction with DrawerListener)

      isChangingFragment = true;
      Fragment fragment = new Fragment();
      fragment.setRetainInstance(true);
      getSupportFragmentManager()
          .beginTransaction()
          .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
          .replace(R.id.framelayout_main_container, fragment)
          .commit();

      // Finish up

      drawerlayout_main.closeDrawer(GravityCompat.START);
      perspective = toPerspective;
      return true;
    }
  }

  class DrawerListener implements DrawerLayout.DrawerListener {

    @Override
    public void onDrawerClosed(View drawerView) {

      if (isChangingFragment) {

        // End the fragment transition by adding the new fragment
        // (in conjunction with NavigationViewListener)

        ListFragment newFragment = ListFragment.newInstance(perspective, null);
        newFragment.setRetainInstance(true);

        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.framelayout_main_container, newFragment)
            .commit();

        fragment = newFragment;
        isChangingFragment = false;
      }
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
  }
}
