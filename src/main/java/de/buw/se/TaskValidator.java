package de.buw.se;

public class TaskValidator {

    public static boolean isTitleValid(String title) {
        return title != null && !title.trim().isEmpty();
    }

    public static boolean isDueDatePresent(String dueDate) {
        return dueDate != null && !dueDate.trim().isEmpty();
    }

    public static boolean isValidDateFormat(String dueDate) {
        return dueDate != null && dueDate.matches("\\d{2}/\\d{2}/\\d{4}");
    }
}
