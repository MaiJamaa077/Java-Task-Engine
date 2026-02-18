package de.buw.se;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class BlackBoxTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
    }

    @Test
    void addValidTask_taskAppearsWithCorrectFields() {
        manager.addTask("Study Math", "High", "15/07/2026", "Study");

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "Exactly one task should have been added");

        Task t = tasks.get(0);
        assertEquals("Study Math",      t.getTitle());
        assertEquals("High",            t.getPriority());
        assertEquals("15/07/2026",      t.getDueDate());
        assertEquals("Study",           t.getCategory());
        assertEquals(Task.NOT_STARTED,  t.getStatus(),
                "New tasks must default to 'Not Started'");
    }

    @Test
    void emptyTitleRejected_byValidator() {
        assertFalse(TaskValidator.isTitleValid(""),
                "Empty string must be invalid");
        assertFalse(TaskValidator.isTitleValid("   "),
                "Whitespace-only string must be invalid");
        assertFalse(TaskValidator.isTitleValid(null),
                "Null must be invalid");
        assertTrue(TaskValidator.isTitleValid("Study Math"),
                "Non-empty title must be valid");
    }

    @Test
    void invalidDateFormats_rejectedByValidator() {
        assertFalse(TaskValidator.isValidDateFormat("2026-07-15"),
                "ISO format (yyyy-MM-dd) uses hyphens – must be invalid");
        assertFalse(TaskValidator.isValidDateFormat("15-07-2026"),
                "Hyphen-separated dd-MM-yyyy must be invalid");
        assertFalse(TaskValidator.isValidDateFormat("1/7/2026"),
                "Single-digit day/month without leading zero must be invalid");
        assertFalse(TaskValidator.isValidDateFormat(""),
                "Empty string must be invalid");
        assertFalse(TaskValidator.isValidDateFormat(null),
                "Null must be invalid");
        assertTrue(TaskValidator.isValidDateFormat("15/07/2026"),
                "Correct dd/mm/yyyy format must be valid");
    }

    @Test
    void emptyDueDate_rejectedByValidator() {
        assertFalse(TaskValidator.isDueDatePresent(""),
                "Empty due date must be invalid");
        assertFalse(TaskValidator.isDueDatePresent("   "),
                "Whitespace-only due date must be invalid");
        assertFalse(TaskValidator.isDueDatePresent(null),
                "Null due date must be invalid");
        assertTrue(TaskValidator.isDueDatePresent("15/07/2026"),
                "Non-empty due date must be present");
    }

    @Test
    void editTaskTitle_titleUpdatedOtherFieldsUnchanged() {
        manager.addTask("Football", "Medium", "02/06/2026", "Other");
        int id = manager.getTasks().get(0).getId();

        manager.editTask(id, "Football Practice", "Medium", "02/06/2026", "Other", Task.NOT_STARTED);

        Task t = manager.getTasks().get(0);
        assertEquals("Football Practice", t.getTitle(),    "Title must be updated");
        assertEquals("Medium",            t.getPriority(), "Priority must be unchanged");
        assertEquals("02/06/2026",        t.getDueDate(),  "Due date must be unchanged");
        assertEquals("Other",             t.getCategory(), "Category must be unchanged");
        assertEquals(Task.NOT_STARTED,    t.getStatus(),   "Status must be unchanged");
    }

    @Test
    void deleteTask_confirmYes_taskRemovedFromList() {
        manager.addTask("Morning Lecture", "High", "02/06/2026", "Study");
        int id = manager.getTasks().get(0).getId();
        assertEquals(1, manager.getTasks().size(), "Pre-condition: 1 task exists");

        manager.deleteTask(id);

        assertEquals(0, manager.getTasks().size(), "Task must be removed after deletion");
    }

    @Test
    void deleteTask_cancelNo_taskRemainsInList() {
        manager.addTask("Morning Lecture", "High", "02/06/2026", "Study");
        assertEquals(1, manager.getTasks().size(), "Pre-condition: 1 task exists");
        assertEquals(1, manager.getTasks().size(),
                "Task must remain when deletion is cancelled");
        assertEquals("Morning Lecture", manager.getTasks().get(0).getTitle(),
                "Task content must be unchanged");
    }

    @Test
    void updateTaskStatus_statusChangedCorrectly() {
        manager.addTask("Football", "Medium", "02/06/2026", "Other");
        int id = manager.getTasks().get(0).getId();
        assertEquals(Task.NOT_STARTED, manager.getTasks().get(0).getStatus(),
                "Pre-condition: status is 'Not Started'");

        manager.updateTask(id, Task.IN_PROGRESS);

        assertEquals(Task.IN_PROGRESS, manager.getTasks().get(0).getStatus(),
                "Status must be updated to 'In Progress'");
    }

    @Test
    void searchByTitleKeyword_onlyMatchingTasksReturned() {
        manager.addTask("Morning Lecture", "High",   "02/06/2026", "Study");
        manager.addTask("Football",        "Medium", "02/06/2026", "Other");
        manager.addTask("Evening Lecture", "Low",    "03/06/2026", "Study");

        List<Task> results = manager.searchTasks("lecture");

        assertEquals(2, results.size(), "Two tasks contain 'lecture' in their title");
        assertTrue(results.stream()
                .allMatch(t -> t.getTitle().toLowerCase().contains("lecture")),
                "Every result must contain the keyword in the title");
    }

    @Test
    void searchByCategoryKeyword_onlyMatchingCategoryTasksReturned() {
        manager.addTask("Morning Lecture",    "High",   "02/06/2026", "Study");
        manager.addTask("Football",           "Medium", "02/06/2026", "Other");
        manager.addTask("Project Submission", "High",   "05/06/2026", "Study");

        List<Task> results = manager.searchTasks("study");

        assertEquals(2, results.size(), "Two tasks belong to the 'Study' category");
        assertTrue(results.stream()
                .allMatch(t -> t.getCategory().equalsIgnoreCase("Study")),
                "Every result must be in the 'Study' category");
    }

    @Test
    void searchByPriorityKeyword_onlyMatchingPriorityTasksReturned() {
        manager.addTask("Task A", "High",   "01/07/2026", "Work");
        manager.addTask("Task B", "Medium", "02/07/2026", "Work");
        manager.addTask("Task C", "Low",    "03/07/2026", "Work");

        List<Task> results = manager.searchTasks("high");

        assertEquals(1, results.size());
        assertEquals("High", results.get(0).getPriority());
    }

    // BB-12
    @Test
    void searchWithNoMatch_returnsEmptyList() {
        manager.addTask("Morning Lecture", "High", "02/06/2026", "Study");
        manager.addTask("Football",        "Low",  "02/06/2026", "Other");

        List<Task> results = manager.searchTasks("zzzznonexistent");

        assertTrue(results.isEmpty(), "No tasks should match a nonsense keyword");
    }
    
    @Test
    void filterByStatus_onlyMatchingStatusTasksReturned() {
        manager.addTask("Task A", "High", "01/07/2026", "Work");
        manager.addTask("Task B", "Low",  "02/07/2026", "Study");
        manager.updateTask(manager.getTasks().get(0).getId(), Task.COMPLETED);

        List<Task> results = manager.filterTasks("", Task.COMPLETED, "All Priorities");

        assertEquals(1, results.size());
        assertEquals(Task.COMPLETED, results.get(0).getStatus());
    }

    @Test
    void filterByPriority_onlyMatchingPriorityTasksReturned() {
        manager.addTask("Task A", "High",   "01/07/2026", "Work");
        manager.addTask("Task B", "Medium", "02/07/2026", "Study");
        manager.addTask("Task C", "Low",    "03/07/2026", "Other");

        List<Task> results = manager.filterTasks("", "All Statuses", "High");

        assertEquals(1, results.size());
        assertEquals("High", results.get(0).getPriority());
    }
    
    @Test
    void sortByPriority_ascending_ordersHighThenMediumThenLow() {
        manager.addTask("Low Task",    "Low",    "01/07/2026", "Work");
        manager.addTask("High Task",   "High",   "02/07/2026", "Work");
        manager.addTask("Medium Task", "Medium", "03/07/2026", "Work");

        manager.sortByPriority(true);

        List<Task> tasks = manager.getTasks();
        assertEquals("High",   tasks.get(0).getPriority());
        assertEquals("Medium", tasks.get(1).getPriority());
        assertEquals("Low",    tasks.get(2).getPriority());
    }

    @Test
    void sortByPriority_descending_ordersLowThenMediumThenHigh() {
        manager.addTask("Low Task",    "Low",    "01/07/2026", "Work");
        manager.addTask("High Task",   "High",   "02/07/2026", "Work");
        manager.addTask("Medium Task", "Medium", "03/07/2026", "Work");

        manager.sortByPriority(false);

        List<Task> tasks = manager.getTasks();
        assertEquals("Low",    tasks.get(0).getPriority());
        assertEquals("Medium", tasks.get(1).getPriority());
        assertEquals("High",   tasks.get(2).getPriority());
    }

    @Test
    void sortByDueDate_ascending_ordersEarliestFirst() {
        manager.addTask("Late Task",  "High",   "31/12/2026", "Work");
        manager.addTask("Early Task", "Low",    "01/01/2026", "Work");
        manager.addTask("Mid Task",   "Medium", "15/06/2026", "Work");

        manager.sortByDueDate(true);

        List<Task> tasks = manager.getTasks();
        assertEquals("01/01/2026", tasks.get(0).getDueDate());
        assertEquals("15/06/2026", tasks.get(1).getDueDate());
        assertEquals("31/12/2026", tasks.get(2).getDueDate());
    }

    @Test
    void sortByDueDate_descending_ordersLatestFirst() {
        manager.addTask("Late Task",  "High",   "31/12/2026", "Work");
        manager.addTask("Early Task", "Low",    "01/01/2026", "Work");
        manager.addTask("Mid Task",   "Medium", "15/06/2026", "Work");

        manager.sortByDueDate(false);

        List<Task> tasks = manager.getTasks();
        assertEquals("31/12/2026", tasks.get(0).getDueDate());
        assertEquals("15/06/2026", tasks.get(1).getDueDate());
        assertEquals("01/01/2026", tasks.get(2).getDueDate());
    }
    @Test
    void calculateProgress_noTasks_returnsZero() {
        assertEquals(0.0, manager.calculateProgress(), 0.001);
    }

    @Test
    void calculateProgress_oneOfThreeCompleted_returnsOneThird() {
        manager.addTask("Task A", "High",   "01/07/2026", "Work");
        manager.addTask("Task B", "Medium", "02/07/2026", "Work");
        manager.addTask("Task C", "Low",    "03/07/2026", "Work");
        manager.updateTask(manager.getTasks().get(0).getId(), Task.COMPLETED);

        double progress = manager.calculateProgress();

        assertEquals(1.0 / 3.0, progress, 0.001, "One of three tasks completed = ~33%");
    }

    @Test
    void persistence_savedTasksReloadedCorrectly(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) {
        manager.addTask("Task A", "High", "01/07/2026", "Work");
        manager.addTask("Task B", "Low",  "02/07/2026", "Study");

        String file = tempDir.resolve("tasks.txt").toString();
        FileHandler.saveTasks(manager.getTasks(), file);

        TaskManager loaded = new TaskManager();
        FileHandler.loadTasks(loaded, file);

        assertEquals(2, loaded.getTasks().size(), "Both tasks must survive a save/load cycle");
        assertEquals("Task A", loaded.getTasks().get(0).getTitle());
        assertEquals("Task B", loaded.getTasks().get(1).getTitle());
        assertEquals("High",   loaded.getTasks().get(0).getPriority());
        assertEquals("Low",    loaded.getTasks().get(1).getPriority());
    }

    @Test
    void combinedFilter_statusAndPriority_onlyMatchingBothConditionsReturned() {
        manager.addTask("Task A", "High",   "01/07/2026", "Work");
        manager.addTask("Task B", "High",   "02/07/2026", "Study");
        manager.addTask("Task C", "Medium", "03/07/2026", "Work");
        manager.updateTask(manager.getTasks().get(0).getId(), Task.IN_PROGRESS);
        manager.updateTask(manager.getTasks().get(1).getId(), Task.IN_PROGRESS);

        List<Task> results = manager.filterTasks("", Task.IN_PROGRESS, "High");

        assertEquals(2, results.size(), "Two tasks are both In-Progress and High priority");
        assertTrue(results.stream().allMatch(t ->
                t.getStatus().equals(Task.IN_PROGRESS) && t.getPriority().equals("High")));
    }

    @Test
    void combinedFilterAndSearch_respectsAllThreeConditions() {
        manager.addTask("Morning Lecture", "High",   "01/07/2026", "Study");
        manager.addTask("Evening Lecture", "High",   "02/07/2026", "Study");
        manager.addTask("Football",        "High",   "03/07/2026", "Other");
        manager.addTask("Morning Lecture", "Medium", "04/07/2026", "Study");
        manager.updateTask(manager.getTasks().get(0).getId(), Task.IN_PROGRESS);
        manager.updateTask(manager.getTasks().get(1).getId(), Task.IN_PROGRESS);

        List<Task> results = manager.filterTasks("lecture", Task.IN_PROGRESS, "High");

        assertEquals(2, results.size(),
                "Only tasks matching keyword 'lecture', status In-Progress, and priority High");
        assertTrue(results.stream().allMatch(t ->
                t.getTitle().toLowerCase().contains("lecture")
                && t.getStatus().equals(Task.IN_PROGRESS)
                && t.getPriority().equals("High")));
    }
}
