package nu.huw.clarity.ui;

import android.accounts.Account;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.account.AccountManagerHelper;
import nu.huw.clarity.db.TreeOperations;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.db.model.NoteHelper;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragment.ListFragment;

public class MainActivity extends AppCompatActivity
    implements ListFragment.OnListFragmentInteractionListener {

  private static final int LOG_IN_FIRST_REQUEST = 1;
  private static final String TAG = MainActivity.class.getSimpleName();

  @BindView(R.id.toolbar_main)
  Toolbar toolbar_main;
  @BindView(R.id.drawerlayout_main)
  DrawerLayout drawerlayout_main;
  @BindView(R.id.navigationview_main_drawer)
  NavigationView navigationview_main_drawer;

  private Fragment newFragment;
  private Fragment currentFragment;
  private boolean isChangingFragment;
  private Perspective perspective;
  private List<Perspective> perspectiveList;
  private IntentFilter syncIntentFilter;
  private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      refreshMenu();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    // Get some perspective
    DataModelHelper dmHelper = new DataModelHelper(getApplicationContext());
    perspective = dmHelper.getPlaceholderPerspective();
    perspectiveList = dmHelper.getPerspectives(false);

    // Register sync receiver
    syncIntentFilter = new IntentFilter(getString(R.string.sync_broadcastintent));

    // Toolbar & Nav Drawer Setup
    setupToolbar();
    setupNavDrawer();

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

  @Override
  protected void onPostResume() {
    super.onPostResume();
    testNotifications();
  }

  private void testNotifications() {

    Context androidContext = getApplicationContext();
    TreeOperations treeOperations = new TreeOperations(androidContext);
    DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
    NotificationManagerCompat notificationManager = NotificationManagerCompat
        .from(getApplicationContext());
    int ID = 0;

    // We have to get the list of newly overdued tasks first, then update all their overdue flags.
    // In that order.

    List<Task> tasks = dataModelHelper.getNewOverdueTasks();
    treeOperations.updateDueSoonAndOverdue();
    treeOperations.updateCountsFromTop();

    // Display a summary notification

    if (tasks.size() >= 4) { // default number for grouping

      String notification_duesummarytitle = getString(R.string.notification_duesummarytitle);
      String notification_duesummarytext = getString(R.string.notification_duesummarytext,
          tasks.size());

      // Result intent: Home screen

      Intent intent = new Intent(this, MainActivity.class);
      PendingIntent pendingIntent = PendingIntent
          .getActivity(this, ID, intent, Intent.FLAG_ACTIVITY_SINGLE_TOP);

      // Build notification

      NotificationCompat.Builder builder = new Builder(getApplicationContext())
          .setSmallIcon(R.drawable.ic_notification)
          .setContentTitle(notification_duesummarytitle)
          .setContentText(notification_duesummarytext)
          .setContentIntent(pendingIntent)
          .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
          .setCategory(NotificationCompat.CATEGORY_REMINDER)
          .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
          .setAutoCancel(true)
          .setGroupSummary(true)
          .setGroup("group_key_overdue");

      notificationManager.notify(ID, builder.build());
      ID++;
    }

    // Display a notification for each returned task

    for (Task task : tasks) {

      String notification_duenow = getString(R.string.notification_duenow);

      // Initialise notification

      NotificationCompat.Builder builder = new Builder(getApplicationContext())
          .setSmallIcon(R.drawable.ic_notification)
          .setContentTitle(task.name)
          .setContentText(notification_duenow)
          .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
          .setCategory(NotificationCompat.CATEGORY_REMINDER)
          .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
          .setAutoCancel(true)
          .setGroup("group_key_overdue");

      // Set an expandable style with the task's note text if necessary
      // Note that the original 'due now' text remains

      if (task.noteXML != null) {

        String noteText = NoteHelper.noteXMLtoString(task.noteXML);
        String expandedText = notification_duenow + " • " + noteText;

        if (!noteText.isEmpty()) {
          builder.setStyle(new BigTextStyle().bigText(expandedText));
        }
      }

      // Set notification actions/intents

      Intent intent = new Intent(this, DetailActivity.class);
      intent.putExtra("ENTRY", task);
      intent.putExtra("PERSPECTIVE", perspective);

      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
      stackBuilder.addNextIntentWithParentStack(intent);
      PendingIntent pendingIntent = stackBuilder
          .getPendingIntent(ID, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentIntent(pendingIntent);

      // Give the notification a unique ID

      notificationManager.notify(ID, builder.build());
      ID++;
    }

  }

  private void setupToolbar() {

    setSupportActionBar(toolbar_main);
    final ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupNavDrawer() {

    // Keep all icons as their original colours
    navigationview_main_drawer.setItemIconTintList(null);

    refreshMenu();

    // Colour and set the checked item
    Perspective oldPerspective = perspective;
    MenuItem firstItem = navigationview_main_drawer.getMenu().getItem(0);
    updatePerspective(firstItem.getItemId());

    navigationview_main_drawer.setCheckedItem(firstItem.getItemId());
    setTitle(firstItem.getTitle());
    changeColors(oldPerspective);

    navigationview_main_drawer.setNavigationItemSelectedListener(new NavigationViewListener());
    drawerlayout_main.addDrawerListener(new DrawerListener());
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

  private void refreshMenu() {

    // Build menu
    // We have a default menu for before the sync finishes, but as soon as that's done we
    // build the menu appropriately based on the user's own perspective choices.

    perspectiveList = new DataModelHelper(getApplicationContext()).getPerspectives(false);

    Menu menu = navigationview_main_drawer.getMenu();
    menu.clear();

    for (Perspective menuItem : perspectiveList) {
      menu.add(R.id.menugroup_main_drawer, menuItem.menuID, Menu.NONE, menuItem.name);
      menu.findItem(menuItem.menuID).setIcon(menuItem.icon).setCheckable(true);
    }

    // Re-select the current perspective in the menu
    navigationview_main_drawer.setCheckedItem(perspective.menuID);
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
        drawerlayout_main.openDrawer(GravityCompat.START);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {

    if (drawerlayout_main.isDrawerOpen(GravityCompat.START)) {
      drawerlayout_main.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onItemListInteraction(@NonNull Entry entry, @Nullable Perspective perspective) {

    newFragment = ListFragment.newInstance(perspective, entry);

    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
    ft.replace(R.id.framelayout_main_container, newFragment);
    ft.addToBackStack(null);
    ft.commit();

    currentFragment = newFragment;

  }

  @Override
  public void onItemDetailInteraction(@NonNull Entry entry, @Nullable Perspective perspective) {

    Intent intent = new Intent(this, DetailActivity.class);
    intent.putExtra("ENTRY", entry);
    intent.putExtra("PERSPECTIVE", perspective);
    startActivity(intent);

  }

  private void changeColors(Perspective oldPerspective) {

    // Get the current header colour
    int colorFrom = ResourcesCompat.getColor(getResources(), oldPerspective.color, null);

    // Set the navigationview_main_drawer highlight color to the current perspective's
    navigationview_main_drawer.setItemTextColor(ResourcesCompat.getColorStateList(getResources(),
        perspective.colorStateListID, null));

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
  }

  private void registerSyncs() {

    Account account = new AccountManagerHelper(getApplicationContext()).getAccount();
    String authority = getString(R.string.sync_authority);

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

      drawerlayout_main.closeDrawer(GravityCompat.START);
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