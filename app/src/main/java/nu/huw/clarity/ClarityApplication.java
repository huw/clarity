package nu.huw.clarity;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class ClarityApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);
  }
}
