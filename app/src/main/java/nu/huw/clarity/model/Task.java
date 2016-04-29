package nu.huw.clarity.model;

import java.util.Date;

/**
 * A 'Task' is any item in the OmniFocus tree that isn't a folder. Projects are also tasks.
 */
public class Task extends Entry {

    public boolean blocked;
    public boolean blockedByDefer;
    public boolean completeWithChildren;
    public String  context;
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
    public boolean projectID;
    public Date    lastReview;
    public Date    nextReview;
    public Date    reviewInterval;
    public String  status;
    public String  repetitionMethod;
    public String  repetitionRule;
    public String  type;

    public Task() {}
}
