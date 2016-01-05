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
class DatabaseContract {

    public DatabaseContract() { Log.w("DatabaseContract", "Database contract instantiated?"); }

    public static abstract class Attachments implements BaseColumns {
        public static final String TABLE_NAME = "Attachment";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_CONTAINING_TRANSACTION = "containingTransactionHint";     // The file under .ofocus which contains this file
        public static final String COLUMN_CONTEXT = "context";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_FOLDER = "folder";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PERSPECTIVE = "perspective";
        public static final String COLUMN_PNG_PREVIEW = "previewPNGData";
        public static final String COLUMN_TASK = "task";
    }

    public static abstract class Contexts implements BaseColumns {
        public static final String TABLE_NAME = "Context";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_ACTIVE_EFFECTIVE = "effectiveActive";
        public static final String COLUMN_ON_HOLD = "allowsNextAction";                             // 0 = On Hold, 1 = Not On Hold
        public static final String COLUMN_ALTITUDE = "altitude";                                    // Altitude for geofencing
        public static final String COLUMN_AVAILABLE_COUNT = "availableTaskCount";
        public static final String COLUMN_CHILDREN_COUNT = "childrenCount";
        public static final String COLUMN_CHILDREN_STATE = "childrenState";                         // Does this have children? 0 = No, 2 = Yes
        public static final String COLUMN_CONTAINED_COUNT = "containedTaskCount";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";                     // Possibly to do with auto-creation of contexts like 'Waiting'
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_LATITTUDE = "latitude";
        public static final String COLUMN_DUE_SOON_COUNT = "localNumberOfDueSoonTasks";             // Number of due soon in this context not including children
        public static final String COLUMN_OVERDUE_COUNT = "localNumberOfOverdueTasks";              // See above
        public static final String COLUMN_LOCATION_NAME = "locationName";                           // User-editable context location name
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NEXT_COUNT = "nextTaskCount";
        public static final String COLUMN_NOTE_PLAINTEXT = "plainTextNote";                         // Contexts can't have notes - Omni error
        public static final String COLUMN_NOTE_XML = "noteXMLData";
        public static final String COLUMN_NOTIFICATION_FLAGS = "notificationFlags";                 // Not sure. All 0s in my database.
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_RANK = "rank";                                            // Used for ordering. Seems to be a misrepresented 32-bit little endian.
        public static final String COLUMN_REMAINING_COUNT = "remainingTaskCount";
        public static final String COLUMN_TOTAL_DUE_SOON_COUNT = "totalNumberOfDueSoonTasks";       // Number of due soon in this context including children
        public static final String COLUMN_TOTAL_OVERDUE_COUNT = "totalNumberOfOverdueTasks";        // I don't need these two
    }

    public static abstract class Folders implements BaseColumns {
        public static final String TABLE_NAME = "Folder";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_ACTIVE_EFFECTIVE = "effectiveActive";
        public static final String COLUMN_AVAILABLE_COUNT = "numberOfAvailableTasks";
        public static final String COLUMN_CHILDREN_COUNT = "childrenCount";
        public static final String COLUMN_CHILDREN_STATE = "childrenState";
        public static final String COLUMN_CONTAINED_COUNT = "numberOfContainedTasks";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_DUE_SOON_COUNT = "numberOfDueSoonTasks";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NOTE_PLAINTEXT = "plainTextNote";
        public static final String COLUMN_NOTE_XML = "noteXMLData";
        public static final String COLUMN_OVERDUE_COUNT = "numberOfOverdueTasks";
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_RANK = "rank";
        public static final String COLUMN_REMAINING_COUNT = "numberOfRemainingTasks";

    }

    public static abstract class Perspectives implements BaseColumns {
        public static final String TABLE_NAME = "Perspective";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_VALUE = "valueData";                                      // Contains a bunch of data about the perspective
    }

