package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;

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
  public int color;
  public int colorStateListID;
  public String filterDuration;
  public String filterFlagged;
  public String filterStatus;
  public String group;
  public int icon;
  public int menuID;
  public String name;
  public String sort;
  public int themeID;
  public String value;
  public String viewMode;

  public Perspective() {
  }

  private Perspective(Parcel in) {

    super(in);
    color = in.readInt();
    colorStateListID = in.readInt();
    filterDuration = in.readString();
    filterFlagged = in.readString();
    filterStatus = in.readString();
    group = in.readString();
    icon = in.readInt();
    menuID = in.readInt();
    name = in.readString();
    themeID = in.readInt();
    sort = in.readString();
    value = in.readString();
    viewMode = in.readString();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);
    out.writeInt(color);
    out.writeInt(colorStateListID);
    out.writeString(filterDuration);
    out.writeString(filterFlagged);
    out.writeString(filterStatus);
    out.writeString(group);
    out.writeInt(icon);
    out.writeInt(menuID);
    out.writeString(name);
    out.writeInt(themeID);
    out.writeString(sort);
    out.writeString(value);
    out.writeString(viewMode);
  }
}
