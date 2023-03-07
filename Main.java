package sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class Main extends Application {

    private TableView<Data> table = new TableView<Data>();
    private final ObservableList<Data> data =
            FXCollections.observableArrayList(
                    new Data("Jacob", "Smith", "jacob.smith@example.com"),
                    new Data("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Data("Ethan", "Williams", "ethan.williams@example.com"),
                    new Data("Emma", "Jones", "emma.jones@example.com"),
                    new Data("Michael", "Brown", "michael.brown@example.com"));
    final HBox hb = new HBox();
    private static final String PATH = "/Users/kamil/IdeaProjects/PasswordManager/PasswordManager.iml";
    private static final String PATH_DATA = "/Users/kamil/IdeaProjects/PasswordManager/PasswordManager.iml";
    public List<String> hashes = readHashes(PATH);

    @Override
    public void start(Stage primaryStage) {
        try {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            primaryStage.setTitle("Password Manager");

            Text username = new Text("Username:");
            username.setFont(Font.font("Arial Nova Light", 20));
            TextField usernameInput = new TextField();

            Text password = new Text("Password:");
            password.setFont(Font.font("Arial Nova Light", 20));
            PasswordField passwordInput = new PasswordField();

            Button login = new Button("Login");
            login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if(usernameInput.getText().length() < 1 || passwordInput.getText().length() < 1) {
                        return;
                    }
                    hashes = readHashes(PATH);
                    for(int i = 0 ; i < hashes.size(); i+= 2) {
                        try {
                            if (hashes.get(i).replaceAll("\n","").equals(toHexString(getSHA(usernameInput.getText()))) && hashes.get(i+1).replaceAll("\n","").equals(toHexString(getSHA(passwordInput.getText())))) {
                                login(primaryStage);
                                return;
                            }
                        } catch (NoSuchAlgorithmException e1) {
                            e1.printStackTrace();
                        }
                    }
                    alert.setTitle("Error");
                    alert.setHeaderText("Incorrect informations");
                    alert.setContentText("The username or password you're trying to input, is incorrect.");
                    alert.showAndWait();
                }
            });

            Button signUp = new Button("Sign Up");
            signUp.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    if(usernameInput.getText().length() < 1) {
                        return;
                    }
                    boolean registration_flag = true;
                    hashes = readHashes(PATH);
                    for(int i = 0 ; i < hashes.size(); i+= 2) {
                        try {
                            if (hashes.get(i).replace("\n","").equals(toHexString(getSHA(usernameInput.getText())))) {
                                registration_flag = false;
                                alert.setTitle("Error");
                                alert.setHeaderText("Username already in use!");
                                alert.setContentText("The username you're trying to input, is already in use.");
                                alert.showAndWait();
                            }
                        } catch (NoSuchAlgorithmException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if(passwordInput.getText().length() < 1) {
                        return;
                    }
                    if(registration_flag) {
                        try {
                            appendHash(usernameInput.getText());
                            appendHash(passwordInput.getText());
                        } catch (NoSuchAlgorithmException e1) {
                            e1.printStackTrace();
                        }
                        login(primaryStage);
                    }
                }
            });

            GridPane gridPane = new GridPane();
            gridPane.setMinSize(400, 200);
            gridPane.setPadding(new Insets(10, 10, 10, 10));

            gridPane.setVgap(5);
            gridPane.setHgap(5);
            gridPane.setAlignment(Pos.CENTER);
            gridPane.add(username, 0, 0);
            gridPane.add(usernameInput, 1, 0);
            gridPane.add(password, 0, 1);
            gridPane.add(passwordInput, 1, 1);
            gridPane.add(login, 0, 2);
            gridPane.add(signUp, 1, 2);

            Scene scene = new Scene(gridPane);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    public void login(Stage primaryStage) {
        primaryStage.hide();
        List<String> encrypted_data;

        File file = new File(PATH_DATA);
        if (file.length() == 0) {
            encrypted_data = new ArrayList<String>();
        } else {
            encrypted_data = readHashes(PATH_DATA);
        }
        Stage newStage = new Stage();
        Scene scene = new Scene(new Group());
        newStage.setTitle("Password Manager");
        newStage.setWidth(450);
        newStage.setHeight(550);

        final Label label = new Label("Password Manager");
        label.setFont(new Font("Arial Nova Light", 20));

        table.setEditable(true);
        Callback<TableColumn, TableCell> cellFactory =
                new Callback<TableColumn, TableCell>() {
                    public TableCell call(TableColumn p) {
                        return new EditingCell();
                    }
                };

        TableColumn usernameCol = new TableColumn("Username");
        usernameCol.setMinWidth(100);
        usernameCol.setCellValueFactory(
                new PropertyValueFactory<Data, String>("username"));
        usernameCol.setCellFactory(cellFactory);
        usernameCol.setOnEditCommit(
                new EventHandler<CellEditEvent<Data, String>>() {
                    @Override
                    public void handle(CellEditEvent<Data, String> t) {
                        ((Data) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setUsername(t.getNewValue());
                    }
                }
        );


        TableColumn passwordCol = new TableColumn("Password");
        passwordCol.setMinWidth(100);
        passwordCol.setCellValueFactory(
                new PropertyValueFactory<Data, String>("password"));
        passwordCol.setCellFactory(cellFactory);
        passwordCol.setOnEditCommit(
                new EventHandler<CellEditEvent<Data, String>>() {
                    @Override
                    public void handle(CellEditEvent<Data, String> t) {
                        ((Data) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setPassword(t.getNewValue());
                    }
                }
        );

        TableColumn descriptionCol = new TableColumn("Description");
        descriptionCol.setMinWidth(200);
        descriptionCol.setCellValueFactory(
                new PropertyValueFactory<Data, String>("description"));
        descriptionCol.setCellFactory(cellFactory);
        descriptionCol.setOnEditCommit(
                new EventHandler<CellEditEvent<Data, String>>() {
                    @Override
                    public void handle(CellEditEvent<Data, String> t) {
                        ((Data) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setDescription(t.getNewValue());
                    }
                }
        );

        table.setItems(data);
        table.getColumns().addAll(usernameCol, passwordCol, descriptionCol);

        final TextField addUsername = new TextField();
        addUsername.setPromptText("Username");
        addUsername.setMaxWidth(usernameCol.getPrefWidth());
        final TextField addPassword = new TextField();
        addPassword.setMaxWidth(passwordCol.getPrefWidth());
        addPassword.setPromptText("Password");
        final TextField addDescription = new TextField();
        addDescription.setMaxWidth(descriptionCol.getPrefWidth());
        addDescription.setPromptText("Description");

        final Button addButton = new Button("+");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                data.add(new Data(
                        addUsername.getText(),
                        addPassword.getText(),
                        addDescription.getText()));
                addUsername.clear();
                addPassword.clear();
                addDescription.clear();
            }
        });

        hb.getChildren().addAll(addUsername, addPassword, addDescription, addButton);
        hb.setSpacing(3);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, hb);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        newStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                primaryStage.show();
            }
        });
        newStage.setScene(scene);
        newStage.show();
    }

    public static class Data {

        private final SimpleStringProperty username;
        private final SimpleStringProperty password;
        private final SimpleStringProperty description;

        private Data(String user, String pass, String desc) {
            this.username = new SimpleStringProperty(user);
            this.password = new SimpleStringProperty(pass);
            this.description = new SimpleStringProperty(desc);
        }

        public String getUsername() {
            return username.get();
        }

        public void setUsername(String user) {
            username.set(user);
        }

        public String getPassword() {
            return password.get();
        }

        public void setPassword(String pass) {
            password.set(pass);
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String desc) {
            description.set(desc);
        }
    }

    class EditingCell extends TableCell<Data, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0,
                                    Boolean arg1, Boolean arg2) {
                    if (!arg2) {
                        commitEdit(textField.getText());
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    // Funkcje zapisujace z i od pliku hashes.txt
    public List<String> readHashes(String path) {
        List<String> hashes = new ArrayList<>();
        try {
            File hashesFile = new File(path);
            Scanner reader = new Scanner(hashesFile);
            while (reader.hasNextLine()) {
                hashes.add(reader.nextLine());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return hashes;
    }

    private void appendHash(String str) throws NoSuchAlgorithmException {
        str = toHexString(getSHA(str));
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(PATH, true));
            File file = new File(PATH);
            if (file.length() == 0)
                out.write(str);
            else
                out.write("\n" + str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

    // Funkcje hashujace
    public static byte[] getSHA(String input) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
