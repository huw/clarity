package nu.huw.clarity.model

import nu.huw.clarity.db_old.model.DataModelHelper
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime

// TODO: Move androidContext into Dagger
class Settings(androidContext: android.content.Context) {

    private val ID_DEFAULT_DUE = "DefaultDueTime"
    private val ID_DEFAULT_DEFER = "DefaultStartTime"
    private val ID_DUE_SOON_DURATION = "DueSoonInterval"

    // TODO: Move dataModelHelper into Dagger
    private val dataModelHelper: DataModelHelper = DataModelHelper(androidContext)

    /**
     * Get a LocalTime object representing the default time in your time zone that tasks are due at
     */
    // TODO: Lazy load this?
    val defaultDueTime: LocalTime
        get() {
            val value = dataModelHelper.getSettingFromID(ID_DEFAULT_DUE)
            return LocalTime.parse(value)
        }

    /**
     * Get a LocalTime object representing the default time in your time zone that tasks are deferred
     * to
     */
    val defaultDeferTime: LocalTime
        get() {
            val value = dataModelHelper.getSettingFromID(ID_DEFAULT_DEFER)
            return LocalTime.parse(value)
        }

    /**
     * Get a Duration object representing the time we consider something 'due soon' in
     */
    val dueSoonDuration: Duration
        get() {
            val value = dataModelHelper.getSettingFromID(ID_DUE_SOON_DURATION)
            return Duration.parse(value)
        }
}