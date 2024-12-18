package org.qba.academicflow;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.qba.backend.database.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Random;

import static org.qba.academicflow.Server.log;

public class MainController {
    public TextField searchField;
    public Button searchbt;
    public Button randombt;
    public ImageView logoImage;
    public InfoModel userdata = new InfoModel();
    public ComboBox<Integer> yearComboBox;
    public ImageView settingbt;
    public Button clean_database;
    public AnchorPane mask;
    public VBox settingpane;
    public Button savechange;

    public void setDataModel(InfoModel userdata) {
        this.userdata = userdata;
    }

    public void handleSearch(ActionEvent actionEvent) {
        log("Searching for: "+ searchField.getText());
        userdata.search_name = searchField.getText();
        if (userdata.search_name.isEmpty()) {
            return;
        }
        else {
            switchScene(actionEvent);
        }
    }


    public void handleLucky(ActionEvent actionEvent) {
        Random rand = new Random();
        userdata.search_name = ""+rand.nextInt(10000);
        switchScene(actionEvent);
    }

    public void switchScene(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("searchResult.fxml"));
            Parent root = loader.load();
            SearchResultController newController = loader.getController();
            newController.setDataModel(userdata);  // 先设置数据模型
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("result.css").toExternalForm());
            stage.setScene(scene);
            newController.performSearch(userdata.search_name);
            stage.show();
        } catch(Exception e) {
            log("failed load searcher");
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        settingpane.setManaged(false);
        settingpane.setVisible(false);
        int now_year = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(now_year,now_year-1,now_year-3,-1);
        yearComboBox.getSelectionModel().select(now_year-3);
        yearComboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer integer) {
                if(integer == now_year){
                    return "this year";
                }
                else if(integer == now_year-1){
                   return "last 1 year";
                } else if (integer == now_year-3) {
                    return "last 3 years";
                }else {
                    return "no limit";
                }
            }

            @Override
            public Integer fromString(String s) {
                return 0;
            }
        });
    }

    public void EnterSearch(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            log("Searching for: "+ searchField.getText());
            userdata.search_name = searchField.getText();
            if (userdata.search_name.isEmpty()) {
                return;
            }
            else {
                switchScene(keyEvent);
            }
        }
    }

    public void opensetting(MouseEvent mouseEvent) {
        settingpane.setVisible(true);
        settingpane.setManaged(true);
        mask.setEffect(new GaussianBlur(100));
    }

    public void clean_database(ActionEvent actionEvent) {
        if(Files.exists(Path.of("./PaperData/paperinfo.sqlite"))){
            return;
        }else {
            try{
                Utils.clean();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void savechange(ActionEvent actionEvent) {
        userdata.search_year = yearComboBox.getSelectionModel().getSelectedItem();
        if ((yearComboBox.getSelectionModel().getSelectedItem()!=null))
        {
            userdata.search_year = yearComboBox.getSelectionModel().getSelectedItem();
        }else {
            userdata.search_year = -1;
        }
        mask.setEffect(null);
        settingpane.setVisible(false);
        settingpane.setManaged(false);
    }
}