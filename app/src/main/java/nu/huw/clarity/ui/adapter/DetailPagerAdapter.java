package nu.huw.clarity.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import nu.huw.clarity.R;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import nu.huw.clarity.ui.fragment.DetailFragment;

public class DetailPagerAdapter extends FragmentPagerAdapter {

  android.content.Context androidContext;
  Perspective perspective;
  Entry entry;

  public DetailPagerAdapter(FragmentManager fragmentManager, android.content.Context androidContext,
      Perspective perspective, Entry entry) {
    super(fragmentManager);
    this.androidContext = androidContext;
    this.perspective = perspective;
    this.entry = entry;
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 2:
        //
      case 1:
        // return DetailFragment.newInstance(entry, perspective);
      case 0:
      default:
        return DetailFragment.newInstance(entry, perspective);
    }
  }

  @Override
  public int getCount() {

    // If it's a task, it can have notes and attachments
    // Otherwise, just details

    if (entry instanceof Task) {
      return 3;
    } else {
      return 1;
    }
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
      case 2:
        return androidContext.getString(R.string.detail_tabattachments);
      case 1:
        return androidContext.getString(R.string.detail_tabnote);
      case 0:
      default:
        return androidContext.getString(R.string.detail_tabinfo);
    }
  }
}
