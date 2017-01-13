package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import nu.huw.clarity.R;

/**
 * Everything in the OmniFocus tree (folders, contexts, projects, tasks) will implement these
 * methods. They're common to everything.
 */
public class Entry extends Base implements Comparable<Entry> {

  public static final int VT_TASK = 1;
  public static final int VT_CONTEXT = 2;
  public static final int VT_FOLDER = 3;
  public static final int VT_PROJECT = 4;
  public static final int VT_NESTED_TASK = 5;

  public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
    public Entry createFromParcel(Parcel in) {
      return new Entry(in);
    }
    public Entry[] newArray(int size) {
      return new Entry[size];
    }
  };
  public long countAvailable;
  public long countChildren;
  public long countCompleted;
  public long countDueSoon;
  public long countOverdue;
  public long countRemaining;
  public String name;
  public String parentID;
  public long rank;
  public boolean headerRow;
  Entry parent;

  public Entry() {
  }

  protected Entry(Parcel in) {

    super(in);
    countAvailable = in.readLong();
    countChildren = in.readLong();
    countCompleted = in.readLong();
    countDueSoon = in.readLong();
    countOverdue = in.readLong();
    name = in.readString();
    parent = in.readParcelable(Entry.class.getClassLoader());
    parentID = in.readString();
    rank = in.readLong();
    headerRow = in.readInt() != 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);
    out.writeLong(countAvailable);
    out.writeLong(countChildren);
    out.writeLong(countCompleted);
    out.writeLong(countDueSoon);
    out.writeLong(countOverdue);
    out.writeString(name);
    out.writeParcelable(parent, 0);
    out.writeString(parentID);
    out.writeLong(rank);
    out.writeInt(headerRow ? 1 : 0);
  }

  @Override
  public int compareTo(@NonNull Entry entry) {

    // Checking is done this way, in case type casting to an int creates a problem

    if (rank < entry.rank) {
      return -1;
    } else if (rank > entry.rank) {
      return 1;
    }

    return 0;
  }

  public Entry getParent(android.content.Context androidContext) {
    throw new UnsupportedOperationException("Subclass must override getParent()");
  }

  /**
   * Return the view type integer for use with the ListAdapter
   */
  public int getViewType() {
    throw new UnsupportedOperationException("Subclass must override getViewType()");
  }

  /**
   * Return the correct count of children given a string representing a perspective's status filter
   */
  public long getCount(@Nullable String filterStatus) {
    if (filterStatus == null) return countChildren;
    switch (filterStatus) {
      case "complete":
        return countCompleted;
      case "incomplete":
        return countRemaining;
      case "due":
        return countAvailable;
      default:
        return countChildren;
    }
  }

  @StringRes
  public int getCountString(long count, @Nullable String filterStatus) {
    if (filterStatus == null) return count > 0 ? R.string.children : R.string.no_children;
    switch (filterStatus) {
      case "complete":
        return count > 0 ? R.string.completed : R.string.no_completed;
      case "incomplete":
        return count > 0 ? R.string.remaining : R.string.no_remaining;
      case "due":
        return count > 0 ? R.string.available : R.string.no_available;
      default:
        return count > 0 ? R.string.children : R.string.no_children;
    }
  }
}
