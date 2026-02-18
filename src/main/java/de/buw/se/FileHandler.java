package de.buw.se;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import org.apache.commons.csv.*;

public class FileHandler {

    private static final String[] HEADERS = {"id", "title", "priority", "dueDate", "category", "status"};

    public static void saveTasks(ArrayList<Task> tasks, String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
             CSVPrinter printer = new CSVPrinter(writer,
                 CSVFormat.DEFAULT.builder().setHeader(HEADERS).build())) {
            for (Task task : tasks) {
                printer.printRecord(
                    task.getId(), task.getTitle(), task.getPriority(),
                    task.getDueDate(), task.getCategory(), task.getStatus()
                );
            }
            System.out.println("Tasks saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    public static void loadTasks(TaskManager manager, String fileName) {
        File file = new File(fileName);
        if (!file.exists()) return;

        try (Reader reader = Files.newBufferedReader(Paths.get(fileName));
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            for (CSVRecord record : parser) {
                try {
                    int id          = Integer.parseInt(record.get(0).trim());
                    String title    = record.get(1);
                    String priority = record.get(2);
                    String dueDate  = record.get(3);
                    String category = record.get(4);
                    String status   = record.get(5);
                    manager.addExistingTask(new Task(id, title, priority, dueDate, category, status));
                } catch (NumberFormatException e) {
                    // skip header row or malformed record
                }
            }
            System.out.println("Tasks loaded successfully!");
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }
}
