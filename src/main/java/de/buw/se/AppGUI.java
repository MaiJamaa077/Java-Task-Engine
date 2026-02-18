package de.buw.se;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.*;

public class AppGUI extends Application {

    private static final String DATA_FILE = "data/tasks.txt";
    private TaskManager manager = new TaskManager();
    private ObservableList<Task> taskList = FXCollections.observableArrayList();
    private TableView<Task> table = new TableView<>();
    private TextField searchField = new TextField();
    private ProgressBar progressBar = new ProgressBar();
    private Label progressPercentLabel = new Label("0%");
    private boolean darkMode = false;
    private BorderPane mainRoot;

    // Component refs needed for theme switching
    private HBox topBar;
    private HBox statsBar;
    private HBox filterBar;
    private VBox centerBox;
    private Label appTitle;
    private Label progressTitle;
    private Label totalLbl;
    private Label progressLbl;
    private Label doneLbl;
    private Label highLbl;
    private Label filterLbl;
    private Label priorityLbl;
    private Label sortLbl;
    private ComboBox<String> statusFilterRef;
    private ComboBox<String> priorityFilterRef;
    private ComboBox<String> sortBoxRef;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        java.io.File dir = new java.io.File("data");
        if (!dir.exists()) dir.mkdirs();
        FileHandler.loadTasks(manager, DATA_FILE);
        taskList.addAll(manager.getTasks());

        stage.setTitle("Smart Task Manager");
        stage.setWidth(900);
        stage.setHeight(620);

        mainRoot = new BorderPane();
        mainRoot.setStyle("-fx-background-color: #f4f4f0;");

