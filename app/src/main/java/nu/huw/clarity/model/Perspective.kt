package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.R
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime
import java.util.*

@Entity
@TypeConverters(Converters::class)
class Perspective(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), var filterDuration: DurationFilterState = DurationFilterState.NONE, var filterFlagged: FlaggedFilterState = FlaggedFilterState.NONE, var filterStatus: StatusFilterState = StatusFilterState.NONE, var collation: CollationState = CollationState.NONE, var name: String = "", var sort: SortState = SortState.NONE, var data: String? = null, var viewMode: ViewModeState = ViewModeState.CONTEXT, var iconState: PerspectiveIconState = PerspectiveIconState.INBOX, var colorState: PerspectiveColorState = PerspectiveColorState.BLUE_GREY) : Base(id, dateAdded, dateModified) {
    @Ignore constructor() : this(ID())

    @Ignore val random: Random = Random()

    @Ignore
    var icon = {
        when (iconState) {
            PerspectiveIconState.FLAGGED -> R.drawable.ic_flag_orange
            PerspectiveIconState.CONTEXTS -> R.drawable.ic_contexts_purple
            PerspectiveIconState.PROJECTS -> R.drawable.ic_projects_blue
            PerspectiveIconState.INBOX -> R.drawable.ic_inbox_bluegrey
            PerspectiveIconState.FORECAST -> R.drawable.ic_forecast_red
            PerspectiveIconState.NEARBY -> R.drawable.ic_nearby_green
        }
    }

    @Ignore
    var colorID = {
        when (colorState) {
            PerspectiveColorState.ORANGE -> R.color.primary_orange
            PerspectiveColorState.PURPLE -> R.color.primary
            PerspectiveColorState.BLUE -> R.color.primary_blue
            PerspectiveColorState.BLUE_GREY -> R.color.primary_blue_grey
            PerspectiveColorState.RED -> R.color.primary_red
            PerspectiveColorState.GREEN -> R.color.primary_green
        }
    }

    @Ignore
    var colorStateListID = {
        when (colorState) {
            PerspectiveColorState.ORANGE -> R.color.state_list_orange
            PerspectiveColorState.PURPLE -> R.color.state_list_purple
            PerspectiveColorState.BLUE -> R.color.state_list_blue
            PerspectiveColorState.BLUE_GREY -> R.color.state_list_blue_grey
            PerspectiveColorState.RED -> R.color.state_list_red
            PerspectiveColorState.GREEN -> R.color.state_list_green
        }
    }

    @Ignore
    var themeID = {
        when (colorState) {
            PerspectiveColorState.ORANGE -> R.style.AppTheme_Orange
            PerspectiveColorState.PURPLE -> R.style.AppTheme
            PerspectiveColorState.BLUE -> R.style.AppTheme_Blue
            PerspectiveColorState.BLUE_GREY -> R.style.AppTheme_BlueGrey
            PerspectiveColorState.RED -> R.style.AppTheme_Red
            PerspectiveColorState.GREEN -> R.style.AppTheme_Green
        }
    }

    @Ignore
    var menuID = {
        when (id.value) {
            "ProcessFlagged" -> R.id.menuitem_main_flagged
            "ProcessContexts" -> R.id.menuitem_main_context
            "ProcessProjects" -> R.id.menuitem_main_projects
            "ProcessInbox" -> R.id.menuitem_main_inbox
            "ProcessForecast" -> R.id.menuitem_main_forecast
            else -> random.nextInt()
        }
    }
}