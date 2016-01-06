package nu.huw.clarity.model;

import android.util.Xml;

import java.util.Date;

/**
 * TODO: Read in plist data properly
 */
public class Perspective extends Base {

    public Xml mPlistData;

    public Perspective(String id, Date dateAdded, Date dateModified,

            Xml plistData
    ) {
        super(id, dateAdded, dateModified);

        mPlistData = plistData;
    }
}
