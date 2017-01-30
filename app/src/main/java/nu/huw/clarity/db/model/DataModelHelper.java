package nu.huw.clarity.db.model;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import nu.huw.clarity.BuildConfig;
import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Comparators;
import nu.huw.clarity.model.Comparators.HeaderTypeComparator;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Header;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

/**
 * Like DatabaseHelper, DataModelHelper contains a number of helper classes for interacting with
 * the database, but only those specific to interacting with the data model (for views). Getters
 * and setters are all welcome.
 */
public class DataModelHelper {

  private static final String TAG = DataModelHelper.class.getSimpleName();
  private static final String[] DEFAULT_PERSPECTIVE_ORDER = new String[]{"ProcessForecast",
      "ProcessInbox", "ProcessProjects", "ProcessFlagged", "ProcessNearby", "ProcessReview"};
  private static final Duration DURATION_5M = Duration.ofMinutes(5);
  private static final Duration DURATION_15M = Duration.ofMinutes(15);
  private static final Duration DURATION_30M = Duration.ofMinutes(30);
  private static final Duration DURATION_1H = Duration.ofHours(1);
  private DatabaseHelper dbHelper;
  private android.content.Context androidContext;
  private AttachmentHelper attachmentHelper;
  private ContextHelper contextHelper;
  private FolderHelper folderHelper;
  private PerspectiveHelper perspectiveHelper;
  private SettingHelper settingHelper;
  private TaskHelper taskHelper;

  public DataModelHelper(android.content.Context context) {
    dbHelper = new DatabaseHelper(context);
    this.androidContext = context;
  }

  /**
   * Logic for using perspectives to get entries
   *
   * @param perspective The perspective used for filtering/sorting entries
   * @param parent The parent entry (nullable) to determine what to retrieve
   * @return A list with items that are at least Entries. May be a List<Task> or List<Context>.
   */
  @WorkerThread
  TreeMap<Header, TreeSet<? extends Entry>> getEntriesFromPerspective(Perspective perspective,
      Entry parent) {

    // This is just a switch to pick the items
    // Actual processing with the perspective occurs in the individual functions

    switch (perspective.id) {

      case "ProcessContexts":
        return getContexts(perspective, parent);
      case "ProcessInbox":
        return getInbox(perspective);
      case "ProcessProjects":
        return getProjects(perspective, parent);
      case "ProcessFlagged":
      default:
        return getTasks(perspective, (Task) parent);
    }
  }

  /**
   * Given an inbox perspective, this function will get a list of inbox entries suitable for
   * display in a list.
   *
   * @param perspective Perspective for filtering/organising entries, must be an inbox perspective.
   * @return List of inbox entries
   */
  TreeMap<Header, TreeSet<? extends Entry>> getContexts(Perspective perspective, Entry parent) {

    // If the parent is a project or task, then it can only have task children
    if (parent instanceof Task) return getTasks(perspective, (Task) parent);

    // Get items
    // Note that TreeSets and TreeMaps are automatically implicitly sorted based on a passed
    // comparator, or a natural ordering.

    if (contextHelper == null) contextHelper = new ContextHelper(dbHelper);
    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);

    Comparator<Header> keyComparator = new HeaderTypeComparator();
    Comparator<Task> taskComparator = new Comparators(androidContext)
        .getTaskComparator(perspective);

    TreeMap<Header, TreeSet<? extends Entry>> entryMap = new TreeMap<>(keyComparator);
    TreeSet<Context> contexts = new TreeSet<>();
    TreeSet<Task> tasks = new TreeSet<>(taskComparator);

    // Filter items
    // In this case, only tasks need to be filtered

    contexts.addAll(contextHelper.getContextsFromParent((Context) parent, androidContext));

    for (Task task : taskHelper.getTasksFromContext((Context) parent)) {
      if (filterTask(perspective, task)) {
        tasks.add(task);
      }
    }

    // Convert and return

    Header contextsHeader = new Header("Contexts");
    contextsHeader.classType = Entry.VT_CONTEXT;

    Header tasksHeader = new Header("Tasks");
    tasksHeader.classType = Entry.VT_TASK;

    entryMap.put(contextsHeader, contexts);
    entryMap.put(tasksHeader, tasks);

