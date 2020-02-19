package sample;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sample.connection.DbConn;
import sample.datamodel.TodoItem;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea todoTextArea;

    @FXML
    private Label dueDate;

    @FXML
    private MenuItem newItem;

    private MenuItem editItemContext;

    @FXML
    private MenuItem editItem;

    private MenuItem deleteItemContext;

    @FXML
    private MenuItem deleteItem;

    @FXML
    private MenuItem exit;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button btnDB;

    @FXML
    private BorderPane mainBorderPane;

    private Predicate<TodoItem> filter;


    public void initialize(){
        loadTodoItems();
        setContextMenu();
        setEventHandlers();
        customizeListView();
    }

    private void loadTodoItems(){
        FilteredList<TodoItem> filteredList = new FilteredList<>(DbConn.getInstance().getItems(), filter);
        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, Comparator.comparing(TodoItem::getDeadline));
        todoListView.getItems().setAll(sortedList);

    }

    private void setContextMenu(){
        listContextMenu = new ContextMenu();
        deleteItemContext = new MenuItem();
        deleteItemContext.setText("Delete");
        editItemContext = new MenuItem();
        editItemContext.setText("Edit Item");
        listContextMenu.getItems().add(editItemContext);
        listContextMenu.getItems().add(deleteItemContext);
    }

    private void setEventHandlers(){
        deleteItemContext.setOnAction(event -> deleteItem.fire());

        editItemContext.setOnAction(event -> editItem.fire());

        todoListView.getSelectionModel().selectedItemProperty().addListener((observableValue, todoItem, t1) -> {
            if (t1 != null){
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                todoTextArea.setText(item.getDetails());
                dueDate.setText(item.getDeadline().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            }
        });

        todoListView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2){
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                if (item == null){
                    showAddTodoItemDialog();
                }else if(item.getId() > 0){
                    showEditDialog();
                }
            }
        });

        newItem.setOnAction(event -> showAddTodoItemDialog());

        toggleButton.setOnAction(event -> {
            if (toggleButton.isSelected()){
                filter = todoItem -> todoItem.getDeadline().equals(LocalDate.now());
            }else{
                filter = todoItem -> true;
            }
            loadTodoItems();
            todoListView.getSelectionModel().selectFirst();
        });

        btnDB.setOnAction(event -> showDbConnSettings());

        editItem.setOnAction(event -> showEditDialog());

        deleteItem.setOnAction(event -> {
            int id = todoListView.getSelectionModel().getSelectedItem().getId();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Todo Item");
            alert.setHeaderText("Delete Item with ID: " + id);
            alert.setContentText("Are you sure! Pres OK to confirm, or cancel to back out.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                if (id>0){
                    DbConn.getInstance().removeItem(id);
                    loadTodoItems();
                }
            }
            todoListView.getSelectionModel().selectFirst();
        });

        exit.setOnAction(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void customizeListView(){
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean b) {
                        super.updateItem(todoItem, b);
                        if (b) {
                            setText(null);
                        } else {
                            setText(todoItem.getDescription());
                            if (todoItem.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if (todoItem.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.GREEN);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });
    }

    public void showAddTodoItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            System.out.println("Could not load the dialog");
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        TodoItem todoItem = null;
        if (result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            todoItem = controller.processResults();

        }else {
            System.out.println("Cancelled");
        }
        loadTodoItems();
        if (todoItem != null){
            todoListView.getSelectionModel().select(todoItem);
        }
    }

    private void showEditDialog(){
        TodoItem todoItem = todoListView.getSelectionModel().getSelectedItem();
        if (todoItem==null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Todo Item selected");
            alert.setContentText("Please select Todo Item to edit");
            alert.showAndWait();
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit Todo Item");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.out.println("Could'n load dialog");
            e.printStackTrace();
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        DialogController controller = loader.getController();
        controller.loadTodoItem(todoItem);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            controller.updatedTodoItem();
            loadTodoItems();
        }
    }

    private void showDbConnSettings(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("dbconnection.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        }catch (IOException e){
            System.out.println("Couldn't load the dialog");
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        DbConnectionController connectionController = loader.getController();
        connectionController.setValues();
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY){
            connectionController.saveSettings();
        }
    }
}

