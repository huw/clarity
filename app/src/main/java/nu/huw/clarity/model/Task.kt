package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import nu.huw.clarity.db.NoteHelper
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime

@Entity
@TypeConverters(Converters::class)
class Task(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), countAvailable: Long = 0, countChildren: Long = 0, countCompleted: Long = 0, countDueSoon: Long = 0, countOverdue: Long = 0, countRemaining: Long = 0, name: String = "", parentID: ID? = null, rank: Long = 0, status: StatusState = StatusState.ACTIVE,
           var blockedState: BlockedState = BlockedState.FALSE, var completeWithChildren: Boolean = false, var contextID: ID? = null, var dateCompleted: LocalDateTime? = null, var dateDefer: LocalDateTime? = null, var dateDeferEffective: LocalDateTime? = null, var dateDue: LocalDateTime? = null, var dateDueEffective: LocalDateTime? = null, var dueSoon: Boolean = false, var droppedState: DroppedState = DroppedState.FALSE, var duration: Duration? = null, var flaggedState: FlaggedState = FlaggedState.FALSE, var inInbox: Boolean = true, var isProject: Boolean = false, var nextID: ID? = null, var noteXML: String? = null, var overdue: Boolean = false, var projectID: ID? = null, var lastReview: LocalDateTime? = null, var nextReview: LocalDateTime? = null, var reviewInterval: Duration? = null, var repetitionMethod: String? = null, var repetitionRule: String? = null, var completionRule: CompletionRuleState = CompletionRuleState.PARALLEL) : Entry(id, dateAdded, dateModified, countAvailable, countChildren, countCompleted, countDueSoon, countOverdue, countRemaining, name, parentID, rank, status) {
    // TODO: Override fields like dueSoon in the class?
    @Ignore constructor() : this(ID())

    @Ignore val note = if (noteXML != null) NoteHelper.noteXMLtoString(noteXML as String) else null

    // Helper properties (mostly so code looks more English-like)
    @Ignore val blocked = blockedState != BlockedState.FALSE
    @Ignore val dropped = droppedState != DroppedState.FALSE
    @Ignore val flagged = flaggedState != FlaggedState.FALSE
    @Ignore val completed = dateCompleted != null || dropped
    @Ignore val remaining = !completed
    @Ignore val available = remaining && !blocked
    @Ignore val hasChildren = countChildren > 0
}