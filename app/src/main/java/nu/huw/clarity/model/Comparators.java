package nu.huw.clarity.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Holds comparators for sorting collections of entries
 */
public class Comparators {

    public static Comparator<Task> getComparator(Perspective perspective) {

        if (perspective == null || perspective.sort == null) return null;

        switch (perspective.sort) {
            case "context":
                return new ContextComparator();
            case "project":
                return new ProjectComparator();
            case "due":
                return Collections.reverseOrder(new DueComparator());
            case "defer":
                return Collections.reverseOrder(new DeferComparator());
            case "completed":
                return Collections.reverseOrder(new CompletedComparator());
            case "added":
                return Collections.reverseOrder(new AddedComparator());
            case "modified":
                return Collections.reverseOrder(new ModifiedComparator());
            case "flagged":
                return new FlaggedComparator();
            case "none":
            default:
                return null;
        }
    }

    /**
     * Compares two nullable strings, such that a non-null string takes a greater precedence
     */
    public static class StringComparator implements Comparator<String> {

        @Override public int compare(String s1, String s2) {

            if (s1 == null) {
                if (s2 == null) {
                    // Both are null and equal
                    return 0;
                } else {
                    // Only s1 is null, so s2 is 'greater' than it
                    return -1;
                }
            } else {
                if (s2 == null) {
                    // Only s2 is null, so s2 is 'greater'
                    return 1;
                } else {
                    // Neither are null, compare normally
                    return s1.compareTo(s2);
                }
            }
        }
    }

    /**
     * Compares two nullable dates, such that the non-null date takes precedence
     */
    public static class DateComparator implements Comparator<Date> {

        @Override public int compare(Date d1, Date d2) {

            if (d1 == null) {
                if (d2 == null) {
                    // Both are null and equal
                    return 0;
                } else {
                    // Only s1 is null, so s2 is 'greater' than it
                    return -1;
                }
            } else {
                if (d2 == null) {
                    // Only s2 is null, so s2 is 'greater'
                    return 1;
                } else {
                    // Neither are null, compare normally
                    return d1.compareTo(d2);
                }
            }
        }
    }

    public static class ContextComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            // Sort first by context name...

            int compare = new StringComparator().compare(t1.contextName, t2.contextName);

            // ...then rank
            // Note that any two entry objects automatically compare by rank for easier sorting

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class ProjectComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            // Sort first by project name, then rank
            int compare = new StringComparator().compare(t1.projectName, t2.projectName);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class DueComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            // Sort first by due date, then rank
            int compare = new DateComparator().compare(t1.dateDue, t2.dateDue);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class DeferComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            // You get the picture
            int compare = new DateComparator().compare(t1.dateDefer, t2.dateDefer);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class CompletedComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            int compare = new DateComparator().compare(t1.dateCompleted, t2.dateCompleted);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class AddedComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            int compare = new DateComparator().compare(t1.dateAdded, t2.dateAdded);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class ModifiedComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            int compare = new DateComparator().compare(t1.dateModified, t2.dateModified);

            if (compare != 0) {
                return compare;
            } else {
                return t1.compareTo(t2);
            }
        }
    }

    public static class FlaggedComparator implements Comparator<Task> {

        @Override public int compare(Task t1, Task t2) {

            // If they have the same status, order by rank
            // If they have different statuses, order by status

            if ((t1.flagged && t2.flagged) || (!t1.flagged && !t2.flagged)) {
                return t1.compareTo(t2);
            } else if (t1.flagged) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
