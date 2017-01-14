package nu.huw.clarity.model;

import android.support.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

/**
 * Holds comparators for sorting collections of entries
 */
public class Comparators {

  private android.content.Context androidContext;

  public Comparators(android.content.Context androidContext) {

    this.androidContext = androidContext;
  }

  public Comparator<Task> getComparator(Perspective perspective) {

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
      case "time":
        return Collections.reverseOrder(new EstimatedTimeComparator());
      case "none":
      default:
        return null;
    }
  }

  /**
   * Compares two nullable dates, such that the non-null date takes precedence
   */
  public static class DateComparator implements Comparator<LocalDateTime> {

    int reverse = 1;

    DateComparator(boolean reverse) {
      this.reverse = reverse ? -1 : 1;
    }

    @Override
    public int compare(@Nullable LocalDateTime d1, @Nullable LocalDateTime d2) {

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
          return d1.compareTo(d2) * reverse;
        }
      }
    }
  }

  /**
   * Compares two nullable durations, such that the non-null date takes precedence
   */
  public static class DurationComparator implements Comparator<Duration> {

    int reverse = 1;

    DurationComparator(boolean reverse) {
      this.reverse = reverse ? -1 : 1;
    }

    @Override
    public int compare(@Nullable Duration d1, @Nullable Duration d2) {

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
          // Also reverse the number so that the smallest durations are at the top
          return d1.compareTo(d2) * reverse;
        }
      }
    }
  }

  public static class DueComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort first by due date ASCENDING (reverse), then rank
      int compare = new DateComparator(true).compare(t1.dateDue, t2.dateDue);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public static class DeferComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort by defer date ascending (reversed)
      int compare = new DateComparator(true).compare(t1.dateDefer, t2.dateDefer);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public static class CompletedComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort by completed date descending (normal)
      int compare = new DateComparator(false).compare(t1.dateCompleted, t2.dateCompleted);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public static class AddedComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort by added date descending (normal)
      int compare = new DateComparator(false).compare(t1.dateAdded, t2.dateAdded);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public static class ModifiedComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort by modified date descending (normal)
      int compare = new DateComparator(false).compare(t1.dateModified, t2.dateModified);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public static class FlaggedComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

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

  public static class EstimatedTimeComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Sort by estimated time ascending (reverse)
      int compare = new DurationComparator(true).compare(t1.estimatedTime, t2.estimatedTime);

      if (compare != 0) {
        return compare;
      } else {
        return t1.compareTo(t2);
      }
    }
  }

  public class ContextComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Check for nulls, and if none then compare by context name

      if (t1.contextID == null) {
        if (t2.contextID == null) {
          // Both are null and equal
          return 0;
        } else {
          // Only s1 is null, so s2 is 'greater' than it
          return -1;
        }
      } else {
        if (t2.contextID == null) {
          // Only s2 is null, so s2 is 'greater'
          return 1;
        } else {

          // Neither are null, compare normally
          Context c1 = t1.getContext(androidContext);
          Context c2 = t2.getContext(androidContext);

          int compare = c1.compareTo(c2);
          if (compare != 0) {
            return compare;
          } else {
            return t1.compareTo(t2);
          }
        }
      }
    }
  }

  public class ProjectComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

      // Check for nulls, and if none then compare by project name

      if (t1.projectID == null) {
        if (t2.projectID == null) {
          // Both are null and equal
          return 0;
        } else {
          // Only s1 is null, so s2 is 'greater' than it
          return -1;
        }
      } else {
        if (t2.projectID == null) {
          // Only s2 is null, so s2 is 'greater'
          return 1;
        } else {

          // Neither are null, compare normally
          Task p1 = t1.getProject(androidContext);
          Task p2 = t2.getProject(androidContext);

          int compare = p1.compareTo(p2);
          if (compare != 0) {
            return compare;
          } else {
            return t1.compareTo(t2);
          }
        }
      }
    }
  }
}