    return entryMap;
  }

  /**
   * Given an inbox perspective, this function will get a list of inbox entries suitable for
   * display in a list.
   *
   * @param perspective Perspective for filtering/organising entries, must be an inbox perspective.
   * @return List of inbox entries
   */
  TreeMap<Header, TreeSet<? extends Entry>> getInbox(Perspective perspective) {

    // Get items

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    Comparator<Task> taskComparator = new Comparators(androidContext)
        .getTaskComparator(perspective);

    TreeMap<Header, TreeSet<? extends Entry>> entryMap = new TreeMap<>();
    TreeSet<Task> tasks = new TreeSet<>(taskComparator);

    // Filter items

    for (Task task : taskHelper.getTasksInInbox()) {
      if (filterTask(perspective, task)) {
        tasks.add(task);
      }
    }

    // Convert and return

    Header tasksHeader = new Header(androidContext.getString(R.string.listitem_headingitems));
    entryMap.put(tasksHeader, tasks);

    return entryMap;
  }

  /**
   * Given a perspective for filtering, and a parent to determine what entries to retrieve, this
   * function will get a list of entries suitable for display in a list.
   *
   * @param perspective Perspective for filtering/organising entries
   * @param parent Nullable, parent of requested entries
   * @return List of entries corresponding to parent and perspective
   */
  TreeMap<Header, TreeSet<? extends Entry>> getProjects(Perspective perspective, Entry parent) {

    // If the parent is a project or task, then it can only have task children
    if (parent instanceof Task) return getTasks(perspective, (Task) parent);

    // Get items

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    if (folderHelper == null) folderHelper = new FolderHelper(dbHelper);

    TreeMap<Header, TreeSet<? extends Entry>> entryMap = new TreeMap<>();
    TreeSet<Entry> entries = new TreeSet<>();
    List<Entry> candidates = new ArrayList<>();

    candidates.addAll(taskHelper.getProjectsFromParent((Folder) parent));
    candidates.addAll(folderHelper.getFoldersFromParent((Folder) parent));

    // Filter items

    for (Entry entry : candidates) {
      if ((entry instanceof Task && filterTask(perspective, (Task) entry)) ||
          entry instanceof Folder) {
        entries.add(entry);
      }
    }

    // Convert and return
    // Remember, sorting is implicit according to 'natural' (rank) ordering

    Header genericHeader = new Header(androidContext.getString(R.string.listitem_headingitems));
    entryMap.put(genericHeader, entries);

    return entryMap;
  }

  /**
   * Given a perspective for filtering, and a parent to determine what tasks to retrieve, this
   * function will get a list of entries suitable for display in a list.
   *
   * @param perspective Perspective for filtering/organising tasks
   * @param parent Nullable, parent of requested tasks
   * @return List of tasks corresponding to parent and perspective
   */
  TreeMap<Header, TreeSet<? extends Entry>> getTasks(Perspective perspective, Task parent) {

    // Get items
    // If no parent is specified, get all tasks

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);

    List<Task> candidates = new ArrayList<>();
    if (parent == null) {
      candidates.addAll(taskHelper.getAllTasks());
    } else {
      candidates.addAll(taskHelper.getTasksFromParent(parent));
    }

    TreeMap<Header, TreeSet<? extends Entry>> entryMap = splitTasksIntoTreeMap(perspective,
        candidates);

    return entryMap;
  }

  /**
   * Using the given perspective, returns whether a task is suitable for that perspective.
   *
   * @param perspective Any complete Perspective object
   * @param task Any complete Task object
   * @return True if task is appropriate for the perspective
   */
  private boolean filterTask(Perspective perspective, Task task) {

    // FILTER BY STATUS

    switch (perspective.filterStatus) {
      case "complete":

        // Only completed tasks
        if (!task.isCompleted()) return false;

        break;
      case "incomplete":

        // 'incomplete' means remaining, which means no completed or dropped items
        if (!task.isRemaining()) return false;

        break;
      case "due":

        // 'due' means available, which means no blocked/deferred/on hold items as well
        if (!task.isAvailable()) return false;

        break;
    }

    // FILTER BY FLAGGED

    switch (perspective.filterFlagged) {
      case "flagged":

        // Remove if not flagged
        if (!task.flaggedEffective) return false;

        break;
      case "unflagged":

        if (task.flaggedEffective) return false;

        break;
      case "due":

        if (!(task.dueSoon || task.overdue)) return false;

        break;
      case "due-or-flagged":

        // Remove if neither due nor flagged
        if (!(task.dueSoon || task.overdue || task.flaggedEffective)) {
          return false;
        }

        break;
      case "due-and-flagged":

        // Remove if not (due soon or overdue) and (flagged or flagged effective)
        if (!((task.dueSoon || task.overdue) && task.flaggedEffective)) {
          return false;
        }

        break;
      case "due-and-unflagged":

        // Remove if (not (due soon or overdue)) and (flagged or flagged effective)
        if (!(task.dueSoon || task.overdue) && task.flaggedEffective) {
          return false;
        }
        break;
    }

    // FILTER BY DURATION

    if (task.estimatedTime != null) {
      switch (perspective.filterDuration) {
        case "5m":
          // If the comparator returns a positive integer, then the task is longer than 5 minutes
          if (task.estimatedTime.compareTo(DURATION_5M) > 0) return false;
          break;
        case "15m":
          if (task.estimatedTime.compareTo(DURATION_15M) > 0) return false;
          break;
        case "30m":
          if (task.estimatedTime.compareTo(DURATION_30M) > 0) return false;
          break;
        case "1h":
          if (task.estimatedTime.compareTo(DURATION_1H) > 0) return false;
          break;
        case "long":
          if (task.estimatedTime.compareTo(DURATION_1H) <= 0) return false;
          break;
        case "unestimated":
          return false; // Will always be estimated if estimatedTime != null
      }
    }

    return true;
  }

  private TreeMap<Header, TreeSet<? extends Entry>> splitTasksIntoTreeMap(Perspective perspective,
      List<Task> tasks) {

    Comparators comparators = new Comparators(androidContext);

    Comparator<Task> taskComparator = comparators.getTaskComparator(perspective);
    Comparator<Header> keyComparator = comparators.getKeyComparator(perspective);
    TreeMap<Header, TreeSet<? extends Entry>> entryMap = new TreeMap<>(keyComparator);

    if (perspective != null && perspective.collation != null) {

      switch (perspective.collation) {

        // Note that Java is block scoped, and we're defining a few identical variables in these
        // cases. So we enclose each in a block to avoid errors.

        case "folder": {

          Header noneHeader = new Header(
              androidContext.getString(R.string.listitem_headingnofolder));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          // For folders, get tasks that have projects in each folder

          for (Header header : new FolderHelper(dbHelper).getFolderHeaders()) {

            TreeSet<Task> treeSet = new TreeSet<>(taskComparator);

            for (Task task : tasks) {

              // If the task isn't filtered, then consider it.
              // If the task is in a folder, and the folder is the current one, then add it.
              // If the task isn't in a folder, and not already in the noneSet, then add it.
              // Otherwise just fall through.

              Task project = task.getProject(androidContext);
              if (filterTask(perspective, task)) {
                if (project != null && project.parentID != null) {
                  if (project.parentID.equals(header.folderID)) {
                    treeSet.add(task);
                  }
                } else if (!noneSet.contains(task)) {
                  noneSet.add(task);
                }
              }
            }

            entryMap.put(header, treeSet);
          }

          entryMap.put(noneHeader, noneSet);

          break;
        }

        case "project": {

          Header noneHeader = new Header(
              androidContext.getString(R.string.listitem_headingnoproject));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          // For projects, get tasks that match the project ID

          for (Header header : new TaskHelper(dbHelper).getProjectHeaders()) {

            // Implicitly sort using comparator

            TreeSet<Task> treeSet = new TreeSet<>(taskComparator);

            // Filter

            for (Task task : tasks) {

              // If the task isn't filtered, then consider it.
              // If the task is in a project, and the project is the current one, then add it.
              // If the task isn't in a project, and not already in the noneSet, then add it.
              // Otherwise just fall through.

              if (filterTask(perspective, task)) {
                if (task.projectID != null) {
                  if (task.projectID.equals(header.projectID)) {
                    treeSet.add(task);
                  }
                } else if (!noneSet.contains(task)) {
                  noneSet.add(task);
                }
              }

            }
            entryMap.put(header, treeSet);
          }

          entryMap.put(noneHeader, noneSet);

          break;
        }

        case "context": {

          Header noneHeader = new Header(
              androidContext.getString(R.string.listitem_headingnocontext));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          // For contexts, get tasks that match the context ID

          for (Header header : new ContextHelper(dbHelper).getContextHeaders()) {

            // Implicitly sort using comparator

            TreeSet<Task> treeSet = new TreeSet<>(taskComparator);

            // Filter

            for (Task task : tasks) {

              // If the task isn't filtered, then consider it.
              // If the task is in a context, and the context is the current one, then add it.
              // If the task isn't in a context, and not already in the noneSet, then add it.
              // Otherwise just fall through.

              if (filterTask(perspective, task)) {
                if (task.contextID != null) {
                  if (task.contextID.equals(header.contextID)) {
                    treeSet.add(task);
                  }
                } else if (!noneSet.contains(task)) {
                  noneSet.add(task);
                }
              }

            }
            entryMap.put(header, treeSet);
          }

          entryMap.put(noneHeader, noneSet);

          break;
        }

        case "flagged": {

          // Two very easy headers:

          Header flaggedHeader = new Header(
              androidContext.getString(R.string.listitem_headingflagged));
          flaggedHeader.flagged = true;

          Header unflaggedHeader = new Header(
              androidContext.getString(R.string.listitem_headingunflagged));
          unflaggedHeader.flagged = false;

          TreeSet<Task> flaggedSet = new TreeSet<>(taskComparator);
          TreeSet<Task> unflaggedSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {
              if (task.flaggedEffective) {
                flaggedSet.add(task);
              } else {
                unflaggedSet.add(task);
              }
            }
          }

          entryMap.put(flaggedHeader, flaggedSet);
          entryMap.put(unflaggedHeader, unflaggedSet);
          break;
        }

        case "due": {

          // We use past, earlier this year, earlier this month, earlier this week, yesterday,
          // today, tomorrow, later this week, later this month, later this week, the future,
          // and no due date

          Header todayHeader = new Header(androidContext.getString(R.string.listitem_headingtoday));
          todayHeader.dateDue = LocalDate.now().plusDays(1).atStartOfDay();
          TreeSet<Task> todaySet = new TreeSet<>(taskComparator);

          Header yesterdayHeader = new Header(
              androidContext.getString(R.string.listitem_headingyesterday));
          yesterdayHeader.dateDue = LocalDate.now().minusDays(1).atStartOfDay();
          TreeSet<Task> yesterdaySet = new TreeSet<>(taskComparator);

          Header earlierWeekHeader = new Header(
              androidContext.getString(R.string.listitem_headingweekearlier));
          earlierWeekHeader.dateDue = LocalDate.now().minusWeeks(1).atStartOfDay();
          TreeSet<Task> earlierWeekSet = new TreeSet<>(taskComparator);

          Header earlierMonthHeader = new Header(
              androidContext.getString(R.string.listitem_headingmonthearlier));
          earlierMonthHeader.dateDue = LocalDate.now().minusMonths(1).atStartOfDay();
          TreeSet<Task> earlierMonthSet = new TreeSet<>(taskComparator);

          Header earlierYearHeader = new Header(
              androidContext.getString(R.string.listitem_headingyearearlier));
          earlierYearHeader.dateDue = LocalDate.now().minusYears(1).atStartOfDay();
          TreeSet<Task> earlierYearSet = new TreeSet<>(taskComparator);

          Header pastHeader = new Header(androidContext.getString(R.string.listitem_headingpast));
          pastHeader.dateDue = LocalDateTime.MIN;
          TreeSet<Task> pastSet = new TreeSet<>(taskComparator);

          Header tomorrowHeader = new Header(
              androidContext.getString(R.string.listitem_headingtomorrow));
          tomorrowHeader.dateDue = LocalDate.now().plusDays(2).atStartOfDay();
          TreeSet<Task> tomorrowSet = new TreeSet<>(taskComparator);

          Header laterWeekHeader = new Header(
              androidContext.getString(R.string.listitem_headingweeklater));
          laterWeekHeader.dateDue = LocalDate.now().plusWeeks(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterWeekSet = new TreeSet<>(taskComparator);

          Header laterMonthHeader = new Header(
              androidContext.getString(R.string.listitem_headingmonthlater));
          laterMonthHeader.dateDue = LocalDate.now().plusMonths(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterMonthSet = new TreeSet<>(taskComparator);

          Header laterYearHeader = new Header(
              androidContext.getString(R.string.listitem_headingyearlater));
          laterYearHeader.dateDue = LocalDate.now().plusYears(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterYearSet = new TreeSet<>(taskComparator);

          Header futureHeader = new Header(
              androidContext.getString(R.string.listitem_headingfuture));
          futureHeader.dateDue = LocalDateTime.MAX;
          TreeSet<Task> futureSet = new TreeSet<>(taskComparator);

          Header noneHeader = new Header(androidContext.getString(R.string.listitem_headingnodue));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {

              // A long and convoluted if statement to sort into these bins

              if (task.dateDueEffective != null) {
                if (task.dateDueEffective.isBefore(todayHeader.dateDue)) {

                  // Default to past-date sorting

                  if (task.dateDueEffective.isAfter(todayHeader.dateDue.minusDays(1))) {
                    todaySet.add(task);
                  } else if (task.dateDueEffective.isAfter(yesterdayHeader.dateDue)) {
                    yesterdaySet.add(task);
                  } else if (task.dateDueEffective.isAfter(earlierWeekHeader.dateDue)) {
                    earlierWeekSet.add(task);
                  } else if (task.dateDueEffective.isAfter(earlierMonthHeader.dateDue)) {
                    earlierMonthSet.add(task);
                  } else if (task.dateDueEffective.isAfter(earlierYearHeader.dateDue)) {
                    earlierYearSet.add(task);
                  } else {
                    pastSet.add(task);
                  }

                } else if (task.dateDueEffective.isBefore(tomorrowHeader.dateDue)) {
                  tomorrowSet.add(task);
                } else if (task.dateDueEffective.isBefore(laterWeekHeader.dateDue)) {
                  laterWeekSet.add(task);
                } else if (task.dateDueEffective.isBefore(laterMonthHeader.dateDue)) {
                  laterMonthSet.add(task);
                } else if (task.dateDueEffective.isBefore(laterYearHeader.dateDue)) {
                  laterYearSet.add(task);
                } else {
                  futureSet.add(task);
                }
              } else {
                if (!perspective.id.equals("ProcessForecast")) {

                  // The Forecast perspective does not have a 'none' option

                  noneSet.add(task);
                }
              }
            }
          }

          entryMap.put(pastHeader, pastSet);
          entryMap.put(earlierYearHeader, earlierYearSet);
          entryMap.put(earlierMonthHeader, earlierMonthSet);
          entryMap.put(earlierWeekHeader, earlierWeekSet);
          entryMap.put(yesterdayHeader, yesterdaySet);
          entryMap.put(todayHeader, todaySet);
          entryMap.put(tomorrowHeader, tomorrowSet);
          entryMap.put(laterWeekHeader, laterWeekSet);
          entryMap.put(laterMonthHeader, laterMonthSet);
          entryMap.put(laterYearHeader, laterYearSet);
          entryMap.put(futureHeader, futureSet);
          entryMap.put(noneHeader, noneSet);
          break;
        }

        case "defer": {

          // We use the past, earlier this year, earlier this month, earlier this week, yesterday,
          // today, tomorrow, later this week, later this month, later this week, the future,
          // and available now

          Header todayHeader = new Header(androidContext.getString(R.string.listitem_headingtoday));
          todayHeader.dateDefer = LocalDate.now().plusDays(1).atStartOfDay();
          TreeSet<Task> todaySet = new TreeSet<>(taskComparator);

          Header yesterdayHeader = new Header(
              androidContext.getString(R.string.listitem_headingyesterday));
          yesterdayHeader.dateDefer = LocalDate.now().minusDays(1).atStartOfDay();
          TreeSet<Task> yesterdaySet = new TreeSet<>(taskComparator);

          Header earlierWeekHeader = new Header(
              androidContext.getString(R.string.listitem_headingweekearlier));
          earlierWeekHeader.dateDefer = LocalDate.now().minusWeeks(1).atStartOfDay();
          TreeSet<Task> earlierWeekSet = new TreeSet<>(taskComparator);

          Header earlierMonthHeader = new Header(
              androidContext.getString(R.string.listitem_headingmonthearlier));
          earlierMonthHeader.dateDefer = LocalDate.now().minusMonths(1).atStartOfDay();
          TreeSet<Task> earlierMonthSet = new TreeSet<>(taskComparator);

          Header earlierYearHeader = new Header(
              androidContext.getString(R.string.listitem_headingyearearlier));
          earlierYearHeader.dateDefer = LocalDate.now().minusYears(1).atStartOfDay();
          TreeSet<Task> earlierYearSet = new TreeSet<>(taskComparator);

          Header pastHeader = new Header(androidContext.getString(R.string.listitem_headingpast));
          pastHeader.dateDefer = LocalDateTime.MIN;
          TreeSet<Task> pastSet = new TreeSet<>(taskComparator);

          Header tomorrowHeader = new Header(
              androidContext.getString(R.string.listitem_headingtomorrow));
          tomorrowHeader.dateDefer = LocalDate.now().plusDays(2).atStartOfDay();
          TreeSet<Task> tomorrowSet = new TreeSet<>(taskComparator);

          Header laterWeekHeader = new Header(
              androidContext.getString(R.string.listitem_headingweeklater));
          laterWeekHeader.dateDefer = LocalDate.now().plusWeeks(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterWeekSet = new TreeSet<>(taskComparator);

          Header laterMonthHeader = new Header(
              androidContext.getString(R.string.listitem_headingmonthlater));
          laterMonthHeader.dateDefer = LocalDate.now().plusMonths(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterMonthSet = new TreeSet<>(taskComparator);

          Header laterYearHeader = new Header(
              androidContext.getString(R.string.listitem_headingyearlater));
          laterYearHeader.dateDefer = LocalDate.now().plusYears(1).plusDays(1).atStartOfDay();
          TreeSet<Task> laterYearSet = new TreeSet<>(taskComparator);

          Header futureHeader = new Header(
              androidContext.getString(R.string.listitem_headingfuture));
          futureHeader.dateDefer = LocalDateTime.MAX;
          TreeSet<Task> futureSet = new TreeSet<>(taskComparator);

          Header noneHeader = new Header(
              androidContext.getString(R.string.listitem_headingnodefer));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {

              // A long and convoluted if statement to sort into these bins

              if (task.dateDeferEffective != null) {
                if (task.dateDeferEffective.isBefore(todayHeader.dateDefer)) {

                  // Default to past-date sorting

                  if (task.dateDeferEffective.isAfter(todayHeader.dateDefer.minusDays(1))) {
                    todaySet.add(task);
                  } else if (task.dateDeferEffective.isAfter(yesterdayHeader.dateDefer)) {
                    yesterdaySet.add(task);
                  } else if (task.dateDeferEffective.isAfter(earlierWeekHeader.dateDefer)) {
                    earlierWeekSet.add(task);
                  } else if (task.dateDeferEffective.isAfter(earlierMonthHeader.dateDefer)) {
                    earlierMonthSet.add(task);
                  } else if (task.dateDeferEffective.isAfter(earlierYearHeader.dateDefer)) {
                    earlierYearSet.add(task);
                  } else {
                    pastSet.add(task);
                  }

                } else if (task.dateDeferEffective.isBefore(tomorrowHeader.dateDefer)) {
                  tomorrowSet.add(task);
                } else if (task.dateDeferEffective.isBefore(laterWeekHeader.dateDefer)) {
                  laterWeekSet.add(task);
                } else if (task.dateDeferEffective.isBefore(laterMonthHeader.dateDefer)) {
                  laterMonthSet.add(task);
                } else if (task.dateDeferEffective.isBefore(laterYearHeader.dateDefer)) {
                  laterYearSet.add(task);
                } else {
                  futureSet.add(task);
                }
              } else {
                noneSet.add(task);
              }
            }
          }

          entryMap.put(pastHeader, pastSet);
          entryMap.put(earlierYearHeader, earlierYearSet);
          entryMap.put(earlierMonthHeader, earlierMonthSet);
          entryMap.put(earlierWeekHeader, earlierWeekSet);
          entryMap.put(yesterdayHeader, yesterdaySet);
          entryMap.put(todayHeader, todaySet);
          entryMap.put(tomorrowHeader, tomorrowSet);
          entryMap.put(laterWeekHeader, laterWeekSet);
          entryMap.put(laterMonthHeader, laterMonthSet);
          entryMap.put(laterYearHeader, laterYearSet);
          entryMap.put(futureHeader, futureSet);
          entryMap.put(noneHeader, noneSet);
          break;
        }

        case "completed": {

          // We use today, yesterday, this week, this month, this year, and the past

          Header todayHeader = new Header(androidContext.getString(R.string.listitem_headingtoday));
          todayHeader.dateCompleted = LocalDate.now().atStartOfDay();
          TreeSet<Task> todaySet = new TreeSet<>(taskComparator);

          Header tomorrowHeader = new Header(
              androidContext.getString(R.string.listitem_headingyesterday));
          tomorrowHeader.dateCompleted = LocalDate.now().minusDays(1).atStartOfDay();
          TreeSet<Task> tomorrowSet = new TreeSet<>(taskComparator);

          Header weekHeader = new Header(androidContext.getString(R.string.listitem_headingweek));
          weekHeader.dateCompleted = LocalDate.now().minusWeeks(1).atStartOfDay();
          TreeSet<Task> weekSet = new TreeSet<>(taskComparator);

          Header monthHeader = new Header(androidContext.getString(R.string.listitem_headingmonth));
          monthHeader.dateCompleted = LocalDate.now().minusMonths(1).atStartOfDay();
          TreeSet<Task> monthSet = new TreeSet<>(taskComparator);

          Header yearHeader = new Header(androidContext.getString(R.string.listitem_headingyear));
          yearHeader.dateCompleted = LocalDate.now().minusYears(1).atStartOfDay();
          TreeSet<Task> yearSet = new TreeSet<>(taskComparator);

          Header pastHeader = new Header(androidContext.getString(R.string.listitem_headingpast));
          pastHeader.dateCompleted = LocalDateTime.MIN;
          TreeSet<Task> pastSet = new TreeSet<>(taskComparator);

          Header noneHeader = new Header(
              androidContext.getString(R.string.listitem_headingnocomplete));
          TreeSet<Task> noneSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {

              // A long and convoluted if statement to sort into these bins

              if (task.dateCompleted != null) {
                if (task.dateCompleted.isAfter(todayHeader.dateCompleted)) {
                  todaySet.add(task);
                } else if (task.dateCompleted.isAfter(tomorrowHeader.dateCompleted)) {
                  tomorrowSet.add(task);
                } else if (task.dateCompleted.isAfter(weekHeader.dateCompleted)) {
                  weekSet.add(task);
                } else if (task.dateCompleted.isAfter(monthHeader.dateCompleted)) {
                  monthSet.add(task);
                } else if (task.dateCompleted.isAfter(yearHeader.dateCompleted)) {
                  yearSet.add(task);
                } else {
                  pastSet.add(task);
                }
              } else {
                noneSet.add(task);
              }
            }
          }

          entryMap.put(todayHeader, todaySet);
          entryMap.put(tomorrowHeader, tomorrowSet);
          entryMap.put(weekHeader, weekSet);
          entryMap.put(monthHeader, monthSet);
          entryMap.put(yearHeader, yearSet);
          entryMap.put(pastHeader, pastSet);
          entryMap.put(noneHeader, noneSet);
          break;
        }

        case "added": {

          // We use today, yesterday, this week, this month, this year, and the past

          Header todayHeader = new Header(androidContext.getString(R.string.listitem_headingtoday));
          todayHeader.dateAdded = LocalDate.now().atStartOfDay();
          TreeSet<Task> todaySet = new TreeSet<>(taskComparator);

          Header tomorrowHeader = new Header(
              androidContext.getString(R.string.listitem_headingyesterday));
          tomorrowHeader.dateAdded = LocalDate.now().minusDays(1).atStartOfDay();
          TreeSet<Task> tomorrowSet = new TreeSet<>(taskComparator);

          Header weekHeader = new Header(androidContext.getString(R.string.listitem_headingweek));
          weekHeader.dateAdded = LocalDate.now().minusWeeks(1).atStartOfDay();
          TreeSet<Task> weekSet = new TreeSet<>(taskComparator);

          Header monthHeader = new Header(androidContext.getString(R.string.listitem_headingmonth));
          monthHeader.dateAdded = LocalDate.now().minusMonths(1).atStartOfDay();
          TreeSet<Task> monthSet = new TreeSet<>(taskComparator);

          Header yearHeader = new Header(androidContext.getString(R.string.listitem_headingyear));
          yearHeader.dateAdded = LocalDate.now().minusYears(1).atStartOfDay();
          TreeSet<Task> yearSet = new TreeSet<>(taskComparator);

          Header pastHeader = new Header(androidContext.getString(R.string.listitem_headingpast));
          pastHeader.dateAdded = LocalDateTime.MIN;
          TreeSet<Task> pastSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {

              // A long and convoluted if statement to sort into these bins

              if (task.dateAdded != null) {
                if (task.dateAdded.isAfter(todayHeader.dateAdded)) {
                  todaySet.add(task);
                } else if (task.dateAdded.isAfter(tomorrowHeader.dateAdded)) {
                  tomorrowSet.add(task);
                } else if (task.dateAdded.isAfter(weekHeader.dateAdded)) {
                  weekSet.add(task);
                } else if (task.dateAdded.isAfter(monthHeader.dateAdded)) {
                  monthSet.add(task);
                } else if (task.dateAdded.isAfter(yearHeader.dateAdded)) {
                  yearSet.add(task);
                } else {
                  pastSet.add(task);
                }
              }
            }
          }

          entryMap.put(todayHeader, todaySet);
          entryMap.put(tomorrowHeader, tomorrowSet);
          entryMap.put(weekHeader, weekSet);
          entryMap.put(monthHeader, monthSet);
          entryMap.put(yearHeader, yearSet);
          entryMap.put(pastHeader, pastSet);
          break;
        }

        case "modified": {

          // We use today, yesterday, this week, this month, this year, and the past

          Header todayHeader = new Header(androidContext.getString(R.string.listitem_headingtoday));
          todayHeader.dateModified = LocalDate.now().atStartOfDay();
          TreeSet<Task> todaySet = new TreeSet<>(taskComparator);

          Header tomorrowHeader = new Header(
              androidContext.getString(R.string.listitem_headingyesterday));
          tomorrowHeader.dateModified = LocalDate.now().minusDays(1).atStartOfDay();
          TreeSet<Task> tomorrowSet = new TreeSet<>(taskComparator);

          Header weekHeader = new Header(androidContext.getString(R.string.listitem_headingweek));
          weekHeader.dateModified = LocalDate.now().minusWeeks(1).atStartOfDay();
          TreeSet<Task> weekSet = new TreeSet<>(taskComparator);

          Header monthHeader = new Header(androidContext.getString(R.string.listitem_headingmonth));
          monthHeader.dateModified = LocalDate.now().minusMonths(1).atStartOfDay();
          TreeSet<Task> monthSet = new TreeSet<>(taskComparator);

          Header yearHeader = new Header(androidContext.getString(R.string.listitem_headingyear));
          yearHeader.dateModified = LocalDate.now().minusYears(1).atStartOfDay();
          TreeSet<Task> yearSet = new TreeSet<>(taskComparator);

          Header pastHeader = new Header(androidContext.getString(R.string.listitem_headingpast));
          pastHeader.dateModified = LocalDateTime.MIN;
          TreeSet<Task> pastSet = new TreeSet<>(taskComparator);

          for (Task task : tasks) {
            if (filterTask(perspective, task)) {

              // A long and convoluted if statement to sort into these bins

              if (task.dateModified != null) {
                if (task.dateModified.isAfter(todayHeader.dateModified)) {
                  todaySet.add(task);
                } else if (task.dateModified.isAfter(tomorrowHeader.dateModified)) {
                  tomorrowSet.add(task);
                } else if (task.dateModified.isAfter(weekHeader.dateModified)) {
                  weekSet.add(task);
                } else if (task.dateModified.isAfter(monthHeader.dateModified)) {
                  monthSet.add(task);
                } else if (task.dateModified.isAfter(yearHeader.dateModified)) {
                  yearSet.add(task);
                } else {
                  pastSet.add(task);
                }
              }
            }
          }

          entryMap.put(todayHeader, todaySet);
          entryMap.put(tomorrowHeader, tomorrowSet);
          entryMap.put(weekHeader, weekSet);
          entryMap.put(monthHeader, monthSet);
          entryMap.put(yearHeader, yearSet);
          entryMap.put(pastHeader, pastSet);
          break;
        }

        default: {
          Header tasksHeader = new Header(androidContext.getString(R.string.listitem_headingtasks));
          TreeSet<Task> treeSet = new TreeSet<>();
          for (Task task : tasks) {
            if (filterTask(perspective, task)) {
              treeSet.add(task);
            }
          }
          entryMap.put(tasksHeader, treeSet);
        }
      }
    } else {
      Header tasksHeader = new Header(androidContext.getString(R.string.listitem_headingtasks));
      TreeSet<Task> treeSet = new TreeSet<>();
      for (Task task : tasks) {
        if (filterTask(perspective, task)) {
          treeSet.add(task);
        }
      }
      entryMap.put(tasksHeader, treeSet);
    }
    return entryMap;
  }

  /**
   * This function will get a list of perspectives from the database
   *
   * @param defaultSet If true, only get the default set of perspectives acccording to the iOS app,
   * which are: Forecast, Inbox, Projects, Contexts, Flagged, Nearby & Review
   */
  public List<Perspective> getPerspectives(boolean defaultSet) {

    if (BuildConfig.DEBUG) {
      defaultSet = false;
    }

    // Get items

    if (perspectiveHelper == null) {
      perspectiveHelper = new PerspectiveHelper(dbHelper, androidContext);
    }
    HashMap<String, Perspective> perspectives = perspectiveHelper
        .getPerspectivesFromSelection(null, null);
    Perspective forecast = perspectiveHelper.getForecast();
    perspectives.put(forecast.id, forecast);

    // Filter + Sort

    String[] perspectiveOrder = getPerspectiveOrder();
    if (defaultSet || perspectiveOrder == null) {
      perspectiveOrder = DEFAULT_PERSPECTIVE_ORDER;
    }
    List<Perspective> results = new ArrayList<>();

    for (String id : perspectiveOrder) {

      // Since we have all of our perspectives in a HashMap indexed by ID, we can use the IDs stored
      // in settings to filter and sort our list. So we've found all the IDs in order, and we
      // iterate through them and change them (since OmniFocus has small problems), then add them to
      // the new list from the HashMap

      id = id.replaceAll("(Process[a-zA-Z]+)\\.v2", "$1"); // Strip the '.v2' if necessary
      if (id.equals("ProcessFlaggedItems")) id = "ProcessFlagged"; // ProcessFlagged has a problem

      Perspective perspective = perspectives.get(id);
      if (perspective != null) results.add(perspective);
    }

    // Return

    return results;
  }

  /**
   * Get the special Forecast perspective
   */

  public Perspective getForecastPerspective() {

    if (perspectiveHelper == null) {
      perspectiveHelper = new PerspectiveHelper(dbHelper, androidContext);
    }
    return perspectiveHelper.getForecast();
  }

  /**
   * Get a blank placeholder perspective
   */
  public Perspective getPlaceholderPerspective() {

    if (perspectiveHelper == null) {
      perspectiveHelper = new PerspectiveHelper(dbHelper, androidContext);
    }
    return perspectiveHelper.getPlaceholder();
  }

  /**
   * Get a string array representing the raw order of perspectives in the sidebar
   */
  @Nullable
  public String[] getPerspectiveOrder() {
    String str = getSettingFromID("PerspectiveOrder_v2");
    if (str != null) {
      return str.split(",");
    } else {
      return null;
    }
  }

  /**
   * Get all attachments from the current task
   */
  public List<Attachment> getAttachmentsFromTask(Task task) {

    if (attachmentHelper == null) {
      attachmentHelper = new AttachmentHelper(dbHelper);
    }
    return attachmentHelper.getAttachmentsFromParent(task);
  }

  /**
   * Get the context with the specified ID
   *
   * @param id ID of a context
   */
  public Context getContextFromID(String id) {

    if (id == null) throw new NullPointerException("Null ID provided");
    if (contextHelper == null) contextHelper = new ContextHelper(dbHelper);

    return contextHelper.getContextFromID(id);
  }

  /**
   * Get the folder with the specified ID
   *
   * @param id ID of a folder
   */
  public Folder getFolderFromID(String id) {

    if (id == null) throw new NullPointerException("Null ID provided");
    if (folderHelper == null) folderHelper = new FolderHelper(dbHelper);

    return folderHelper.getFolderFromID(id);
  }

  /**
   * Get the setting with the specified ID
   *
   * @param id ID of a setting
   */
  public String getSettingFromID(String id) {

    if (id == null) throw new NullPointerException("Null ID provided");
    if (settingHelper == null) settingHelper = new SettingHelper(dbHelper);

    return settingHelper.getSettingFromID(id);
  }

  /**
   * Get the task with the specified ID
   *
   * @param id ID of a task
   */
  public Task getTaskFromID(String id) {

    if (id == null) throw new NullPointerException("Null ID provided");
    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);

    return taskHelper.getTaskFromID(id);
  }

  /**
   * Get a list of tasks that are currently overdue
   */
  public List<Task> getNewOverdueTasks() {
    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    return taskHelper.getNewOverdue();
  }
}
