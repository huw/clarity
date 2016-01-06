package nu.huw.clarity.model;

import java.util.Date;

/**
 * Base class for entries in our data model. Here's a map of inheritances:
 *
 * - Base
 *   - Entry
 *     - Folder
 *     - Context
 *     - Task
 *       - Project
 *   - Attachment
 *   - Perspective
 *   - Setting
 */
public abstract class Base {

    public String mID;
    public Date mDateAdded;
    public Date mDateModified;

    public Base(String id, Date dateAdded, Date dateModified) {
        mID = id;
        mDateAdded = dateAdded;
        mDateModified = dateModified;
    }
}