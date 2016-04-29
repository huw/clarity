package nu.huw.clarity.model;

/**
 * Everything in the OmniFocus tree (folders, contexts, projects, tasks) will implement these
 * methods. They're common to everything.
 */
public class Entry extends Base {

    public boolean active;
    public boolean activeEffective;
    public int     countAvailable;
    public int     countChildren;
    public int     countCompleted;
    public int     countDueSoon;
    public int     countOverdue;
    public int     countRemaining;
    public boolean hasChildren;
    public String  name;
    public String  parentID;
    public long    rank;

    public Entry() {}
}
