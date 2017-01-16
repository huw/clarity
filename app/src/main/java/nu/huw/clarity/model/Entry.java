package nu.huw.clarity.model;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
  public int getViewType(@Nullable Perspective perspective) {
    throw new UnsupportedOperationException("Subclass must override getViewType()");
  }

  /**
   * Return a string resource given a count and perspective, such that the number displayed
   * depends on the perspective and whether there are any items in the count
   */
  public String getCountString(android.content.Context androidContext,
      @Nullable Perspective perspective) {

    if (perspective == null || perspective.filterStatus == null) {
      if (countChildren > 0) {
        return androidContext.getString(R.string.listitem_countchildren, countChildren);
      } else {
        return androidContext.getString(R.string.listitem_countchildrenempty);
      }
    }

    switch (perspective.filterStatus) {
      case "complete":
        if (countCompleted > 0) {
          return androidContext.getString(R.string.listitem_countcompleted, countCompleted);
        } else {
          return androidContext.getString(R.string.listitem_countcompletedempty);
        }
      case "incomplete":
        if (countRemaining > 0) {
          return androidContext.getString(R.string.listitem_countremaining, countRemaining);
        } else {
          return androidContext.getString(R.string.listitem_countremainingempty);
        }
      case "due":
        if (countAvailable > 0) {
          return androidContext.getString(R.string.listitem_countavailable, countAvailable);
        } else {
          return androidContext.getString(R.string.listitem_countavailableempty);
        }
      default:
        if (countChildren > 0) {
          return androidContext.getString(R.string.listitem_countchildren, countChildren);
        } else {
          return androidContext.getString(R.string.listitem_countchildrenempty);
        }
    }
  }

  /**
   * Return a string of the form 'x due soon', depending on locale
   */
  public String getCountDueSoonString(android.content.Context androidContext) {
    if (countDueSoon > 0) {
      return androidContext.getString(R.string.listitem_countduesoon, countDueSoon);
    }
    return null;
  }

  /**
   * Return a string of the form 'x overdue', depending on locale
   */
  public String getCountOverdueString(android.content.Context androidContext) {
    if (countOverdue > 0) {
      return androidContext.getString(R.string.listitem_countoverdue, countOverdue);
    }
    return null;
  }

  /**
   * When displaying items in a list, sometimes we have to set a different text style on their names
   *
   * @return int Anything like Typeface.BOLD (e.g. Typeface.BOLD_ITALIC, Typeface.NORMAL, etc.)
   */
  public int getNameTextStyle() {
    if (headerRow) {
      return Typeface.BOLD;
    } else {
      return Typeface.NORMAL;
    }
  }
}
