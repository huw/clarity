package nu.huw.clarity.model_old;

import android.os.Parcel;
import android.support.annotation.Nullable;
import java.util.Random;
import nu.huw.clarity.db_old.model.DataModelHelper;
import org.threeten.bp.LocalDateTime;

public class Header extends Entry {

  public String name;

  public String contextID;
  public String folderID;
  public String projectID;

  public int classType;
  public Context context;
  public LocalDateTime dateAdded;
  public LocalDateTime dateDue;
  public LocalDateTime dateDefer;
  public LocalDateTime dateCompleted;
  public LocalDateTime dateModified;
  public boolean flagged;
  public Folder folder;
  public Task project;

  public Header(String name) {
    this.name = name;
    this.rank = new Random().nextLong();
  }

  protected Header(Parcel in) {
    name = in.readString();
    contextID = in.readString();
    folderID = in.readString();
    projectID = in.readString();
    classType = in.readInt();
    context = in.readParcelable(Context.class.getClassLoader());
    dateAdded = getDateTime(in.readString());
    dateDue = getDateTime(in.readString());
    dateDefer = getDateTime(in.readString());
    dateCompleted = getDateTime(in.readString());
    dateModified = getDateTime(in.readString());
    flagged = in.readInt() != 0;
    folder = in.readParcelable(Folder.class.getClassLoader());
    project = in.readParcelable(Task.class.getClassLoader());
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(name);
    out.writeString(contextID);
    out.writeString(folderID);
    out.writeString(projectID);
    out.writeInt(classType);
    out.writeParcelable(context, 0);
    out.writeString(getTimeString(dateAdded));
    out.writeString(getTimeString(dateDue));
    out.writeString(getTimeString(dateDefer));
    out.writeString(getTimeString(dateCompleted));
    out.writeString(getTimeString(dateModified));
    out.writeInt(flagged ? 1 : 0);
    out.writeParcelable(folder, 0);
    out.writeParcelable(project, 0);
  }

  /**
   * Return the view type integer for use with the ListAdapter
   */
  public int getViewType(@Nullable Perspective perspective) {
    return Entry.VT_HEADER_COLLATION;
  }

  Context getContext(android.content.Context androidContext) {

    if (context == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      context = dataModelHelper.getContextFromID(contextID);
    }
    return context;
  }

  Folder getFolder(android.content.Context androidContext) {

    if (folder == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      folder = dataModelHelper.getFolderFromID(folderID);
    }
    return folder;
  }

  Task getProject(android.content.Context androidContext) {

    if (project == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      project = dataModelHelper.getTaskFromID(projectID);
    }
    return project;
  }
}
