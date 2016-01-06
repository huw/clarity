package nu.huw.clarity.model;

import java.util.Date;

public class Context extends Entry {

    public boolean mOnHold;
    public boolean mActive;

    public long mAltitude;
    public long mLatitude;
    public long mLongitude;
    public long mRadius;
    public String mLocationName;

    public Context(String id, Date dateAdded, Date dateModified, String name, String parentID,
                   int rank,

            boolean onHold,
            boolean active,
            long altitude,
            long latitude,
            long longitude,
            long radius,
            String locationName
    ) {
        super(id, dateAdded, dateModified, name, parentID, rank);

        mOnHold = onHold;
        mActive = active;
        mAltitude = altitude;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mLocationName = locationName;
    }

}
