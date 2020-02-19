package sample;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sample.connection.DbConn;
import sample.datamodel.TodoItem;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField item;

    @FXML
    private TextArea details;

    @FXML
    private DatePicker dueDate;

    private int id=0;

    public TodoItem processResults(){
        String todoItemTitle = item.getText().trim();
        String itemDescription = details.getText().trim();
        LocalDate date = dueDate.getValue();
        TodoItem todoItem = new TodoItem(todoItemTitle, itemDescription, date);
        DbConn.getInstance().insertData(todoItem);
        return todoItem;
    }

    public void loadTodoItem(TodoItem todoItem){
        id = todoItem.getId();
        item.setText(todoItem.getDescription());
        details.setText(todoItem.getDetails());
        dueDate.setValue(todoItem.getDeadline());
    }

    public void updatedTodoItem(){
        String itemTitle = item.getText().trim();
        String itemDetails = details.getText().trim();
        LocalDate date = dueDate.getValue();
        DbConn.getInstance().editItem(id, itemTitle, itemDetails, date);
    }

}
