package nu.huw.clarity.db;

import android.provider.BaseColumns;
import android.util.Log;

/**
 * The contract class maintains a useful set of constants for labels and
 * other stuff in the database. It makes it a little easier to handle
 * things like column names, especially if you're changing them around
 * (which might need to happen with something like OmniFocus, you never
 * really know).
 *
 * This shouldn't be instantiated, and will log a warning if you do.
 */
public class DatabaseContract {

    public DatabaseContract() { Log.w("DatabaseContract", "Database contract instantiated?"); }

    public static class SQLKeyValue {
        public String name;
        public String val;

        public SQLKeyValue(String name, String defaultValue) {
            this.name = name;
            this.val = defaultValue;
        }

        public SQLKeyValue(String name) {
            this.name = name;
            this.val = null;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public abstract static class Table {
        SQLKeyValue[] keys;
        String TABLE_NAME;
    }

    public static abstract class Base implements BaseColumns {
        public static final String TABLE_NAME = "";
        public static final SQLKeyValue COLUMN_ID = new SQLKeyValue("id");
        public static final SQLKeyValue COLUMN_DATE_ADDED = new SQLKeyValue("dateAdded");
        public static final SQLKeyValue COLUMN_DATE_MODIFIED = new SQLKeyValue("dateModified");

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED};
    }

    public static abstract class Entry extends Base {
        public static final SQLKeyValue COLUMN_ACTIVE = new SQLKeyValue("active", "1");                                   // Local activity setting, independent of activeEffective
        public static final SQLKeyValue COLUMN_ACTIVE_EFFECTIVE = new SQLKeyValue("activeEffective", "1");                // Is it active/inactive because a parent is active?
        public static final SQLKeyValue COLUMN_COUNT_AVAILABLE = new SQLKeyValue("countAvailable");
        public static final SQLKeyValue COLUMN_COUNT_CHILDREN = new SQLKeyValue("countChildren");
        public static final SQLKeyValue COLUMN_COUNT_COMPLETED = new SQLKeyValue("countCompleted");
        public static final SQLKeyValue COLUMN_COUNT_DUE_SOON = new SQLKeyValue("countDueSoon");
        public static final SQLKeyValue COLUMN_COUNT_OVERDUE = new SQLKeyValue("countOverdue");
        public static final SQLKeyValue COLUMN_COUNT_REMAINING = new SQLKeyValue("countRemaining");
        public static final SQLKeyValue COLUMN_HAS_CHILDREN = new SQLKeyValue("hasChildren", "0");
        public static final SQLKeyValue COLUMN_NAME = new SQLKeyValue("name");
        public static final SQLKeyValue COLUMN_PARENT_ID = new SQLKeyValue("parentID");
        public static final SQLKeyValue COLUMN_RANK = new SQLKeyValue("rank", "0");                                       // Big-endian number, converts to long, used for ordering

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_ACTIVE, COLUMN_ACTIVE_EFFECTIVE,
                COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
                COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_COUNT_REMAINING,
                COLUMN_HAS_CHILDREN, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK};
    }