        // ── Top bar ──────────────────────────────────────────────
        topBar = new HBox(12);
        progressBar.setPrefWidth(220);
        progressBar.setProgress(0);
        progressPercentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        topBar.setPadding(new Insets(16, 20, 12, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0ddd5; -fx-border-width: 0 0 1 0;");

        appTitle = new Label("Smart Task Manager");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        appTitle.setTextFill(Color.web("#1a1a1a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Search by title, category or priority…");
        searchField.setPrefWidth(260);
        searchField.setStyle(fieldStyle());
        searchField.textProperty().addListener((o, ov, nv) -> applyFilter());

        Button darkBtn = styledButton("🌙", "#333333", "#ffffff");
        darkBtn.setOnAction(e -> {
            darkMode = !darkMode;
            applyTheme(darkMode);
        });

        Button addBtn = styledButton("+ Add Task", "#185FA5", "#ffffff");
        addBtn.setOnAction(e -> showTaskDialog(null));

        topBar.getChildren().addAll(appTitle, spacer, searchField, darkBtn, addBtn);

        // ── Stats bar ────────────────────────────────────────────
        statsBar = new HBox(12);
        statsBar.setPadding(new Insets(12, 20, 12, 20));
        statsBar.setStyle("-fx-background-color: #f4f4f0;");

        totalLbl    = statCard("Total",        "0", "#1a1a1a");
        progressLbl = statCard("In Progress",  "0", "#185FA5");
        doneLbl     = statCard("Completed",    "0", "#3B6D11");
        highLbl     = statCard("High Priority","0", "#A32D2D");

        progressTitle = new Label("Completion");
        progressTitle.setStyle("-fx-font-weight: bold;");
        VBox progressBox = new VBox(5, progressTitle, progressBar, progressPercentLabel);

        statsBar.getChildren().addAll(totalLbl, progressLbl, doneLbl, highLbl, progressBox);

        // ── Filter row ───────────────────────────────────────────
        filterBar = new HBox(10);
        filterBar.setPadding(new Insets(0, 20, 12, 20));
        filterBar.setAlignment(Pos.CENTER_LEFT);

        statusFilterRef = new ComboBox<>();
        statusFilterRef.getItems().addAll("All Statuses", Task.NOT_STARTED, Task.IN_PROGRESS, Task.COMPLETED, Task.ARCHIVED);
        statusFilterRef.setValue("All Statuses");
        statusFilterRef.setStyle(fieldStyle());
        statusFilterRef.setOnAction(e -> applyFilter());

        priorityFilterRef = new ComboBox<>();
        priorityFilterRef.getItems().addAll("All Priorities", "High", "Medium", "Low");
        priorityFilterRef.setValue("All Priorities");
        priorityFilterRef.setStyle(fieldStyle());
        priorityFilterRef.setOnAction(e -> applyFilter());

        sortBoxRef = new ComboBox<>();
        sortBoxRef.getItems().addAll("Default Order", "Priority ↑", "Priority ↓", "Due Date ↑", "Due Date ↓");
        sortBoxRef.setValue("Default Order");
        sortBoxRef.setStyle(fieldStyle());
        sortBoxRef.setOnAction(e -> applySort(sortBoxRef.getValue()));

        filterLbl  = new Label("Filter:");
        priorityLbl = new Label("Priority:");
        sortLbl    = new Label("Sort:");
        filterBar.getChildren().addAll(filterLbl, statusFilterRef, priorityLbl, priorityFilterRef, sortLbl, sortBoxRef);

        VBox topSection = new VBox(0, topBar, statsBar, filterBar);
        mainRoot.setTop(topSection);

        // ── Table ─────────────────────────────────────────────────
        table.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0ddd5;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(taskList);
        table.setPlaceholder(new Label("No tasks yet — click '+ Add Task' to get started."));
        table.getColumns().addAll(
            makeCol("ID",       "id",       60,  false),
            makeCol("Title",    "title",    200, true),
            makeCol("Priority", "priority", 90,  false),
            makeCol("Due Date", "dueDate",  100, false),
            makeCol("Category", "category", 100, false),
            makeCol("Status",   "status",   120, false),
            actionsColumn()
        );

        centerBox = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        centerBox.setPadding(new Insets(0, 20, 20, 20));
        mainRoot.setCenter(centerBox);

        // ── Refresh stats whenever list changes ───────────────────
        taskList.addListener((ListChangeListener<Task>) c -> {
            refreshStats();
            updateProgressBar();
        });

        FXCollections.copy(taskList, new java.util.ArrayList<>(taskList));
        refreshStats();
        updateProgressBar();

        stage.setOnCloseRequest(e -> {
            syncManagerFromList();
            FileHandler.saveTasks(manager.getTasks(), DATA_FILE);
        });

        stage.setScene(new Scene(mainRoot));
        stage.show();
    }

    // ── Theme ─────────────────────────────────────────────────────
    private void applyTheme(boolean dark) {
        String rootBg      = dark ? "#1e1e1e" : "#f4f4f0";
        String surfaceBg   = dark ? "#2d2d2d" : "#ffffff";
        String borderColor = dark ? "#3d3d3d" : "#e0ddd5";
        String textPrimary = dark ? "#e8e8e8" : "#1a1a1a";
        String fStyle      = fieldStyleForTheme(dark);

        mainRoot.setStyle("-fx-background-color: " + rootBg + ";");
        topBar.setStyle("-fx-background-color: " + surfaceBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
        statsBar.setStyle("-fx-background-color: " + rootBg + ";");
        centerBox.setStyle("-fx-background-color: " + rootBg + ";");
        table.setStyle("-fx-background-color: " + surfaceBg + "; -fx-border-color: " + borderColor
                + "; -fx-control-inner-background: " + surfaceBg + "; -fx-text-fill: " + textPrimary + ";");

        appTitle.setTextFill(Color.web(textPrimary));
        progressTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textPrimary + ";");
        progressPercentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textPrimary + ";");

        filterLbl.setStyle("-fx-text-fill: " + textPrimary + ";");
        priorityLbl.setStyle("-fx-text-fill: " + textPrimary + ";");
        sortLbl.setStyle("-fx-text-fill: " + textPrimary + ";");

        searchField.setStyle(fStyle);
        applyComboBoxTheme(statusFilterRef,  dark);
        applyComboBoxTheme(priorityFilterRef, dark);
        applyComboBoxTheme(sortBoxRef,        dark);

        refreshStats();
    }

    private String fieldStyleForTheme(boolean dark) {
        if (dark) {
            return "-fx-background-color: #2d2d2d; -fx-border-color: #555555; " +
                   "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10 6 10; " +
                   "-fx-font-size: 13; -fx-text-fill: #e8e8e8;";
        }
        return fieldStyle();
    }

    // ComboBox -fx-text-fill doesn't reach the internal button cell via inline styles,
    // so we must set a custom button cell to make text visible in dark mode.
    private void applyComboBoxTheme(ComboBox<String> cb, boolean dark) {
        cb.setStyle(fieldStyleForTheme(dark));
        String fg = dark ? "#e8e8e8" : "#333333";
        String bg = dark ? "#2d2d2d" : "#ffffff";
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: " + fg + "; -fx-background-color: " + bg + "; -fx-padding: 0 0 0 2;");
            }
        });
    }

    // ── Filter / sort ─────────────────────────────────────────────
    private void applyFilter() {
        String kw       = searchField.getText().toLowerCase().trim();
        String status   = statusFilterRef.getValue();
        String priority = priorityFilterRef.getValue();

        taskList.setAll(manager.getTasks().stream()
            .filter(t -> kw.isEmpty() ||
                t.getTitle().toLowerCase().contains(kw) ||
                t.getCategory().toLowerCase().contains(kw) ||
                t.getPriority().toLowerCase().contains(kw))
            .filter(t -> status.equals("All Statuses")    || t.getStatus().equals(status))
            .filter(t -> priority.equals("All Priorities") || t.getPriority().equals(priority))
            .toList());
    }

    private void applySort(String mode) {
        switch (mode) {
            case "Priority ↑" -> manager.sortByPriority(true);
            case "Priority ↓" -> manager.sortByPriority(false);
            case "Due Date ↑" -> manager.sortByDueDate(true);
            case "Due Date ↓" -> manager.sortByDueDate(false);
            default -> {}
        }
        applyFilter();
    }

    private void syncManagerFromList() {
        manager.getTasks().clear();
        manager.getTasks().addAll(taskList);
    }

    // ── Add / Edit dialog ─────────────────────────────────────────
    private void showTaskDialog(Task existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add New Task" : "Edit Task");
        dialog.setResizable(false);

        String bg      = darkMode ? "#2d2d2d" : "#ffffff";
        String fStyle  = fieldStyleForTheme(darkMode);
        String lblColor = darkMode ? "#aaaaaa" : "#555";

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: " + bg + ";");

        TextField titleF   = new TextField(existing != null ? existing.getTitle()   : "");
        TextField dueDateF = new TextField(existing != null ? existing.getDueDate() : "");
        titleF.setPromptText("Task title");
        dueDateF.setPromptText("dd/mm/yyyy");
        titleF.setStyle(fStyle);
        dueDateF.setStyle(fStyle);

        ComboBox<String> priorityCB = new ComboBox<>();
        priorityCB.getItems().addAll("High", "Medium", "Low");
        priorityCB.setValue(existing != null ? existing.getPriority() : "Medium");
        priorityCB.setMaxWidth(Double.MAX_VALUE);
        applyComboBoxTheme(priorityCB, darkMode);

        ComboBox<String> categoryCB = new ComboBox<>();
        categoryCB.getItems().addAll("Work", "Personal", "Study", "Health", "Finance", "Other");
        categoryCB.setValue(existing != null ? existing.getCategory() : "Work");
        categoryCB.setMaxWidth(Double.MAX_VALUE);
        applyComboBoxTheme(categoryCB, darkMode);

        ComboBox<String> statusCB = new ComboBox<>();
        statusCB.getItems().addAll(Task.NOT_STARTED, Task.IN_PROGRESS, Task.COMPLETED, Task.ARCHIVED);
        statusCB.setValue(existing != null ? existing.getStatus() : Task.NOT_STARTED);
        statusCB.setMaxWidth(Double.MAX_VALUE);
        applyComboBoxTheme(statusCB, darkMode);

        grid.addRow(0, formLabel("Title",    lblColor), titleF);
        grid.addRow(1, formLabel("Priority", lblColor), priorityCB);
        grid.addRow(2, formLabel("Due Date", lblColor), dueDateF);
        grid.addRow(3, formLabel("Category", lblColor), categoryCB);
        grid.addRow(4, formLabel("Status",   lblColor), statusCB);
        GridPane.setHgrow(titleF,     Priority.ALWAYS);
        GridPane.setHgrow(priorityCB, Priority.ALWAYS);
        GridPane.setHgrow(dueDateF,   Priority.ALWAYS);
        GridPane.setHgrow(categoryCB, Priority.ALWAYS);
        GridPane.setHgrow(statusCB,   Priority.ALWAYS);

        Button saveBtn   = styledButton(existing == null ? "Add Task" : "Save Changes", "#185FA5", "#ffffff");
        Button cancelBtn = styledButton("Cancel", darkMode ? "#444444" : "#ffffff", darkMode ? "#e8e8e8" : "#333333");
        cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-border-color: " + (darkMode ? "#666666" : "#cccccc") + "; -fx-border-width: 1;");

        HBox btnRow = new HBox(10, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 20, 20, 20));
        btnRow.setStyle("-fx-background-color: " + bg + ";");

        saveBtn.setOnAction(e -> {
            String t = titleF.getText().trim();
            String d = dueDateF.getText().trim();
            if (t.isEmpty())                           { showAlert("Title is required.");                    return; }
            if (d.isEmpty())                           { showAlert("Due date is required (dd/mm/yyyy).");   return; }
            if (!d.matches("\\d{2}/\\d{2}/\\d{4}"))   { showAlert("Date must be dd/mm/yyyy.");             return; }

            if (existing == null) {
                manager.addTask(t, priorityCB.getValue(), d, categoryCB.getValue());
            } else {
                manager.editTask(existing.getId(), t, priorityCB.getValue(), d, categoryCB.getValue(), statusCB.getValue());
            }
            applyFilter();
            dialog.close();
        });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox root = new VBox(grid, btnRow);
        root.setStyle("-fx-background-color: " + bg + ";");
        dialog.setScene(new Scene(root, 400, 310));
        dialog.showAndWait();
    }

    // ── Actions column ────────────────────────────────────────────
    private TableColumn<Task, Void> actionsColumn() {
        TableColumn<Task, Void> col = new TableColumn<>("Actions");
        col.setPrefWidth(140);
        col.setSortable(false);
        col.setCellFactory(c -> new TableCell<>() {
            private final Button editBtn   = styledButton("Edit",   "#185FA5", "#ffffff");
            private final Button statusBtn = styledButton("Status", "#3B6D11", "#ffffff");
            private final Button delBtn    = styledButton("Delete", "#E24B4A", "#ffffff");
            private final HBox box = new HBox(4, editBtn, statusBtn, delBtn);
            {
                box.setAlignment(Pos.CENTER);
                editBtn.setStyle(smallBtnStyle("#185FA5", "#ffffff"));
                statusBtn.setStyle(smallBtnStyle("#3B6D11", "#ffffff"));
                delBtn.setStyle(smallBtnStyle("#E24B4A", "#ffffff"));

                editBtn.setOnAction(e -> showTaskDialog(getTableView().getItems().get(getIndex())));
                statusBtn.setOnAction(e -> showStatusDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete \"" + t.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            manager.deleteTask(t.getId());
                            applyFilter();
                        }
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
        return col;
    }

    // ── Quick status dialog ───────────────────────────────────────
    private void showStatusDialog(Task task) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Update Status");
        dialog.setResizable(false);

        String bg        = darkMode ? "#2d2d2d" : "#ffffff";
        String textColor = darkMode ? "#cccccc" : "#444";

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + bg + ";");

        Label heading = new Label("Choose new status for:\n\"" + task.getTitle() + "\"");
        heading.setWrapText(true);
        heading.setStyle("-fx-font-size: 13; -fx-text-fill: " + textColor + ";");
        box.getChildren().add(heading);

        ToggleGroup grp = new ToggleGroup();
        for (String s : new String[]{Task.NOT_STARTED, Task.IN_PROGRESS, Task.COMPLETED, Task.ARCHIVED}) {
            RadioButton rb = new RadioButton(s);
            rb.setToggleGroup(grp);
            rb.setSelected(s.equals(task.getStatus()));
            rb.setStyle("-fx-font-size: 13; -fx-text-fill: " + textColor + ";");
            box.getChildren().add(rb);
        }

        Button saveBtn   = styledButton("Update", "#185FA5", "#ffffff");
        Button cancelBtn = styledButton("Cancel", darkMode ? "#444444" : "#f0f0f0", darkMode ? "#e8e8e8" : "#333333");
        HBox btnRow = new HBox(10, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(8, 0, 0, 0));
        box.getChildren().add(btnRow);

        saveBtn.setOnAction(e -> {
            RadioButton sel = (RadioButton) grp.getSelectedToggle();
            if (sel != null) {
                manager.updateTask(task.getId(), sel.getText());
                applyFilter();
            }
            dialog.close();
        });
        cancelBtn.setOnAction(e -> dialog.close());

        dialog.setScene(new Scene(box, 320, 240));
        dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private TableColumn<Task, ?> makeCol(String name, String prop, int width, boolean wrap) {
        TableColumn<Task, String> col = new TableColumn<>(name);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        if (wrap) col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                setWrapText(true);
            }
        });
        return col;
    }

    private Label statCard(String label, String value, String color) {
        String bg     = darkMode ? "#2d2d2d" : "#ffffff";
        String border = darkMode ? "#4d4d4d" : "#e0ddd5";
        Label lbl = new Label(value + "  " + label);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-padding: 10 16 10 16; " +
            "-fx-background-radius: 8; -fx-border-color: " + border + "; -fx-border-radius: 8; " +
            "-fx-font-size: 13; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        return lbl;
    }

    private void updateStatCard(Label lbl, String label, String value, String color) {
        String bg     = darkMode ? "#2d2d2d" : "#ffffff";
        String border = darkMode ? "#4d4d4d" : "#e0ddd5";
        lbl.setText(value + "  " + label);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-padding: 10 16 10 16; " +
            "-fx-background-radius: 8; -fx-border-color: " + border + "; -fx-border-radius: 8; " +
            "-fx-font-size: 13; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void refreshStats() {
        long t = taskList.size();
        long p = taskList.stream().filter(x -> x.getStatus().equals(Task.IN_PROGRESS)).count();
        long d = taskList.stream().filter(x -> x.getStatus().equals(Task.COMPLETED)).count();
        long h = taskList.stream().filter(x -> x.getPriority().equals("High") &&
                 !x.getStatus().equals(Task.COMPLETED) && !x.getStatus().equals(Task.ARCHIVED)).count();

        String cTotal = darkMode ? "#e8e8e8" : "#1a1a1a";
        String cProg  = darkMode ? "#5aacdf" : "#185FA5";
        String cDone  = darkMode ? "#6ab830" : "#3B6D11";
        String cHigh  = darkMode ? "#e05050" : "#A32D2D";

        updateStatCard(totalLbl,    "Total",         String.valueOf(t), cTotal);
        updateStatCard(progressLbl, "In Progress",   String.valueOf(p), cProg);
        updateStatCard(doneLbl,     "Completed",     String.valueOf(d), cDone);
        updateStatCard(highLbl,     "High Priority", String.valueOf(h), cHigh);
    }

    private Button styledButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
            "-fx-background-radius: 8; -fx-padding: 7 16 7 16; -fx-font-size: 13; -fx-cursor: hand;");
        return btn;
    }

    private String smallBtnStyle(String bg, String fg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
            "-fx-background-radius: 6; -fx-padding: 4 8 4 8; -fx-font-size: 11; -fx-cursor: hand;";
    }

    private Label formLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        l.setMinWidth(70);
        return l;
    }

    private String fieldStyle() {
        return "-fx-background-color: #ffffff; -fx-border-color: #cccccc; " +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10 6 10; -fx-font-size: 13;";
    }

    private void updateProgressBar() {
        long total     = taskList.size();
        long completed = taskList.stream().filter(t -> t.getStatus().equals(Task.COMPLETED)).count();

        if (total == 0) {
            progressBar.setProgress(0);
            progressPercentLabel.setText("0%");
            return;
        }
        double progress = (double) completed / total;
        progressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.0f%%", progress * 100));
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