    public static abstract class Projects implements BaseColumns {
        public static final String TABLE_NAME = "ProjectInfo";
        public static final String COLUMN_ID = "pk";
        public static final String COLUMN_AVAILABLE_COUNT = "availableCount";
        public static final String COLUMN_CONTAINED_COUNT = "containedCount";
        public static final String COLUMN_DEFER = "taskDateToStart";
        public static final String COLUMN_DUE = "minimumDueDate";                                   // Due date
        public static final String COLUMN_DUE_SOON_COUNT = "numberOfDueSoonTasks";
        public static final String COLUMN_FOLDER = "folder";
        public static final String COLUMN_FOLDER_ACTIVE_EFFECTIVE = "folderActiveEffective";
        public static final String COLUMN_LAST_REVIEW = "lastReviewDate";
        public static final String COLUMN_NEXT_REVIEW = "nextReviewDate";
        public static final String COLUMN_NEXT_TASK = "nextTask";
        public static final String COLUMN_OVERDUE_COUNT = "numberOfOverdueTasks";
        public static final String COLUMN_REMAINING_COUNT = "numberOfRemainingTasks";
        public static final String COLUMN_REPEAT_REVIEW = "reviewRepetitionString";                 // String to determine how often to repeat the review process
        public static final String COLUMN_SINGLE_ACTION = "containsSingletonActions";               // 1 = Single Action List, 0 = Other
        public static final String COLUMN_STATUS = "status";                                        // Options: active, done, inactive (On Hold), dropped
        public static final String COLUMN_TASK = "task";
        public static final String COLUMN_TASK_BLOCKED = "taskBlocked";                             // 1 = can continue, 0 = nothing to do
        public static final String COLUMN_TASK_BLOCKED_BY_DEFER = "taskBlockedByFutureStartDate";
    }

    public static abstract class Settings implements BaseColumns {
        public static final String TABLE_NAME = "Setting";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_VALUE = "valueData";
    }

    public static abstract class Tasks implements BaseColumns {
        public static final String TABLE_NAME = "Task";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_BLOCKED = "blocked";
        public static final String COLUMN_BLOCKED_BY_DEFER = "blockedByFutureStartDate";
        public static final String COLUMN_CHILDREN_AVAILABLE_COUNT = "childrenCountAvailable";
        public static final String COLUMN_CHILDREN_COMPLETED_COUNT = "childrenCountCompleted";
        public static final String COLUMN_CHILDREN_COUNT = "childrenCount";
        public static final String COLUMN_CHILDREN_STATE = "childrenState";
        public static final String COLUMN_COMPLETE_WITH_CHILDREN = "completeWhenChildrenComplete";
        public static final String COLUMN_CONTAINS_NEXT_TASK = "containsNextTask";
        public static final String COLUMN_CONTEXT = "context";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_COMPLETED = "dateCompleted";
        public static final String COLUMN_DATE_DUE = "dateDue";
        public static final String COLUMN_DATE_DUE_EFFECTIVE = "effectiveDateDue";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_DEFER = "dateToStart";
        public static final String COLUMN_DATE_START_EFFECTIVE = "effectiveDateToStart";
        public static final String COLUMN_DUE_SOON = "isDueSoon";
        public static final String COLUMN_ESTIMATED_TIME = "estimatedInMinutes";
        public static final String COLUMN_FLAGGED = "flagged";
        public static final String COLUMN_FLAGGED_EFFECTIVE = "effectiveFlagged";
        public static final String COLUMN_HAS_COMPLETED_DESCENDANT = "hasCompletedDescendant";
        public static final String COLUMN_HAS_FLAGGED_IN_TREE = "hasFlaggedTaskInTree";
        public static final String COLUMN_HAS_UNESTIMATED_IN_TREE = "hasUnestimatedLeafTaskInTree";
        public static final String COLUMN_INBOX = "inInbox";
        public static final String COLUMN_INBOX_EFFECTIVE = "effectiveInInbox";
        public static final String COLUMN_MAXIMUM_ESTIMATE_IN_TREE = "maximumEstimateInTree";
        public static final String COLUMN_MINIMUM_ESTIMATE_IN_TREE = "minimumEstimateInTree";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NOTE_PLAINTEXT = "plainTextNote";
        public static final String COLUMN_NOTE_XML = "noteXMLData";
        public static final String COLUMN_OVERDUE = "isOverdue";
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_PROJECT = "projectInfo";
        public static final String COLUMN_PROJECT_ACTIVE_EFFECTIVE = "effectiveContainingProjectInfoActive";
        public static final String COLUMN_PROJECT_SINGLE_ACTION = "containingProjectContainsSingletons";
        public static final String COLUMN_CONTAINING_PROJECT = "containingProjectInfo";
        public static final String COLUMN_PROJECT_NEXT_TASK = "nextTaskOfProjectInfo";
        public static final String COLUMN_PROJECT_REMAINING_EFFECTIVE = "effectiveContainingProjectInfoRemaining";
        public static final String COLUMN_RANK = "rank";
        public static final String COLUMN_REPETITION_METHOD = "repetitionMethodString";
        public static final String COLUMN_REPETITION_RULE = "repetitionRuleString";
        public static final String COLUMN_SEQUENTIAL = "sequential";
    }
}
