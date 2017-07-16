package nu.huw.clarity.model

enum class DroppedState {
    TRUE, FALSE, EFFECTIVE
}

enum class FlaggedState {
    TRUE, FALSE, EFFECTIVE
}

enum class BlockedState {
    TRUE, FALSE, DEFERRED
}

enum class StatusState {
    ACTIVE, COMPLETED, DROPPED, ON_HOLD
}

enum class TypeState {
    SEQUENTIAL, PARALLEL, SINGLE_ACTION
}

enum class StatusFilterState {
    FIRST_AVAILABLE, AVAILABLE, REMAINING, COMPLETED, NONE
}

enum class FlaggedFilterState {
    FLAGGED, UNFLAGGED, DUE, DUE_OR_FLAGGED, DUE_AND_FLAGGED, DUE_AND_UNFLAGGED, NONE
}

enum class DurationFilterState {
    FIVE, FIFTEEN, THIRTY, HOUR, LONG, UNESTIMATED, NONE
}

enum class CollationState {
    CONTEXT, PROJECT, DUE, DEFER, COMPLETED, ADDED, MODIFIED, FLAGGED, NONE
}

enum class ViewModeState {
    CONTEXT, PROJECT
}

enum class SortState {
    CONTEXT, PROJECT, DUE, DEFER, COMPLETED, ADDED, MODIFIED, FLAGGED, DURATION, NONE
}