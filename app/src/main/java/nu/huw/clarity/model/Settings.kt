package nu.huw.clarity.model

import com.chibatching.kotpref.KotprefModel
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime

object Settings : KotprefModel() {
    private var defaultDueTimeString by stringPref(default = "07:00")
    private var defaultDeferTimeString by stringPref(default = "17:00")
    private var dueSoonDurationString by stringPref(default = "PT72H")
    val perspectiveOrder by stringSetPref(default = setOf("ProcessForecast",
            "ProcessInbox", "ProcessProjects", "ProcessFlagged", "ProcessNearby", "ProcessReview"))

    var defaultDueTime: LocalTime
        get() = LocalTime.parse(defaultDueTimeString)
        set(time) {
            defaultDueTimeString = time.toString()
        }

    var defaultDeferTime: LocalTime
        get() = LocalTime.parse(defaultDeferTimeString)
        set(time) {
            defaultDeferTimeString = time.toString()
        }

    var dueSoonDuration: Duration
        get() = Duration.parse(dueSoonDurationString)
        set(duration) {
            dueSoonDurationString = duration.toString()
        }
}