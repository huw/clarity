package nu.huw.clarity.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Perspective;

class PerspectiveHelper {

    private DatabaseHelper          dbHelper;
    private android.content.Context androidContext;
    private Random                  random;

    PerspectiveHelper(DatabaseHelper dbHelper, android.content.Context context) {

        this.dbHelper = dbHelper;
        this.androidContext = context;
        this.random = new Random();
    }

    /**
     * Gets all perspectives matching a given SQL selection
     *
     * @param selection     SQL selection matching a list of perspectives
     * @param selectionArgs Arguments for the selection
     */
    List<Perspective> getPerspectivesFromSelection(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.query(db, Perspectives.TABLE_NAME, Perspectives.columns, selection,
                                       selectionArgs);

        List<Perspective> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getPerspectiveFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Gets a Forecast perspective object
     */
    public Perspective getForecast() {

        Perspective perspective = new Perspective();

        perspective.id = "ProcessForecast";
        perspective.filterDuration = "any";
        perspective.filterFlagged = "any";
        perspective.filterStatus = "due";

        // Don't set `group` or `sort` because Forecast uses a custom grouping/sorting

        perspective.name = androidContext.getString(R.string.forecast);
        perspective.menuID = R.id.nav_forecast;
        perspective.themeID = R.style.AppTheme_Red;
        perspective.color = R.color.primary_red;
        perspective.colorStateListID = R.color.state_list_red;
        perspective.icon = R.drawable.ic_forecast_red;

        return perspective;
    }

    /**
     * Gets a perspective object suitable for a placeholder
     */
    public Perspective getPlaceholder() {

        Perspective perspective = getForecast();
        perspective.color = R.color.primary;
        perspective.colorStateListID = R.color.state_list_purple;
        perspective.themeID = R.style.AppTheme;
        perspective.icon = R.drawable.ic_forecast_red;
        return perspective;
    }

    /**
     * Fetch a Perspective object from a given cursor
     *
     * @param cursor A database cursor containing a perspective object at the current pointer
     */
    private Perspective getPerspectiveFromCursor(Cursor cursor) {

        Perspective perspective = new Perspective();

        // Base methods
        perspective.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        perspective.dateAdded = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        perspective.dateModified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Perspectives methods
        perspective.filterDuration =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_DURATION.name);
        perspective.filterFlagged =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_FLAGGED.name);
        perspective.filterStatus =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_STATUS.name);
        perspective.group = dbHelper.getString(cursor, Perspectives.COLUMN_GROUP.name);
        perspective.name = dbHelper.getString(cursor, Perspectives.COLUMN_NAME.name);
        perspective.sort = dbHelper.getString(cursor, Perspectives.COLUMN_SORT.name);
        perspective.value = dbHelper.getString(cursor, Perspectives.COLUMN_VALUE.name);
        perspective.viewMode = dbHelper.getString(cursor, Perspectives.COLUMN_VIEW_MODE.name);

        String iconName = dbHelper.getString(cursor, Perspectives.COLUMN_ICON.name);

        switch (iconName) {
            case "ProcessFlagged":
                perspective.icon = R.drawable.ic_flag_orange;
                break;
            case "ProcessContexts":
                perspective.icon = R.drawable.ic_contexts_purple;
                break;
            case "ProcessProjects":
                perspective.icon = R.drawable.ic_projects_blue;
                break;
            case "ProcessInbox":
            default:
                perspective.icon = R.drawable.ic_inbox_bluegrey;
                break;
        }

        switch (perspective.id) {
            case "ProcessFlagged":
                perspective.color = R.color.primary_orange;
                perspective.colorStateListID = R.color.state_list_orange;
                perspective.menuID = R.id.nav_flagged;
                perspective.themeID = R.style.AppTheme_Orange;
                break;
            case "ProcessContexts":
                perspective.color = R.color.primary;
                perspective.colorStateListID = R.color.state_list_purple;
                perspective.menuID = R.id.nav_contexts;
                perspective.themeID = R.style.AppTheme;
                break;
            case "ProcessProjects":
                perspective.color = R.color.primary_blue;
                perspective.colorStateListID = R.color.state_list_blue;
                perspective.menuID = R.id.nav_projects;
                perspective.themeID = R.style.AppTheme_Blue;
                break;
            case "ProcessInbox":
                perspective.color = R.color.primary_blue_grey;
                perspective.colorStateListID = R.color.state_list_blue_grey;
                perspective.menuID = R.id.nav_inbox;
                perspective.themeID = R.style.AppTheme_BlueGrey;
                break;
            default:
                perspective.color = R.color.primary_blue_grey;
                perspective.colorStateListID = R.color.state_list_blue_grey;
                perspective.themeID = R.style.AppTheme_BlueGrey;
                // Generate a random number for the other menu IDs, fine as long as it's saved
                perspective.menuID = random.nextInt();
                break;
        }

        return perspective;
    }
}
