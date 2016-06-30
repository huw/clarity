package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

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
    public Date   added;
    public Date   modified;

    public Base() {}

    // Constructor for parcels
    protected Base(Parcel in) {

        id = in.readString();
        added = getDateOrNull(in.readLong());
        modified = getDateOrNull(in.readLong());
    }

    // Write the object data to the parcel
    @Override public void writeToParcel(Parcel out, int flags) {

        out.writeString(id);
        out.writeLong(getTimeOrNull(added));
        out.writeLong(getTimeOrNull(modified));
    }

    @Override public int describeContents() {return 0;}

    protected long getTimeOrNull(Date d) {

        if (d == null) {
            return -1;
        } else {
            return d.getTime();
        }
    }

    protected Date getDateOrNull(long l) {

        if (l == -1) {
            return null;
        } else {
            return new Date(l);
        }
    }
}
