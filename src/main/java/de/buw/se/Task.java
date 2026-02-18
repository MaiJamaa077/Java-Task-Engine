package de.buw.se;
public class Task {
    public static final String NOT_STARTED = "Not Started";
    public static final String IN_PROGRESS = "In Progress";
    public static final String COMPLETED = "Completed";
    public static final String ARCHIVED = "Archived";

    private int id;
    private String title;
    private String priority;
    private String dueDate;
    private String category;
    private String status;

    public Task(int id, String title, String priority,
                String dueDate, String category, String status) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.category = category;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%d | %-20s | %-8s | %-10s | %-10s | %s", 
                id, title, priority, dueDate, category, status);
    }
}
