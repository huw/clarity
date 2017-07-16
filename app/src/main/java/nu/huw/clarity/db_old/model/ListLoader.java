package nu.huw.clarity.db_old.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import java.util.TreeMap;
import java.util.TreeSet;
import nu.huw.clarity.model_old.Entry;
import nu.huw.clarity.model_old.Header;
import nu.huw.clarity.model_old.Perspective;

public class ListLoader extends AsyncTaskLoader<TreeMap<Header, TreeSet<? extends Entry>>> {

  private static final String TAG = ListLoader.class.getSimpleName();
  private Perspective perspective;
  private Entry parent;

  public ListLoader(Context androidContext, Perspective perspective, Entry parent) {

    super(androidContext);
    this.perspective = perspective;
    this.parent = parent;
  }

  @Override
  public TreeMap<Header, TreeSet<? extends Entry>> loadInBackground() {

    DataModelHelper dmHelper = new DataModelHelper(getContext());

    return dmHelper.getEntriesFromPerspective(perspective, parent);
  }

  /**
   * Fixes a bug with AsyncTaskLoader where it doesn't start
   */
  @Override
  protected void onStartLoading() {

    forceLoad();
  }
}
