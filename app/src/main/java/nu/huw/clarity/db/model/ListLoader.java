package nu.huw.clarity.db.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;

public class ListLoader extends AsyncTaskLoader<List<Entry>> {

    private static final String TAG = ListLoader.class.getSimpleName();
    private Perspective perspective;
    private Entry       parent;

    public ListLoader(Context androidContext, Perspective perspective, Entry parent) {

        super(androidContext);
        this.perspective = perspective;
        this.parent = parent;
    }

    @Override public List<Entry> loadInBackground() {

        Context         context  = getContext();
        DataModelHelper dmHelper = new DataModelHelper(getContext());

        List<Entry> entries = dmHelper.getEntriesFromPerspective(perspective, parent);

        if (parent != null) {
            entries.add(0, parent);
        }

        Log.i(TAG, "Load finished (ListLoader)");

        return entries;
    }

    /**
     * Fixes a bug with AsyncTaskLoader where it doesn't start
     */
    @Override protected void onStartLoading() {

        forceLoad();
    }
}
