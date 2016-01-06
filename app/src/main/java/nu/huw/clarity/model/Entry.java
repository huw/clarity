package nu.huw.clarity.model;

import java.util.Date;

/**
 * A basic entry. This is basically anything which belongs in
 * the OmniFocus tree (so not a Perspective or Setting). It
 * includes Folders, Contexts, Tasks and Projects. See Base
 * for the inheritance structure.
 */
public class Entry extends Base {

    public String mName;
    public String mParentID;
    public int mRank;

    public Entry(String ID, Date dateAdded, Date dateModified,

            String name,
            String parentID,
            int rank
    ) {
        super(ID, dateAdded, dateModified);

        mName = name;
        mParentID = parentID;
        mRank = rank;
    }
}
