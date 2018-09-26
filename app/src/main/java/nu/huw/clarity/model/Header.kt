package nu.huw.clarity.model

import org.threeten.bp.LocalDateTime

class Header(name: String, var entryType: EntryTypeState = EntryTypeState.TASK, var contextID: ID? = null, var folderID: ID? = null, var projectID: ID? = null, var dateAdded: LocalDateTime? = null, var dateDue: LocalDateTime? = null, var dateDefer: LocalDateTime? = null, var dateCompleted: LocalDateTime? = null, var dateModified: LocalDateTime? = null, var flagged: FlaggedState = FlaggedState.FALSE)
