package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;

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
    public double  altitude;
    public double  latitude;
    public String  locationName;
    public double  longitude;
    public boolean onHold;
    public double  radius;

    public Context() {}

    protected Context(Parcel in) {

        super(in);
        altitude = in.readDouble();
        latitude = in.readDouble();
        locationName = in.readString();
        longitude = in.readDouble();
        onHold = in.readInt() != 0;
        radius = in.readDouble();
    }

    @Override public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);
        out.writeDouble(altitude);
        out.writeDouble(latitude);
        out.writeString(locationName);
        out.writeDouble(longitude);
        out.writeInt(onHold ? 1 : 0);
        out.writeDouble(radius);
    }

    public @Override Entry getParent(android.content.Context androidContext) {

        if (parent == null) {
            DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
            parent = dataModelHelper.getContextFromID(parentID);
        }
        return parent;
    }
}
