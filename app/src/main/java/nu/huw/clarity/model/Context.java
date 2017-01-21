package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import nu.huw.clarity.R;
import nu.huw.clarity.db.model.DataModelHelper;

/**
 * A context, in OmniFocus, holds a number of tasks but is external from the tree. Every task
 * lists a context to which it belongs.
 */
public class Context extends Entry {

  public static final Parcelable.Creator<Context> CREATOR = new Parcelable.Creator<Context>() {
    public Context createFromParcel(Parcel in) {

      return new Context(in);
    }

    public Context[] newArray(int size) {

      return new Context[size];
    }
  };
  public double altitude;
  public boolean dropped;
  public boolean droppedEffective;
  public double latitude;
  public String locationName;
  public double longitude;
  public boolean onHold;
  public double radius;

  public Context() {
  }

  protected Context(Parcel in) {

    super(in);
    altitude = in.readDouble();
    dropped = in.readInt() > 0;
    droppedEffective = in.readInt() > 0;
    latitude = in.readDouble();
    locationName = in.readString();
    longitude = in.readDouble();
    onHold = in.readInt() > 0;
    radius = in.readDouble();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);
    out.writeDouble(altitude);
    out.writeInt(dropped ? 1 : 0);
    out.writeInt(droppedEffective ? 1 : 0);
    out.writeDouble(latitude);
    out.writeString(locationName);
    out.writeDouble(longitude);
    out.writeInt(onHold ? 1 : 0);
    out.writeDouble(radius);
  }

  public
  @Override
  Entry getParent(android.content.Context androidContext) {

    if (parent == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      parent = dataModelHelper.getContextFromID(parentID);
    }
    return parent;
  }

  @Override
  public int getViewType(@Nullable Perspective perspective) {
    return VT_CONTEXT;
  }

  /**
   * The name field is greyed out if the context is dropped or on hold and not a header row
   */
  @ColorInt
  public int getPrimaryTextColor(@NonNull android.content.Context androidContext) {
    @ColorRes int colorID = (!onHold && !droppedEffective) ? R.color.primary_text_light
        : R.color.disabled_text_light;
    return ContextCompat.getColor(androidContext, colorID);
  }

  /**
   * The count field is greyed out if the context is dropped or on hold
   */
  @ColorInt
  public int getSecondaryTextColor(@NonNull android.content.Context androidContext) {
    @ColorRes int colorID =
        (!onHold && !droppedEffective) ? R.color.secondary_text_light
            : R.color.disabled_text_light;
    return ContextCompat.getColor(androidContext, colorID);
  }
}
