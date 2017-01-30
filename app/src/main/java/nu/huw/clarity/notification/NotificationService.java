package nu.huw.clarity.notification;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import java.util.List;
import nu.huw.clarity.R;
import nu.huw.clarity.db.TreeOperations;
import nu.huw.clarity.db.model.DataModelHelper;
import nu.huw.clarity.db.model.NoteHelper;
import nu.huw.clarity.model.Task;

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

  /**
   * An AsyncTask that firstly updates the overdue/due soon status of every item in the database,
   * and secondly displays notifications for overdue tasks.
   */
  private class NotificationTask extends AsyncTask<Void, Void, List<Task>> {

    /**
     * Update the database in a background thread. Return a list of tasks which are now overdue.
     */
    @Override
    protected List<Task> doInBackground(Void... params) {

      Context androidContext = getApplicationContext();
      TreeOperations treeOperations = new TreeOperations(androidContext);
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);

      // We have to get the list of newly overdued tasks first, then update all their overdue flags.
      // In that order.

      List<Task> tasks = dataModelHelper.getNewOverdueTasks();
      treeOperations.updateDueSoonAndOverdue();

      return tasks;
    }

    /**
     * Create notifications, and call stopSelf() afterward. This is necessary to release the wake
     * lock and kill the service.
     */
    @Override
    protected void onPostExecute(List<Task> tasks) {

      for (Task task : tasks) {

        // Initialise notification

        NotificationCompat.Builder builder = new Builder(getApplicationContext())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.name)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setGroup(GROUP_KEY_OVERDUE);

        // Set content text

        String notification_duenow = getApplicationContext()
            .getString(R.string.notification_duenow);
        builder.setContentText(notification_duenow);

        // Set an expandable style with the task's note text if necessary
        // Note that the original 'due now' text remains

        if (task.noteXML != null) {

          String expandedText =
              notification_duenow + " • " + NoteHelper.noteXMLtoString(task.noteXML);
          builder.setStyle(new BigTextStyle().bigText(expandedText));

        }

        // Set intents
        // TODO

      }

      stopSelf();
    }
  }
}
