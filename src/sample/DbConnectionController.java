package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sample.connection.PropertiesCache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static sample.connection.DbConn.*;

public class DbConnectionController {

    @FXML
    private TextField connString;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button btnTestDbConn;

    public void initialize(){
        btnTestDbConn.setOnAction(event -> testConn());
    }

    public void setValues(){
        connString.setText(PropertiesCache.getInstance().getProperty(PROP_CONNSTRING));
        username.setText(PropertiesCache.getInstance().getProperty(PROP_USERNAME));
        password.setText(PropertiesCache.getInstance().getProperty(PROP_PASSWORD));
    }

    public void saveSettings(){
        String connStr = connString.getText().trim();
        String user = username.getText().trim();
        String pass = password.getText();
        PropertiesCache.getInstance().setProperty(PROP_CONNSTRING, connStr);
        PropertiesCache.getInstance().setProperty(PROP_USERNAME, user);
        PropertiesCache.getInstance().setProperty(PROP_PASSWORD, pass);
        PropertiesCache.getInstance().writeProperty();
    }

    public void testConn(){
        String connectionString = connString.getText().trim();
        String user = username.getText().trim();
        String pass = password.getText().trim();
        try {
            Connection connection = DriverManager.getConnection(connectionString, user,pass);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DB Connection Test");
            alert.setContentText("Connection Successful");
            alert.showAndWait();
        }catch (SQLException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("DB Connection Test");
            alert.setContentText("Connection Unsuccessful");
            alert.showAndWait();
        }

    }
}
