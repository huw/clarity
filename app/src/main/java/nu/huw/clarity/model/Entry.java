package nu.huw.clarity.model;

import android.support.annotation.NonNull;

/**
 * Everything in the OmniFocus tree (folders, contexts, projects, tasks) will implement these
 * methods. They're common to everything.
 */
public class Entry extends Base implements Comparable<Entry> {

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
    public boolean headerRow;

    public Entry() {}

    @Override public int compareTo(@NonNull Entry entry) {

        if (headerRow || rank < entry.rank) {
            return -1;
        } else if (rank > entry.rank) {
            return 1;
        }

        return 0;
    }
}
