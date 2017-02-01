package nu.huw.clarity.notification;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent bootIntent) {

    // Register the notification service
    // Look for new notifications every 5 minutes

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    Intent intent = new Intent(context, NotificationService.class);
    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

    alarmManager.cancel(pendingIntent);
    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 5 * 1000, 5 * 60 * 1000, pendingIntent);

  }
}
