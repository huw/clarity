package nu.huw.clarity.db.model;

import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import nu.huw.clarity.BuildConfig;
import nu.huw.clarity.db.DatabaseHelper;
import nu.huw.clarity.model.Comparators;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;
import org.threeten.bp.Duration;

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
  public List<Entry> getEntriesFromPerspective(Perspective perspective, Entry parent) {

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
  List<Entry> getContexts(Perspective perspective, Entry parent) {

    // If the parent is a project or task, then it can only have task children
    if (parent instanceof Task) return getTasks(perspective, (Task) parent);

    // Get items
    // We want to display a list of sorted tasks beneath the contexts, so we get each
    // separately and sort them separately, then join them back up at the end.

    if (contextHelper == null) contextHelper = new ContextHelper(dbHelper, androidContext);
    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    List<Context> contexts = contextHelper.getContextsFromParent((Context) parent);
    List<Task> tasks = new ArrayList<>();
    List<Entry> entries = new ArrayList<>();

    // Filter items
    // In this case, only tasks need to be filtered

    for (Task task : taskHelper.getTasksFromContext((Context) parent)) {
      if (filterTask(perspective, task)) {
        tasks.add(task);
      }
    }

    // Sort items

    Comparator<Task> comparator = new Comparators(androidContext).getComparator(perspective);
    Collections.sort(tasks, comparator);
    Collections.sort(contexts);

    // Convert and return

    entries.addAll(contexts);
    entries.addAll(tasks);

    return entries;
  }

  /**
   * Given an inbox perspective, this function will get a list of inbox entries suitable for
   * display in a list.
   *
   * @param perspective Perspective for filtering/organising entries, must be an inbox perspective.
   * @return List of inbox entries
   */
  List<Entry> getInbox(Perspective perspective) {

    // Get items

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    List<Task> candidates = taskHelper.getTasksInInbox();
    List<Task> tasks = new ArrayList<>();

    // Filter items

    for (Task task : candidates) {
      if (filterTask(perspective, task)) {
        tasks.add(task);
      }
    }

    // Sort items

    Comparator<Task> comparator = new Comparators(androidContext).getComparator(perspective);
    Collections.sort(tasks, comparator);

    // Convert and return

    return new ArrayList<Entry>(tasks);
  }

  /**
   * Given a perspective for filtering, and a parent to determine what entries to retrieve, this
   * function will get a list of entries suitable for display in a list.
   *
   * @param perspective Perspective for filtering/organising entries
   * @param parent Nullable, parent of requested entries
   * @return List of entries corresponding to parent and perspective
   */
  List<Entry> getProjects(Perspective perspective, Entry parent) {

    // If the parent is a project or task, then it can only have task children
    if (parent instanceof Task) return getTasks(perspective, (Task) parent);

    // Get items

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    if (folderHelper == null) folderHelper = new FolderHelper(dbHelper);
    List<Entry> candidates = new ArrayList<>();
    List<Entry> entries = new ArrayList<>();

    candidates.addAll(taskHelper.getProjectsFromParent((Folder) parent));
    candidates.addAll(folderHelper.getFoldersFromParent((Folder) parent));

    // Filter items

    for (Entry entry : candidates) {
      if ((entry instanceof Task && filterTask(perspective, (Task) entry)) ||
          entry instanceof Folder) {
        entries.add(entry);
      }
    }

    // Sort items

    Collections.sort(entries);
    return entries;
  }

  /**
   * Given a perspective for filtering, and a parent to determine what tasks to retrieve, this
   * function will get a list of entries suitable for display in a list.
   *
   * @param perspective Perspective for filtering/organising tasks
   * @param parent Nullable, parent of requested tasks
   * @return List of tasks corresponding to parent and perspective
   */
  List<Entry> getTasks(Perspective perspective, Task parent) {

    // Get items
    // If no parent is specified, get all tasks

    if (taskHelper == null) taskHelper = new TaskHelper(dbHelper);
    List<Task> candidates = new ArrayList<>();
    List<Task> tasks = new ArrayList<>();

    if (parent == null) {
      candidates.addAll(taskHelper.getAllTasks());
    } else {
      candidates.addAll(taskHelper.getTasksFromParent(parent));
    }

    // Filter items
    // (supposed to be faster than SQL, is definitely easier)

    for (Task task : candidates) {
      if (filterTask(perspective, task)) {
        tasks.add(task);
      }
    }

    // Sort items
    // (should also be faster than SQL)

    Comparator<Task> comparator = new Comparators(androidContext).getComparator(perspective);
    Collections.sort(tasks, comparator);

    // Convert and return

    return new ArrayList<Entry>(tasks);
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
   * Get the context with the specified ID
   *
   * @param id ID of a context
   */
  public Context getContextFromID(String id) {

    if (id == null) throw new NullPointerException("Null ID provided");
    if (contextHelper == null) contextHelper = new ContextHelper(dbHelper, androidContext);

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
}
