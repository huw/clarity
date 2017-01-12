package nu.huw.clarity.db;

import android.provider.BaseColumns;
import android.util.Log;

/**
 * The contract class maintains a useful set of constants for labels and other stuff in the
 * database. It makes it a little easier to handle things like column names, especially if you're
 * changing them around (which might need to happen with something like OmniFocus, you never really
 * know).
 *
 * This shouldn't be instantiated, and will log a warning if you do.
 */
public class DatabaseContract {

  public DatabaseContract() {
    Log.w("DatabaseContract", "Database contract is static and should not be instantiated");
  }

  public static abstract class Table {

    String TABLE_NAME;
  }

  public static abstract class Base implements BaseColumns {

    public static final String TABLE_NAME = "";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE_ADDED = "dateAdded";
    public static final String COLUMN_DATE_MODIFIED = "dateModified";
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED};
  }

  public static abstract class Entries extends Base {

    public static final String COLUMN_COUNT_AVAILABLE = "countAvailable";
    public static final String COLUMN_COUNT_CHILDREN = "countChildren";
    public static final String COLUMN_COUNT_COMPLETED = "countCompleted";
    public static final String COLUMN_COUNT_DUE_SOON = "countDueSoon";
    public static final String COLUMN_COUNT_OVERDUE = "countOverdue";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PARENT_ID = "parentID";
    public static final String COLUMN_RANK = "rank"; // Used for ordering, finnicky format
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
        COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK};
  }

  public static abstract class Attachments extends Base {

    public static final String TABLE_NAME = "Attachments";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PARENT_ID = "parentID";
    public static final String COLUMN_PATH = "path"; // The local path which contains the file
    public static final String COLUMN_PNG_PREVIEW = "previewPNGData";
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_NAME, COLUMN_PATH, COLUMN_PARENT_ID, COLUMN_PNG_PREVIEW};
    public static final String CREATE = String.format(
        "CREATE TABLE Attachments(%s PRIMARY KEY,%s,%s,%s,%s,%s,%s)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_NAME, COLUMN_PATH,
        COLUMN_PARENT_ID, COLUMN_PNG_PREVIEW);
  }

  public static abstract class Contexts extends Entries {

    public static final String TABLE_NAME = "Contexts";
    public static final String COLUMN_ALTITUDE = "altitude"; // Altitude for geofencing
    public static final String COLUMN_DROPPED = "dropped";
    public static final String COLUMN_DROPPED_EFFECTIVE = "droppedEffective";
    public static final String COLUMN_LATITUDE = "latitude"; // Latitude for geofencing
    public static final String COLUMN_LOCATION_NAME = "locationName"; // User-editable context location name
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ON_HOLD = "onHold";
    public static final String COLUMN_RADIUS = "radius"; // Radius around co-ords for geofence
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
        COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK,
        COLUMN_ALTITUDE, COLUMN_DROPPED, COLUMN_DROPPED_EFFECTIVE, COLUMN_LATITUDE,
        COLUMN_LOCATION_NAME, COLUMN_LONGITUDE, COLUMN_ON_HOLD, COLUMN_RADIUS};
    public static final String CREATE = String.format(
        "CREATE TABLE Contexts(%s PRIMARY KEY,%s,%s,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s,%s,%s INT DEFAULT 0,%s,%s BOOLEAN DEFAULT 1,%s BOOLEAN DEFAULT 1,%s,%s,%s,%s BOOLEAN DEFAULT 0,%s)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_COUNT_AVAILABLE,
        COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED, COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE,
        COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK, COLUMN_ALTITUDE, COLUMN_DROPPED,
        COLUMN_DROPPED_EFFECTIVE, COLUMN_LATITUDE, COLUMN_LOCATION_NAME, COLUMN_LONGITUDE,
        COLUMN_ON_HOLD, COLUMN_RADIUS);
  }

  public static abstract class Folders extends Entries {

    public static final String TABLE_NAME = "Folders";
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
        COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK};
    public static final String CREATE = String.format(
        "CREATE TABLE Folders(%s PRIMARY KEY,%s,%s,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s,%s,%s INT DEFAULT 0)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_COUNT_AVAILABLE,
        COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED, COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE,
        COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK);
  }

  public static abstract class Perspectives extends Base {

    public static final String TABLE_NAME = "Perspectives";
    public static final String COLUMN_FILTER_DURATION = "actionDurationFilter";
    public static final String COLUMN_FILTER_FLAGGED = "actionFlaggedFilter";
    public static final String COLUMN_FILTER_STATUS = "actionCompletionFilter";
    public static final String COLUMN_GROUP = "collation";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SORT = "sort";
    public static final String COLUMN_VALUE = "valueData";
    public static final String COLUMN_VIEW_MODE = "viewMode";
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_FILTER_DURATION, COLUMN_FILTER_FLAGGED, COLUMN_FILTER_STATUS, COLUMN_GROUP,
        COLUMN_ICON, COLUMN_NAME, COLUMN_SORT, COLUMN_VALUE, COLUMN_VIEW_MODE};
    public static final String CREATE = String.format(
        "CREATE TABLE Perspectives(%s PRIMARY KEY,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_FILTER_DURATION,
        COLUMN_FILTER_FLAGGED, COLUMN_FILTER_STATUS, COLUMN_GROUP, COLUMN_ICON, COLUMN_NAME,
        COLUMN_SORT, COLUMN_VALUE, COLUMN_VIEW_MODE);
  }

  public static abstract class Settings extends Base {

    public static final String TABLE_NAME = "Settings";
    public static final String COLUMN_VALUE = "valueData";
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_VALUE};
    public static final String CREATE = String.format(
        "CREATE TABLE Settings(%s PRIMARY KEY,%s,%s,%s)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_VALUE);
  }

  public static abstract class Tasks extends Entries {

    // Note about effective attributes:
    // If dateDue is set, dateDueEffective will always be set
    // If dateDueEffective is set, dateDue will not necessarily be set

    public static final String TABLE_NAME = "Tasks";
    public static final String COLUMN_BLOCKED = "blocked"; // Is this in a sequential project and are there things in front of it?
    public static final String COLUMN_DEFERRED = "blockedByDefer"; // Will always set 'blocked' to true
    public static final String COLUMN_COMPLETE_WITH_CHILDREN = "completeWithChildren";
    public static final String COLUMN_CONTEXT = "context";
    public static final String COLUMN_DATE_COMPLETED = "dateCompleted";
    public static final String COLUMN_DATE_DEFER = "dateDefer";
    public static final String COLUMN_DATE_DEFER_EFFECTIVE = "dateDeferEffective";
    public static final String COLUMN_DATE_DUE = "dateDue";
    public static final String COLUMN_DATE_DUE_EFFECTIVE = "dateDueEffective";
    public static final String COLUMN_DUE_SOON = "isDueSoon";
    public static final String COLUMN_DROPPED = "dropped";
    public static final String COLUMN_ESTIMATED_TIME = "estimatedInMinutes"; // Always a number
    public static final String COLUMN_FLAGGED = "flagged";
    public static final String COLUMN_FLAGGED_EFFECTIVE = "flaggedEffective"; // Effectively flagged because a parent is flagged
    public static final String COLUMN_INBOX = "inInbox";
    public static final String COLUMN_NEXT = "nextTask"; // The first available child of this task/project
    public static final String COLUMN_NOTE_PLAINTEXT = "notePlaintext"; // Note in plaintext
    public static final String COLUMN_NOTE_XML = "noteXML"; // Note in weird Omni XML format
    public static final String COLUMN_OVERDUE = "isOverdue";
    public static final String COLUMN_PROJECT = "isProject";
    public static final String COLUMN_PROJECT_ID = "projectID";
    public static final String COLUMN_PROJECT_LAST_REVIEW = "projectReviewDateLast";
    public static final String COLUMN_PROJECT_NEXT_REVIEW = "projectReviewDateNext";
    public static final String COLUMN_PROJECT_REPEAT_REVIEW = "projectReviewInterval";
    public static final String COLUMN_PROJECT_STATUS = "projectStatus";
    public static final String COLUMN_REPETITION_METHOD = "repetitionMethod";
    public static final String COLUMN_REPETITION_RULE = "repetitionRule";
    public static final String COLUMN_TYPE = "type"; // sequential, parallel or single action
    public static final String[] columns = {COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED,
        COLUMN_COUNT_AVAILABLE, COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED,
        COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE, COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK,
        COLUMN_BLOCKED, COLUMN_DEFERRED, COLUMN_COMPLETE_WITH_CHILDREN, COLUMN_CONTEXT,
        COLUMN_DATE_COMPLETED, COLUMN_DATE_DEFER, COLUMN_DATE_DEFER_EFFECTIVE, COLUMN_DATE_DUE,
        COLUMN_DATE_DUE_EFFECTIVE, COLUMN_DUE_SOON, COLUMN_DROPPED, COLUMN_ESTIMATED_TIME,
        COLUMN_FLAGGED, COLUMN_FLAGGED_EFFECTIVE, COLUMN_INBOX, COLUMN_NEXT, COLUMN_NOTE_PLAINTEXT,
        COLUMN_NOTE_XML, COLUMN_OVERDUE, COLUMN_PROJECT, COLUMN_PROJECT_ID,
        COLUMN_PROJECT_LAST_REVIEW, COLUMN_PROJECT_NEXT_REVIEW, COLUMN_PROJECT_REPEAT_REVIEW,
        COLUMN_PROJECT_STATUS, COLUMN_REPETITION_METHOD, COLUMN_REPETITION_RULE, COLUMN_TYPE};
    public static final String CREATE = String.format(
        "CREATE TABLE Tasks(%s PRIMARY KEY,%s,%s,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s INT DEFAULT 0,%s,%s,%s INT DEFAULT 0,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s,%s,%s,%s,%s,%s,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s,%s,%s,%s BOOLEAN DEFAULT 0,%s BOOLEAN DEFAULT 0,%s,%s,%s,%s,%s TEXT DEFAULT active,%s,%s,%s TEXT DEFAULT sequential)",
        COLUMN_ID, COLUMN_DATE_ADDED, COLUMN_DATE_MODIFIED, COLUMN_COUNT_AVAILABLE,
        COLUMN_COUNT_CHILDREN, COLUMN_COUNT_COMPLETED, COLUMN_COUNT_DUE_SOON, COLUMN_COUNT_OVERDUE,
        COLUMN_NAME, COLUMN_PARENT_ID, COLUMN_RANK, COLUMN_BLOCKED, COLUMN_DEFERRED,
        COLUMN_COMPLETE_WITH_CHILDREN, COLUMN_CONTEXT, COLUMN_DATE_COMPLETED, COLUMN_DATE_DEFER,
        COLUMN_DATE_DEFER_EFFECTIVE, COLUMN_DATE_DUE, COLUMN_DATE_DUE_EFFECTIVE, COLUMN_DUE_SOON,
        COLUMN_DROPPED, COLUMN_ESTIMATED_TIME, COLUMN_FLAGGED, COLUMN_FLAGGED_EFFECTIVE,
        COLUMN_INBOX, COLUMN_NEXT, COLUMN_NOTE_PLAINTEXT, COLUMN_NOTE_XML, COLUMN_OVERDUE,
        COLUMN_PROJECT, COLUMN_PROJECT_ID, COLUMN_PROJECT_LAST_REVIEW, COLUMN_PROJECT_NEXT_REVIEW,
        COLUMN_PROJECT_REPEAT_REVIEW, COLUMN_PROJECT_STATUS, COLUMN_REPETITION_METHOD,
        COLUMN_REPETITION_RULE, COLUMN_TYPE);
  }
}
