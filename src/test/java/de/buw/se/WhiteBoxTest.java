package de.buw.se;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WhiteBoxTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
    }

    @Test
    void taskConstructor_storesAllFieldsCorrectly() {
        Task t = new Task(42, "Test Task", "High", "15/07/2026", "Work", Task.IN_PROGRESS);
        assertEquals(42,              t.getId());
        assertEquals("Test Task",    t.getTitle());
        assertEquals("High",         t.getPriority());
        assertEquals("15/07/2026",   t.getDueDate());
        assertEquals("Work",         t.getCategory());
        assertEquals(Task.IN_PROGRESS, t.getStatus());
    }

    @Test
    void taskToString_containsAllFieldValues() {
        Task t = new Task(1, "Morning Lecture", "High", "02/06/2026", "Study", Task.NOT_STARTED);
        String result = t.toString();
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Morning Lecture"));
        assertTrue(result.contains("High"));
        assertTrue(result.contains("02/06/2026"));
        assertTrue(result.contains("Study"));
        assertTrue(result.contains(Task.NOT_STARTED));
    }

    @Test
    void addTask_autoIncrementsIdForEachTask() {
        manager.addTask("Task A", "Low", "01/07/2026", "Work");
        manager.addTask("Task B", "Low", "02/07/2026", "Work");
        manager.addTask("Task C", "Low", "03/07/2026", "Work");

        List<Task> tasks = manager.getTasks();
        int id1 = tasks.get(0).getId();
        int id2 = tasks.get(1).getId();
        int id3 = tasks.get(2).getId();

        assertEquals(id1 + 1, id2, "Second ID must be exactly one more than first");
        assertEquals(id2 + 1, id3, "Third ID must be exactly one more than second");
    }

    @Test
    void addTask_defaultStatusIsNotStarted() {
        manager.addTask("New Task", "Medium", "15/07/2026", "Personal");
        assertEquals(Task.NOT_STARTED, manager.getTasks().get(0).getStatus());
    }

    @Test
    void addExistingTask_updatesNextId_whenHigherId() {
        manager.addExistingTask(new Task(100, "Loaded", "Low", "01/01/2026", "Work", Task.NOT_STARTED));

        manager.addTask("Fresh", "Low", "02/01/2026", "Work");
        int freshId = manager.getTasks().get(1).getId();

        assertEquals(101, freshId,
                "nextId must advance to existing-task-id + 1, so fresh task gets id 101");
    }

    @Test
    void addExistingTask_doesNotReduceNextId_whenLowerId() {
        manager.addTask("First", "Low", "01/01/2026", "Work");
        int firstId = manager.getTasks().get(0).getId();

        manager.addExistingTask(new Task(0, "Old", "Low", "01/01/2020", "Work", Task.COMPLETED));

        manager.addTask("Third", "Low", "03/01/2026", "Work");
        int thirdId = manager.getTasks().get(2).getId();

        assertTrue(thirdId > firstId,
                "nextId must not be reduced by a lower-ID existing task");
    }

    @Test
    void deleteTask_nonExistentId_doesNotThrowAndListUnchanged() {
        manager.addTask("Task", "Low", "01/07/2026", "Work");
        assertDoesNotThrow(() -> manager.deleteTask(9999));
        assertEquals(1, manager.getTasks().size());
    }

    @Test
    void deleteTask_existingId_removesOnlyTargetTask() {
        manager.addTask("Task A", "Low",  "01/07/2026", "Work");
        manager.addTask("Task B", "High", "02/07/2026", "Study");
        int idA = manager.getTasks().get(0).getId();

        manager.deleteTask(idA);

        assertEquals(1, manager.getTasks().size());
        assertEquals("Task B", manager.getTasks().get(0).getTitle());
    }

    @Test
    void updateTask_validStatus_updatesTaskStatus() {
        manager.addTask("Task", "Low", "01/07/2026", "Work");
        int id = manager.getTasks().get(0).getId();

        manager.updateTask(id, Task.COMPLETED);

        assertEquals(Task.COMPLETED, manager.getTasks().get(0).getStatus());
    }

    void updateTask_invalidStatus_doesNotChangeStatus() {
        manager.addTask("Task", "Low", "01/07/2026", "Work");
        int id = manager.getTasks().get(0).getId();

        manager.updateTask(id, "InvalidStatus");

        assertEquals(Task.NOT_STARTED, manager.getTasks().get(0).getStatus());
    }

    @Test
    void updateTask_nonExistentId_doesNotThrow() {
        assertDoesNotThrow(() -> manager.updateTask(9999, Task.COMPLETED));
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void editTask_invalidStatus_noFieldsChanged() {
        manager.addTask("Original", "Low", "01/07/2026", "Work");
        int id = manager.getTasks().get(0).getId();

        manager.editTask(id, "Changed", "High", "01/08/2026", "Study", "BOGUS");

        Task t = manager.getTasks().get(0);
        assertEquals("Original",       t.getTitle());
        assertEquals("Low",            t.getPriority());
        assertEquals("01/07/2026",     t.getDueDate());
        assertEquals("Work",           t.getCategory());
        assertEquals(Task.NOT_STARTED, t.getStatus());
    }

    @Test
    void editTask_nonExistentId_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.editTask(9999, "Title", "Low", "01/07/2026", "Work", Task.NOT_STARTED));
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void isValidStatus_caseInsensitive_acceptsAllVariants() {
        assertTrue(manager.isValidStatus("not started"),   "lowercase must be accepted");
        assertTrue(manager.isValidStatus("IN PROGRESS"),   "uppercase must be accepted");
        assertTrue(manager.isValidStatus("Completed"),     "mixed case must be accepted");
        assertTrue(manager.isValidStatus("ARCHIVED"),      "all-caps must be accepted");
        assertFalse(manager.isValidStatus("done"),         "unrecognised value must be rejected");
        assertFalse(manager.isValidStatus("pending"),      "unrecognised value must be rejected");
    }

    @Test
    void isValidStatus_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> manager.isValidStatus(null),
                "Passing null crashes isValidStatus — no null-guard exists in the original code");
    }
}
