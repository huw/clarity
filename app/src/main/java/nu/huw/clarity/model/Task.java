package nu.huw.clarity.model;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import nu.huw.clarity.R;
import nu.huw.clarity.db.model.DataModelHelper;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

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
    dateCompleted = getDateTime(in.readString());
    dateDefer = getDateTime(in.readString());
    dateDeferEffective = getDateTime(in.readString());
    dateDue = getDateTime(in.readString());
    dateDueEffective = getDateTime(in.readString());
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
    lastReview = getDateTime(in.readString());
    nextReview = getDateTime(in.readString());
    reviewInterval = getDateTime(in.readString());
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
   * If the perspective's viewMode is 'project', then we should display things in the project tree.
   * But otherwise, it only cares about displaying everything as a task.
   */
  @Override
  public int getViewType(@Nullable Perspective perspective) {
    if (perspective == null || (perspective.viewMode != null && perspective.viewMode
        .equals("project"))) {
      if (isProject) {
        return VT_PROJECT;
      } else if (isParent()) {
        return VT_NESTED_TASK;
      }
    }
    return VT_TASK;
  }

  /**
   * The name field is greyed out if the task isn't available, due soon, or overdue or a header row
   */
  @ColorInt
  public int getPrimaryTextColor(@NonNull android.content.Context androidContext) {
    @ColorRes int colorID =
        isAvailable() || dueSoon || overdue ? R.color.primary_text_light
            : R.color.disabled_text_light;
    return ContextCompat.getColor(androidContext, colorID);
  }

  /**
   * Get the string to show in the viewMode text field on the task (below the name)
   */
  @Nullable
  public String getViewModeString(@NonNull android.content.Context androidContext,
      @Nullable Perspective perspective) {

    // If there's no viewMode on the perspective, return the context name (or null)

    if (perspective == null || perspective.viewMode == null) {
      Context context = getContext(androidContext);
      return context != null ? context.name : null;
    }

    // Otherwise...

    switch (perspective.viewMode) {

      // If it's a project, and we have a project, return its name. Otherwise null.

      case "context":
        Task project = getProject(androidContext);
        if (project != null) {
          return project.name;
        }
        break;

      // Same thing with contexts, or any other thing we don't recognise

      case "project":
      default:
        Context context = getContext(androidContext);
        if (context != null) {
          return context.name;
        }
        break;
    }

    return null;
  }

  /**
   * The 'viewMode' field is greyed out if the task isn't available, unless it's due soon/overdue or
   * a header row
   */
  @ColorInt
  public int getSecondaryTextColor(@NonNull android.content.Context androidContext) {
    @ColorRes int colorID =
        isAvailable() || dueSoon || overdue ? R.color.secondary_text_light
            : R.color.disabled_text_light;
    return ContextCompat.getColor(androidContext, colorID);
  }

  /**
   * Get the string to show in the due text field on the task (below the name, to the right)
   */
  public String getDueString(@NonNull android.content.Context androidContext) {

    if (dateDueEffective != null) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
      String string = dateDueEffective.format(dateTimeFormatter);
      return androidContext.getString(R.string.listitem_sortdue, string);
    }

    return null;
  }

  /**
   * When displaying items in a list, sometimes we have to set a different text style on their due
   * strings.
   *
   * @return int Anything like Typeface.BOLD (e.g. Typeface.BOLD_ITALIC, Typeface.NORMAL, etc.)
   */
  public int getDueTextStyle() {
    return isNotDueLocally() ? Typeface.ITALIC : Typeface.NORMAL;
  }

  /**
   * The 'sort' field can have a variety of colours. If the task isn't available, it greys out—but
   * if it is available and due soon/overdue, then it needs to change colour again.
   *
   * There's an annoying quirk here, where if an item is overdue and incomplete, then it should
   * override this rule and show its due soon/overdue colour. And if it's a header row, then it
   * should never grey out
   */
  @ColorInt
  public int getDueColor(@NonNull android.content.Context androidContext) {

    @ColorRes int colorID = R.color.secondary_text_light;

    if (dueSoon) {
      colorID = R.color.foreground_due_soon;
    } else if (overdue) {
      colorID = R.color.foreground_overdue;
    } else if (!isAvailable()) {
      colorID = R.color.disabled_text_light;
    }

    return ContextCompat.getColor(androidContext, colorID);
  }

  /**
   * Sometimes the due date will have a background to show if it's due soon or overdue at a glance.
   * This only appears if it's available, like with {@link #getDueColor(android.content.Context)
   * getDueColor()}
   */
  @DrawableRes
  public int getDueBackgroundDrawable() {

    if (dueSoon) {
      return R.drawable.background_due_soon;
    } else if (overdue) {
      return R.drawable.background_overdue;
    }

    return 0;
  }

  /**
   * Get the current task's enclosing context (if it exists) into the data model
   *
   * @param androidContext The current Android Context
   * @return The task's Context object
   */
  @Nullable
  public Context getContext(@NonNull android.content.Context androidContext) {

    if (context == null && contextID != null) {
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

    if (project == null && projectID != null) {
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
   * @return Whether the task has had its due date set because of a parent
   */
  public boolean isNotDueLocally() {
    return dateDue == null && dateDueEffective != null;
  }

  /**
   * @return Whether the task has had its defer date set because of a parent
   */
  public boolean isNotDeferredLocally() {
    return dateDefer == null && dateDeferEffective != null;
  }

  /**
   * @return Whether the task has had its flag set because of a parent
   */
  public boolean isNotFlaggedLocally() {
    return !flagged && flaggedEffective;
  }

  public boolean isParent() {
    return countChildren > 0;
  }
}
