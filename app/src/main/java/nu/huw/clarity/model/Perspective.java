package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StyleRes;

/**
 * Holds data about a perspective which can be used for organisation
 */
public class Perspective extends Base {

  public static final Parcelable.Creator<Perspective> CREATOR =
      new Parcelable.Creator<Perspective>() {
        public Perspective createFromParcel(Parcel in) {

          return new Perspective(in);
        }

        public Perspective[] newArray(int size) {

          return new Perspective[size];
        }
      };
  @ColorRes
  public int colorID;
  @ColorRes
  public int colorStateListID;
  @DrawableRes
  public int cursorDrawable;
  @DrawableRes
  public int icon;
  @IdRes
  public int menuID;
  @StyleRes
  public int themeID;
  public String filterDuration;
  public String filterFlagged;
  public String filterStatus;
  public String collation;
  public String name;
  public String sort;
  public String value;
  public String viewMode;

  public Perspective() {
  }

  private Perspective(Parcel in) {

    super(in);

    // res
    colorID = in.readInt();
    colorStateListID = in.readInt();
    icon = in.readInt();
    menuID = in.readInt();
    themeID = in.readInt();

    // properties
    filterDuration = in.readString();
    filterFlagged = in.readString();
    filterStatus = in.readString();
    collation = in.readString();
    name = in.readString();
    sort = in.readString();
    value = in.readString();
    viewMode = in.readString();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);

    // res
    out.writeInt(colorID);
    out.writeInt(colorStateListID);
    out.writeInt(icon);
    out.writeInt(menuID);
    out.writeInt(themeID);

    // properties
    out.writeString(filterDuration);
    out.writeString(filterFlagged);
    out.writeString(filterStatus);
    out.writeString(collation);
    out.writeString(name);
    out.writeString(sort);
    out.writeString(value);
    out.writeString(viewMode);
  }

  /**
   * Compare perspectives based on their IDs rather than object references
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Perspective && id.equals(((Perspective) obj).id);
  }
}
