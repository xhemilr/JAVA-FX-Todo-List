package sample.connection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.datamodel.TodoItem;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DbConn {
    public static final String PROP_CONNSTRING = "connString";
    public static final String PROP_USERNAME = "username";
    public static final String PROP_PASSWORD = "password";
    private static final DbConn instance = new DbConn();

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String connString = PropertiesCache.getInstance().getProperty(PROP_CONNSTRING);
    private static final String username = PropertiesCache.getInstance().getProperty(PROP_USERNAME);
    private static final String password = PropertiesCache.getInstance().getProperty(PROP_PASSWORD);
    private static Connection connection;


    private DbConn(){
    }

    public static DbConn getInstance(){
        return instance;
    }

    public ObservableList<TodoItem> getItems(){
        String query = "Select * from TodoData;";
        ObservableList<TodoItem> todoItems = FXCollections.observableArrayList();
        try {
            connection = DriverManager.getConnection(connString, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("_id");
                String todoItem = rs.getString("todo_item");
                String itemDescription = rs.getString("todo_item_Description");
                LocalDate deadline = rs.getDate("todo_deadline").toLocalDate();
                todoItems.add(new TodoItem(id, todoItem, itemDescription, deadline));
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
            System.out.println("Could not connect to DB");
        }finally {
            try {
                connection.close();
            }catch (SQLException e){
                System.out.println("Connection could not be closed");
            }
        }
        return todoItems;
    }

    public void insertData(TodoItem todoItem) {
        String query = "INSERT INTO TodoData (todo_item, todo_item_description,todo_deadline) VALUES (?,?,?);";
        try{
            connection = DriverManager.getConnection(connString, username,password);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,todoItem.getDescription());
            statement.setString(2, todoItem.getDetails());
            statement.setString(3, todoItem.getDeadline().format(formatter));
            statement.executeUpdate();
        } catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            }catch (SQLException e){
                System.out.println("Connection could not be closed");
            }
        }
    }

    public void removeItem(int id){
        String query = "delete from TodoData where _id=?";
        try{
            connection = DriverManager.getConnection(connString, username, password);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            System.out.println("Could not connect to DB");
        }finally {
            try {
                connection.close();
            }catch (SQLException e){
                System.out.println("Connection could not be closed");
            }
        }
    }

    public void editItem(int id, String title, String details, LocalDate date){
        String query = "UPDATE TodoData SET todo_item = ?, todo_item_description = ?, todo_deadline = ? WHERE _id = ?;";
        try {
            connection = DriverManager.getConnection(connString, username, password);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, details);
            statement.setString(3,date.format(formatter));
            statement.setInt(4, id);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            }catch (SQLException e){
                System.out.println("Could not close the connection");
            }
        }
    }
}
