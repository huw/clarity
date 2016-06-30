package nu.huw.clarity.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

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
    public boolean blockedByDefer;
    public boolean completeWithChildren;
    public String  context;
    public String  contextName;
    public Date    dateCompleted;
    public Date    dateDefer;
    public Date    dateDeferEffective;
    public Date    dateDue;
    public Date    dateDueEffective;
    public boolean dueSoon;
    public int     estimatedTime;
    public boolean flagged;
    public boolean flaggedEffective;
    public boolean inInbox;
    public String  next;
    public String  notePlaintext;
    public String  noteXML;
    public boolean overdue;
    public boolean project;
    public String  projectID;
    public String  projectName;
    public Date    lastReview;
    public Date    nextReview;
    public Date    reviewInterval;
    public String  status;
    public String  repetitionMethod;
    public String  repetitionRule;
    public String  type;

    public Task() {}

    protected Task(Parcel in) {

        super(in);
        blocked = in.readInt() != 0;
        blockedByDefer = in.readInt() != 0;
        completeWithChildren = in.readInt() != 0;
        context = in.readString();
        contextName = in.readString();
        dateCompleted = getDateOrNull(in.readLong());
        dateDefer = getDateOrNull(in.readLong());
        dateDeferEffective = getDateOrNull(in.readLong());
        dateDue = getDateOrNull(in.readLong());
        dateDueEffective = getDateOrNull(in.readLong());
        dueSoon = in.readInt() != 0;
        estimatedTime = in.readInt();
        flagged = in.readInt() != 0;
        flaggedEffective = in.readInt() != 0;
        inInbox = in.readInt() != 0;
        next = in.readString();
        notePlaintext = in.readString();
        noteXML = in.readString();
        overdue = in.readInt() != 0;
        project = in.readInt() != 0;
        projectID = in.readString();
        projectName = in.readString();
        lastReview = getDateOrNull(in.readLong());
        nextReview = getDateOrNull(in.readLong());
        reviewInterval = getDateOrNull(in.readLong());
        status = in.readString();
        repetitionMethod = in.readString();
        repetitionRule = in.readString();
        type = in.readString();
    }

    public boolean isAvailable() {

        return !blocked && !blockedByDefer && dateCompleted == null;
    }

    @Override public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);
        out.writeInt(blocked ? 1 : 0);
        out.writeInt(blockedByDefer ? 1 : 0);
        out.writeInt(completeWithChildren ? 1 : 0);
        out.writeString(context);
        out.writeString(contextName);
        out.writeLong(getTimeOrNull(dateCompleted));
        out.writeLong(getTimeOrNull(dateDefer));
        out.writeLong(getTimeOrNull(dateDeferEffective));
        out.writeLong(getTimeOrNull(dateDue));
        out.writeLong(getTimeOrNull(dateDueEffective));
        out.writeInt(dueSoon ? 1 : 0);
        out.writeInt(estimatedTime);
        out.writeInt(flagged ? 1 : 0);
        out.writeInt(flaggedEffective ? 1 : 0);
        out.writeInt(inInbox ? 1 : 0);
        out.writeString(next);
        out.writeString(notePlaintext);
        out.writeString(noteXML);
        out.writeInt(overdue ? 1 : 0);
        out.writeInt(project ? 1 : 0);
        out.writeString(projectID);
        out.writeString(projectName);
        out.writeLong(getTimeOrNull(lastReview));
        out.writeLong(getTimeOrNull(nextReview));
        out.writeLong(getTimeOrNull(reviewInterval));
        out.writeString(status);
        out.writeString(repetitionMethod);
        out.writeString(repetitionRule);
        out.writeString(type);
    }
}
