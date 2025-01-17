package nu.huw.clarity.notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.db.TreeOperations;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.db.model.NoteHelper;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.activity.MainActivity;
import org.threeten.bp.ZoneId;

/**
 * Thanks to: http://it-ride.blogspot.com.au/2010/10/android-implementing-notification.html
 *
 * Note that the alternative to this could be something like registering an alarm for each
 * notification when we set it to be due. Since we need to be updating time-sensitive database info
 * any (dues and overdues), we may as well do this—but it's also better to have a Service running
 * this way to help with power management.
 */
public class NotificationService extends Service {

  private static final String TAG = NotificationService.class.getName();
  private static final String GROUP_KEY_OVERDUE = "group_key_overdue";
  private WakeLock wakeLock;

  /**
   * Return null, since the service doesn't communicate elsewhere.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * Initialise the service stuff, then run the actual code in a background thread
   */
  private void handleIntent(Intent intent) {

    // Obtain a wake lock

    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    wakeLock.acquire();

    // Do work in an AsyncTask

    new NotificationTask().execute();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleIntent(intent);
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    wakeLock.release();
  }

  /**
   * An AsyncTask that firstly updates the overdue/due soon status of every item in the database,
   * and secondly displays notifications for overdue tasks.
   */
  public class NotificationTask extends AsyncTask<Void, Void, List<Task>> {

    DataModelHelper dataModelHelper = new DataModelHelper(getApplicationContext());
    Bitmap background_wearable = BitmapFactory
        .decodeResource(getResources(), R.drawable.background_wearable);

    /**
     * Update the database in a background thread. Return a list of tasks which are now overdue.
     */
    @Override
    protected List<Task> doInBackground(Void... params) {

      TreeOperations treeOperations = new TreeOperations(getApplicationContext());

      // We have to get the list of newly overdued tasks first, then update all their overdue flags.
      // In that order.

      List<Task> tasks = dataModelHelper.getNewOverdueTasks();
      treeOperations.updateDueSoonAndOverdue();
      treeOperations.updateCountsFromTop();

      return tasks;
    }

    /**
     * Create notifications, and call stopSelf() afterward. This is necessary to release the wake
     * lock and kill the service.
     */
    @Override
    protected void onPostExecute(List<Task> tasks) {

      NotificationManagerCompat notificationManager = NotificationManagerCompat
          .from(getApplicationContext());
      int ID = 0;

      // Build a wearable extender to add extra functionality for wearables

      NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
          .setBackground(background_wearable);

      // Display a summary notification

      if (tasks.size() >= 4) { // default number for grouping

        String notification_duesummarytitle = getString(R.string.notification_duesummarytitle);
        String notification_duesummarytext = getString(R.string.notification_duesummarytext,
            tasks.size());

        // Result intent: Home screen

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
            .getActivity(getApplicationContext(), ID, intent, Intent.FLAG_ACTIVITY_SINGLE_TOP);

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
            .setGroup(GROUP_KEY_OVERDUE)
            .extend(wearableExtender);

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
            .setGroup(GROUP_KEY_OVERDUE)
            .extend(wearableExtender);

        // Set the notification time to the task's overdue date

        if (task.dateDueEffective != null) {
          builder.setWhen(
              task.dateDueEffective.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

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

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("ENTRY", task);
        intent.putExtra("PERSPECTIVE", dataModelHelper.getForecastPerspective());
        PendingIntent pendingIntent = PendingIntent
            .getActivity(getApplicationContext(), ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Give the notification a unique ID

        notificationManager.notify(ID, builder.build());
        ID++;
      }

      stopSelf();
    }
  }
}
