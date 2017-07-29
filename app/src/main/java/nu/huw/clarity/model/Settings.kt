package nu.huw.clarity.model

import com.chibatching.kotpref.KotprefModel
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime

object Settings : KotprefModel() {
    private var defaultDueTimeString by stringPref()
    private var defaultDeferTimeString by stringPref()
    private var dueSoonDurationString by stringPref()
    val perspectiveOrder by stringSetPref { return@stringSetPref HashSet() }

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