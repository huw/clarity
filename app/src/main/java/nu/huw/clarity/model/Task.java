package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import nu.huw.clarity.db.model.DataModelHelper;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

/**
 * A 'Task' is any item in the OmniFocus tree that isn't a folder. Projects are also tasks.
 */
public class Task extends Entry {

  public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
    public Task createFromParcel(Parcel in) {

      return new Task(in);
    }

    public Task[] newArray(int size) {

      return new Task[size];
    }
  };
  public boolean blocked;
  public boolean completeWithChildren;
  public String contextID;
  public LocalDateTime dateCompleted;
  public LocalDateTime dateDefer;
  public LocalDateTime dateDeferEffective;
  public LocalDateTime dateDue;
  public LocalDateTime dateDueEffective;
  public boolean deferred;
  public boolean dueSoon;
  public boolean dropped;
  public Duration estimatedTime;
  public boolean flagged;
  public boolean flaggedEffective;
  public boolean inInbox;
  public boolean isProject;
  public String next;
  public String notePlaintext;
  public String noteXML;
  public boolean overdue;
  public String projectID;
  public LocalDateTime lastReview;
  public LocalDateTime nextReview;
  public LocalDateTime reviewInterval;
  public String status;
  public String repetitionMethod;
  public String repetitionRule;
  public String type;
  private Context context;
  private Task project;

  public Task() {
  }

  protected Task(Parcel in) {

    super(in);
    blocked = in.readInt() != 0;
    completeWithChildren = in.readInt() != 0;
    context = in.readParcelable(Context.class.getClassLoader());
    contextID = in.readString();
    dateCompleted = getDate(in.readString());
    dateDefer = getDate(in.readString());
    dateDeferEffective = getDate(in.readString());
    dateDue = getDate(in.readString());
    dateDueEffective = getDate(in.readString());
    deferred = in.readInt() != 0;
    dueSoon = in.readInt() != 0;
    dropped = in.readInt() != 0;
    estimatedTime = getDuration(in.readString());
    flagged = in.readInt() != 0;
    flaggedEffective = in.readInt() != 0;
    inInbox = in.readInt() != 0;
    isProject = in.readInt() != 0;
    next = in.readString();
    notePlaintext = in.readString();
    noteXML = in.readString();
    overdue = in.readInt() != 0;
    project = in.readParcelable(Task.class.getClassLoader());
    projectID = in.readString();
    lastReview = getDate(in.readString());
    nextReview = getDate(in.readString());
    reviewInterval = getDate(in.readString());
    status = in.readString();
    repetitionMethod = in.readString();
    repetitionRule = in.readString();
    type = in.readString();
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {

    super.writeToParcel(out, flags);
    out.writeInt(blocked ? 1 : 0);
    out.writeInt(completeWithChildren ? 1 : 0);
    out.writeParcelable(context, 0);
    out.writeString(contextID);
    out.writeString(getTimeString(dateCompleted));
    out.writeString(getTimeString(dateDefer));
    out.writeString(getTimeString(dateDeferEffective));
    out.writeString(getTimeString(dateDue));
    out.writeString(getTimeString(dateDueEffective));
    out.writeInt(deferred ? 1 : 0);
    out.writeInt(dueSoon ? 1 : 0);
    out.writeInt(dropped ? 1 : 0);
    out.writeString(getTimeString(estimatedTime));
    out.writeInt(flagged ? 1 : 0);
    out.writeInt(flaggedEffective ? 1 : 0);
    out.writeInt(inInbox ? 1 : 0);
    out.writeInt(isProject ? 1 : 0);
    out.writeString(next);
    out.writeString(notePlaintext);
    out.writeString(noteXML);
    out.writeInt(overdue ? 1 : 0);
    out.writeParcelable(project, 0);
    out.writeString(projectID);
    out.writeString(getTimeString(lastReview));
    out.writeString(getTimeString(nextReview));
    out.writeString(getTimeString(reviewInterval));
    out.writeString(status);
    out.writeString(repetitionMethod);
    out.writeString(repetitionRule);
    out.writeString(type);
  }

  /**
   * Get the current task's enclosing parent (if it exists) into the data model
   *
   * @param androidContext The current Context
   * @return The task's parent object. May be a Folder or a Task.
   */
  public
  @Nullable
  @Override
  Entry getParent(@NonNull android.content.Context androidContext) {

    if (parent == null) {

      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      if (isProject) {

        // Parent is a folder
        parent = dataModelHelper.getFolderFromID(parentID);
      } else {

        // Parent is a task or project
        parent = dataModelHelper.getTaskFromID(parentID);
      }
    }

    return parent;
  }

  /**
   * Get the current task's enclosing context (if it exists) into the data model
   *
   * @param androidContext The current Android Context
   * @return The task's Context object
   */
  @Nullable
  public Context getContext(@NonNull android.content.Context androidContext) {

    if (context == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      context = dataModelHelper.getContextFromID(contextID);
    }

    return context;
  }

  /**
   * Get the current task's enclosing project (if it exists) into the data model
   *
   * @param androidContext The current Android Context
   * @return The task's Project object
   */
  @Nullable
  public Task getProject(@NonNull android.content.Context androidContext) {

    if (project == null) {
      DataModelHelper dataModelHelper = new DataModelHelper(androidContext);
      project = dataModelHelper.getTaskFromID(projectID);
    }

    return project;
  }

  /**
   * A task is considered 'completed' if it either has a completed date or has been dropped
   */
  public boolean isCompleted() {
    return dateCompleted != null || dropped;
  }

  /**
   * A task is considered 'remaining' if it is not completed
   */
  public boolean isRemaining() {
    return !isCompleted();
  }

  /**
   * A task is considered 'available' if it is remaining, not blocked, and not deferred into the
   * future (i.e. available for work)
   */
  public boolean isAvailable() {
    return isRemaining() && !blocked && !deferred;
  }

  /**
   * @return Whether the task has had its due date explicitly set (as opposed to {@link
   * #isNotDueLocally()})
   */
  public boolean isDueLocally() {
    return dateDue != null && dateDueEffective != null;
  }

  /**
   * @return Whether the task has had its due date set because of a parent (as opposed to {@link
   * #isDueLocally()})
   */
  public boolean isNotDueLocally() {
    return dateDue == null && dateDueEffective != null;
  }

  public boolean isParent() {
    return countChildren > 0;
  }
}
