package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

/**
 * A basic class for any OmniFocus element. Everything will implement the fields in this class.
 */
public class Base implements Parcelable {

  public static final Parcelable.Creator<Base> CREATOR = new Parcelable.Creator<Base>() {
    public Base createFromParcel(Parcel in) {

      return new Base(in);
    }

    public Base[] newArray(int size) {

      return new Base[size];
    }
  };
  public String id;
  public LocalDateTime dateAdded;
  public LocalDateTime dateModified;

  public Base() {
  }

  // Constructor for parcels
  protected Base(Parcel in) {

    id = in.readString();
    dateAdded = getDateTime(in.readString());
    dateModified = getDateTime(in.readString());
  }

  // Write the object data to the parcel
  @Override
  public void writeToParcel(Parcel out, int flags) {

    out.writeString(id);
    out.writeString(getTimeString(dateAdded));
    out.writeString(getTimeString(dateModified));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  String getTimeString(@Nullable LocalDateTime d) {

    if (d == null) {
      return null;
    } else {
      return d.atZone(ZoneId.systemDefault()).toInstant().toString();
    }
  }

  String getTimeString(@Nullable LocalDate d) {

    if (d == null) {
      return null;
    } else {
      return d.toString();
    }
  }

  String getTimeString(@Nullable Duration d) {

    if (d == null) {
      return null;
    } else {
      return d.toString();
    }
  }

  LocalDateTime getDateTime(@Nullable String s) {

    if (s == null) {
      return null;
    } else {
      return LocalDateTime.ofInstant(Instant.parse(s), ZoneId.systemDefault());
    }
  }

  LocalDate getDate(@Nullable String s) {

    if (s == null) {
      return null;
    } else {
      return LocalDate.parse(s);
    }
  }

  Duration getDuration(@Nullable String s) {

    if (s == null) {
      return null;
    } else {
      return Duration.parse(s);
    }
  }
}
