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
        public static final String COLUMN_CONTAINING_TRANSACTION = "containingTransactionHint";
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
        public static final String COLUMN_ACTIVE = "active";
        public static final String COLUMN_ACTIVE_EFFECTIVE = "effectiveActive";
        public static final String COLUMN_ALLOWS_NEXT_ACTION = "allowsNextAction";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String COLUMN_AVAILABLE_COUNT = "availableTaskCount";
        public static final String COLUMN_CHILDREN_COUNT = "childrenCount";
        public static final String COLUMN_CHILDREN_STATE = "childrenState";
        public static final String COLUMN_CONTAINED_COUNT = "containedTaskCount";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_LATITTUDE = "latitude";
        public static final String COLUMN_LOCAL_DUE_SOON_COUNT = "localNumberOfDueSoonTasks";
        public static final String COLUMN_LOCAL_OVERDUE_COUNT = "localNumberOfOverdueTasks";
        public static final String COLUMN_LOCATION_NAME = "locationName";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NEXT_COUNT = "nextTaskCount";
        public static final String COLUMN_NOTE_PLAINTEXT = "plainTextNote";
        public static final String COLUMN_NOTE_XML = "noteXMLData";
        public static final String COLUMN_NOTIFICATION_FLAGS = "notificationFlags";
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_RANK = "rank";
        public static final String COLUMN_REMAINING_COUNT = "remainingTaskCount";
        public static final String COLUMN_TOTAL_DUE_SOON_COUNT = "totalNumberOfDueSoonTasks";
        public static final String COLUMN_TOTAL_OVERDUE_COUNT = "totalNumberOfOverdueTasks";
    }

    public static abstract class Folders implements BaseColumns {
        public static final String TABLE_NAME = "Folder";
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
        public static final String COLUMN_ID = "persistentIdentifier";
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
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_VALUE = "valueData";
    }

    public static abstract class Projects implements BaseColumns {
        public static final String TABLE_NAME = "ProjectInfo";
        public static final String COLUMN_AVAILABLE_COUNT = "numberOfAvailableTasks";
        public static final String COLUMN_CONTAINED_COUNT = "numberOfContainedTasks";
        public static final String COLUMN_CONTAINS_SINGLETONS = "containsSingletonActions";
        public static final String COLUMN_DUE_SOON_COUNT = "numberOfDueSoonTasks";
        public static final String COLUMN_FOLDER = "folder";
        public static final String COLUMN_FOLDER_ACTIVE_EFFECTIVE = "folderEffectiveActive";
        public static final String COLUMN_ID = "pk";
        public static final String COLUMN_LAST_REVIEW = "lastReviewDate";
        public static final String COLUMN_MINIMUM_DUE = "minimumDueDate";
        public static final String COLUMN_NEXT_REVIEW = "nextReviewDate";
        public static final String COLUMN_NEXT_TASK = "nextTask";
        public static final String COLUMN_OVERDUE_COUNT = "numberOfOverdueTasks";
        public static final String COLUMN_REMAINING_TASKS = "numberOfRemainingTasks";
        public static final String COLUMN_REPEAT_REVIEW = "reviewRepetitionString";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TASK = "task";
        public static final String COLUMN_TASK_BLOCKED = "taskBlocked";
        public static final String COLUMN_TASK_BLOCKED_BY_FUTURE_START = "taskBlockedByFutureStartDate";
        public static final String COLUMN_TASK_START = "taskDateToStart";
    }

    public static abstract class Settings implements BaseColumns {
        public static final String TABLE_NAME = "Setting";
        public static final String COLUMN_CREATION_ORDINAL = "creationOrdinal";
        public static final String COLUMN_DATE_ADDED = "dateAdded";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_ID = "persistentIdentifier";
        public static final String COLUMN_VALUE = "valueData";
    }

    public static abstract class Tasks implements BaseColumns {
        public static final String TABLE_NAME = "Task";
        public static final String COLUMN_BLOCKED = "blocked";
        public static final String COLUMN_BLOCKED_BY_FUTURE_START = "blockedByFutureStartDate";
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
        public static final String COLUMN_DATE_START = "dateToStart";
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
        public static final String COLUMN_PROJECT_CONTAINS_SINGLETONS = "containingProjectContainsSingletons";
        public static final String COLUMN_PROJECT_INFO = "containingProjectInfo";
        public static final String COLUMN_PROJECT_NEXT_TASK = "nextTaskOfProjectInfo";
        public static final String COLUMN_PROJECT_REMAINING = "effectiveContainingProjectInfoRemaining";
        public static final String COLUMN_RANK = "rank";
        public static final String COLUMN_REPETITION_METHOD = "repetitionMethodString";
        public static final String COLUMN_REPETITION_RULE = "repetitionRuleString";
        public static final String COLUMN_SEQUENTIAL = "sequential";
    }
}
