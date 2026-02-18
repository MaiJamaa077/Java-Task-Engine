package de.buw.se;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskManager {
    private ArrayList<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public void addTask(String title, String priority,
                        String dueDate, String category) {
        Task task = new Task(
                nextId++,
                title,
                priority,
                dueDate,
                category,
                Task.NOT_STARTED
        );
        tasks.add(task);
        System.out.println("Task added successfully!");
    }

    public void addExistingTask(Task task) {
        tasks.add(task);
        if (task.getId() >= nextId) {
            nextId = task.getId() + 1;
        }
    }

    public void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        System.out.println("\nID | Title                | Priority | Due Date   | Category   | Status");
        System.out.println("-------------------------------------------------------------------------");
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    public void searchTask(String keyword) {
        boolean found = false;
        String lowKeyword = keyword.toLowerCase();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(lowKeyword) ||
                task.getCategory().toLowerCase().contains(lowKeyword) ||
                task.getPriority().toLowerCase().contains(lowKeyword)) {
                System.out.println(task);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching task found.");
        }
    }

    public List<Task> searchTasks(String keyword) {
        String lowKeyword = keyword.toLowerCase();
        List<Task> results = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(lowKeyword) ||
                task.getCategory().toLowerCase().contains(lowKeyword) ||
                task.getPriority().toLowerCase().contains(lowKeyword)) {
                results.add(task);
            }
        }
        return results;
    }

    public List<Task> filterTasks(String keyword, String status, String priority) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        return tasks.stream()
            .filter(t -> kw.isEmpty()
                || t.getTitle().toLowerCase().contains(kw)
                || t.getCategory().toLowerCase().contains(kw)
                || t.getPriority().toLowerCase().contains(kw))
            .filter(t -> status == null || status.equals("All Statuses") || t.getStatus().equals(status))
            .filter(t -> priority == null || priority.equals("All Priorities") || t.getPriority().equals(priority))
            .toList();
    }

    public double calculateProgress() {
        if (tasks.isEmpty()) return 0.0;
        long completed = tasks.stream()
                .filter(t -> t.getStatus().equals(Task.COMPLETED))
                .count();
        return (double) completed / tasks.size();
    }

    public void updateTask(int id, String newStatus) {
        if (!isValidStatus(newStatus)) {
            System.out.println("Invalid status. Allowed statuses: Not Started, In Progress, Completed, Archived");
            return;
        }
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.setStatus(newStatus);
                System.out.println("Task updated successfully!");
                return;
            }
        }
        System.out.println("Task not found.");
    }

    public void editTask(int id, String title, String priority, String dueDate, String category, String status) {
        if (!isValidStatus(status)) {
            System.out.println("Invalid status. Allowed statuses: Not Started, In Progress, Completed, Archived");
            return;
        }
        for (Task task : tasks) {
            if (task.getId() == id) {
                task.setTitle(title);
                task.setPriority(priority);
                task.setDueDate(dueDate);
                task.setCategory(category);
                task.setStatus(status);
                System.out.println("Task updated successfully!");
                return;
            }
        }
        System.out.println("Task not found.");
    }

    public boolean isValidStatus(String status) {
        return status.equalsIgnoreCase(Task.NOT_STARTED) ||
               status.equalsIgnoreCase(Task.IN_PROGRESS) ||
               status.equalsIgnoreCase(Task.COMPLETED) ||
               status.equalsIgnoreCase(Task.ARCHIVED);
    }

    public void deleteTask(int id) {
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.getId() == id) {
                iterator.remove();
                System.out.println("Task deleted successfully!");
                return;
            }
        }
        System.out.println("Task not found.");
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public int getPriorityValue(String priority) {
        switch (priority.toLowerCase()) {
            case "high": return 1;
            case "medium": return 2;
            case "low": return 3;
            default: return 4;
        }
    }

    public void sortByPriority(boolean ascending) {
        tasks.sort((t1, t2) -> {
            int v1 = getPriorityValue(t1.getPriority());
            int v2 = getPriorityValue(t2.getPriority());
            return ascending ? Integer.compare(v1, v2) : Integer.compare(v2, v1);
        });
        System.out.println("Tasks sorted by priority (" + (ascending ? "ascending" : "descending") + ").");
    }

    public void sortByDueDate(boolean ascending) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        tasks.sort((t1, t2) -> {
            try {
                LocalDate d1 = LocalDate.parse(t1.getDueDate(), dtf);
                LocalDate d2 = LocalDate.parse(t2.getDueDate(), dtf);
                int res = d1.compareTo(d2);
                return ascending ? res : -res;
            } catch (Exception e) {
                int res = t1.getDueDate().compareTo(t2.getDueDate());
                return ascending ? res : -res;
            }
        });
        System.out.println("Tasks sorted by due date (" + (ascending ? "ascending" : "descending") + ").");
    }
}
