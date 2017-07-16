package nu.huw.clarity.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import nu.huw.clarity.R;
import nu.huw.clarity.model_old.Context;
import nu.huw.clarity.model_old.Entry;
import nu.huw.clarity.model_old.Folder;
import nu.huw.clarity.model_old.Perspective;
import nu.huw.clarity.model_old.Task;
import nu.huw.clarity.ui.fragment.DetailAttachmentFragment;
import nu.huw.clarity.ui.fragment.DetailInfoContextFragment;
import nu.huw.clarity.ui.fragment.DetailInfoFolderFragment;
import nu.huw.clarity.ui.fragment.DetailInfoProjectFragment;
import nu.huw.clarity.ui.fragment.DetailInfoTaskFragment;
import nu.huw.clarity.ui.fragment.DetailNoteFragment;

public class DetailPagerAdapter extends FragmentPagerAdapter {

  private static final int POSITION_INFO = 0;
  private static final int POSITION_NOTE = 1;
  private static final int POSITION_ATTACHMENT = 2;
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
      case POSITION_ATTACHMENT:
        return DetailAttachmentFragment.newInstance((Task) entry, perspective);
      case POSITION_NOTE:
        return DetailNoteFragment.newInstance((Task) entry, perspective);
      case POSITION_INFO:
      default:

        if (entry != null) {
          if (entry instanceof Task) {
            if (((Task) entry).isProject) {
              return DetailInfoProjectFragment.newInstance(entry, perspective);
            } else {
              return DetailInfoTaskFragment.newInstance(entry, perspective);
            }
          } else if (entry instanceof Context) {
            return DetailInfoContextFragment.newInstance(entry, perspective);
          } else if (entry instanceof Folder) {
            return DetailInfoFolderFragment.newInstance(entry, perspective);
          }
        }

        throw new NullPointerException("Supplied entry is null or invalid");
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
      case POSITION_ATTACHMENT:
        return androidContext.getString(R.string.detail_tabattachments);
      case POSITION_NOTE:
        return androidContext.getString(R.string.detail_tabnote);
      case POSITION_INFO:
      default:
        return androidContext.getString(R.string.detail_tabinfo);
    }
  }
}
