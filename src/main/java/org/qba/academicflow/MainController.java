package org.qba.academicflow;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.util.Random;

import static org.qba.academicflow.Server.log;

public class MainController {
    public TextField searchField;
    public Button searchbt;
    public Button randombt;
    public ImageView logoImage;
    public InfoModel userdata = new InfoModel();
    public AnchorPane root;

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
}