    public static abstract class Attachments extends Base {
        public static final String TABLE_NAME = "Attachments";
        public static final SQLKeyValue COLUMN_NAME = new SQLKeyValue("name");
        public static final SQLKeyValue COLUMN_PARENT_ID = new SQLKeyValue("parentID");
        public static final SQLKeyValue COLUMN_PATH = new SQLKeyValue("path");                                            // The local path which contains the file
        public static final SQLKeyValue COLUMN_PNG_PREVIEW = new SQLKeyValue("previewPNGData");

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_NAME, COLUMN_PATH, COLUMN_PARENT_ID,
                COLUMN_PNG_PREVIEW};
    }

    public static abstract class Contexts extends Entry {
        public static final String TABLE_NAME = "Contexts";
        public static final SQLKeyValue COLUMN_ALTITUDE = new SQLKeyValue("altitude");                                    // Altitude for geofencing
        public static final SQLKeyValue COLUMN_LATITUDE = new SQLKeyValue("latitude");                                    // Latitude for geofencing
        public static final SQLKeyValue COLUMN_LOCATION_NAME = new SQLKeyValue("locationName");                           // User-editable context location name
        public static final SQLKeyValue COLUMN_LONGITUDE = new SQLKeyValue("longitude");
        public static final SQLKeyValue COLUMN_ON_HOLD = new SQLKeyValue("onHold", "0");
        public static final SQLKeyValue COLUMN_RADIUS = new SQLKeyValue("radius");                                        // Radius around co-ords for geofence

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_ACTIVE, COLUMN_ACTIVE_EFFECTIVE,
                COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
                COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_COUNT_REMAINING,
                COLUMN_HAS_CHILDREN, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK, COLUMN_ALTITUDE,
                COLUMN_LATITUDE, COLUMN_LOCATION_NAME, COLUMN_LONGITUDE, COLUMN_ON_HOLD,
                COLUMN_RADIUS};
    }

    public static abstract class Folders extends Entry {
        public static final String TABLE_NAME = "Folders";
    }

    public static abstract class Perspectives extends Base {
        public static final String TABLE_NAME = "Perspectives";
        public static final SQLKeyValue COLUMN_VALUE = new SQLKeyValue("valueData");                                      // Contains a bunch of data about the perspective TODO: Expand

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_VALUE};
    }

    public static abstract class Settings extends Base {
        public static final String TABLE_NAME = "Settings";
        public static final SQLKeyValue COLUMN_VALUE = new SQLKeyValue("valueData");                                      // Keys and values for the setting and type TODO: Expand

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_VALUE};
    }

    public static abstract class Tasks extends Entry {
        public static final String TABLE_NAME = "Tasks";
        public static final SQLKeyValue COLUMN_BLOCKED = new SQLKeyValue("blocked", "0");                                 // Is this in a sequential project and are there things in front of it?
        public static final SQLKeyValue COLUMN_BLOCKED_BY_DEFER = new SQLKeyValue("blockedByDefer", "0");                 // Will always set 'blocked' to true
        public static final SQLKeyValue COLUMN_COMPLETE_WITH_CHILDREN = new SQLKeyValue("completeWithChildren", "0");
        public static final SQLKeyValue COLUMN_CONTEXT = new SQLKeyValue("context");
        public static final SQLKeyValue COLUMN_DATE_COMPLETED = new SQLKeyValue("dateCompleted");
        public static final SQLKeyValue COLUMN_DATE_DEFER = new SQLKeyValue("dateDefer");
        public static final SQLKeyValue COLUMN_DATE_DEFER_EFFECTIVE = new SQLKeyValue("dateDeferEffective");
        public static final SQLKeyValue COLUMN_DATE_DUE = new SQLKeyValue("dateDue");
        public static final SQLKeyValue COLUMN_DATE_DUE_EFFECTIVE = new SQLKeyValue("dateDueEffective");
        public static final SQLKeyValue COLUMN_DUE_SOON = new SQLKeyValue("isDueSoon", "0");
        public static final SQLKeyValue COLUMN_ESTIMATED_TIME = new SQLKeyValue("estimatedInMinutes");                    // Always a number
        public static final SQLKeyValue COLUMN_FLAGGED = new SQLKeyValue("flagged", "0");
        public static final SQLKeyValue COLUMN_FLAGGED_EFFECTIVE = new SQLKeyValue("flaggedEffective", "0");              // Effectively flagged because a parent is flagged
        public static final SQLKeyValue COLUMN_INBOX = new SQLKeyValue("inInbox", "0");
        public static final SQLKeyValue COLUMN_NEXT = new SQLKeyValue("nextTask");                                        // The first available child of this task/project
        public static final SQLKeyValue COLUMN_NOTE_PLAINTEXT = new SQLKeyValue("notePlaintext");                         // Note in plaintext
        public static final SQLKeyValue COLUMN_NOTE_XML = new SQLKeyValue("noteXML");                                     // Note in weird Omni XML format
        public static final SQLKeyValue COLUMN_OVERDUE = new SQLKeyValue("isOverdue", "0");
        public static final SQLKeyValue COLUMN_PROJECT = new SQLKeyValue("isProject", "0");
        public static final SQLKeyValue COLUMN_PROJECT_LAST_REVIEW = new SQLKeyValue("projectReviewDateLast");
        public static final SQLKeyValue COLUMN_PROJECT_NEXT_REVIEW = new SQLKeyValue("projectReviewDateNext");
        public static final SQLKeyValue COLUMN_PROJECT_REPEAT_REVIEW = new SQLKeyValue("projectReviewInterval");
        public static final SQLKeyValue COLUMN_PROJECT_STATUS = new SQLKeyValue("projectStatus", "active");
        public static final SQLKeyValue COLUMN_REPETITION_METHOD = new SQLKeyValue("repetitionMethod");
        public static final SQLKeyValue COLUMN_REPETITION_RULE = new SQLKeyValue("repetitionRule");
        public static final SQLKeyValue COLUMN_TYPE = new SQLKeyValue("type", "sequential");                              // sequential, parallel or single action

        public static final SQLKeyValue[] keys = {COLUMN_ID, COLUMN_DATE_ADDED,
                COLUMN_DATE_MODIFIED, COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN,
                COLUMN_COUNT_COMPLETED, COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE,
                COLUMN_COUNT_REMAINING, COLUMN_HAS_CHILDREN, COLUMN_NAME, COLUMN_PARENT_ID,
                COLUMN_RANK, COLUMN_BLOCKED, COLUMN_BLOCKED_BY_DEFER, COLUMN_COMPLETE_WITH_CHILDREN,
                COLUMN_CONTEXT, COLUMN_DATE_COMPLETED, COLUMN_DATE_DEFER,
                COLUMN_DATE_DEFER_EFFECTIVE, COLUMN_DATE_DUE, COLUMN_DATE_DUE_EFFECTIVE,
                COLUMN_DUE_SOON, COLUMN_ESTIMATED_TIME, COLUMN_FLAGGED, COLUMN_FLAGGED_EFFECTIVE,
                COLUMN_INBOX, COLUMN_NEXT, COLUMN_NOTE_PLAINTEXT, COLUMN_NOTE_XML, COLUMN_OVERDUE,
                COLUMN_PROJECT, COLUMN_PROJECT_LAST_REVIEW, COLUMN_PROJECT_NEXT_REVIEW,
                COLUMN_PROJECT_REPEAT_REVIEW, COLUMN_PROJECT_STATUS, COLUMN_REPETITION_METHOD,
                COLUMN_REPETITION_RULE, COLUMN_TYPE};
    }
}
