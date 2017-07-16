package nu.huw.clarity.model_old;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import nu.huw.clarity.db_old.model.DataModelHelper;

/**
 * Folder. Contains projects or other folders. Projects, by the way, cannot contain projects.
 */
public class Folder extends Entry {

  public static final Parcelable.Creator<Folder> CREATOR = new Parcelable.Creator<Folder>() {
    public Folder createFromParcel(Parcel in) {

      return new Folder(in);
    }

    public Folder[] newArray(int size) {

      return new Folder[size];
    }
  };

  public Folder() {
  }

  protected Folder(Parcel in) {

    super(in);
  }

  public
  @Override
  Entry getParent(android.content.Context androidContext) {

    if (parent == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      parent = dataModelHelper.getFolderFromID(parentID);
    }
    return parent;
  }

  @Override
  public int getViewType(@Nullable Perspective perspective) {
    return VT_FOLDER;
  }
}
