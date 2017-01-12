package nu.huw.clarity.model;

import android.support.annotation.NonNull;
import nu.huw.clarity.db.model.DataModelHelper;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

public class Settings {

  private final String ID_DEFAULT_DUE = "DefaultDueTime";
  private final String ID_DEFAULT_DEFER = "DefaultStartTime";
  private final String ID_DUE_SOON_DURATION = "DueSoonInterval";

  private DataModelHelper dataModelHelper;

  public Settings(@NonNull android.content.Context androidContext) {
    this.dataModelHelper = new DataModelHelper(androidContext);
  }

  /**
   * Get a LocalTime object representing the default time in your time zone that tasks are due at
   */
  @NonNull
  public LocalTime getDefaultDueTime() {
    String value = dataModelHelper.getSettingFromID(ID_DEFAULT_DUE);
    return LocalTime.parse(value);
  }

  /**
   * Get a LocalTime object representing the default time in your time zone that tasks are deferred
   * to
   */
  @NonNull
  public LocalTime getDefaultDeferTime() {
    String value = dataModelHelper.getSettingFromID(ID_DEFAULT_DEFER);
    return LocalTime.parse(value);
  }

  /**
   * Get a Duration object representing the time we consider something 'due soon' in
   */
  @NonNull
  public Duration getDueSoonDuration() {
    String value = dataModelHelper.getSettingFromID(ID_DUE_SOON_DURATION);
    return Duration.parse(value);
  }
}
