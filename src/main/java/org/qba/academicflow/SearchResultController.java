package org.qba.academicflow;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.qba.backend.api.GoogleAPI;
import org.qba.backend.paper.Paper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.qba.academicflow.Server.log;

public class SearchResultController implements Initializable {
    public Button searchButton;
    public Label searching_label;
    public TextField searchField;
    public VBox list_root;
    public AnchorPane root;
    public ScrollPane result_pane;
    public Label failedlabel;

    private InfoModel userdata;
    private GoogleAPI api = Server.getInstance().getApi();
    private ExecutorService executor = Server.getInstance().getExecutor();
    private HostServices hostServices;
    private Future now_search;
    public void setDataModel(InfoModel userdata) {
        this.userdata = userdata;
    }
    public void performSearch(String query) {
        try {

            now_search = executor.submit(() -> {
                try {
                    Server.log("search for "+query);

                    // UI 更新需要在 JavaFX 线程中执行
                    Platform.runLater(() -> {
                        list_root.setVisible(false);
                        result_pane.setVisible(false);
                        failedlabel.setVisible(false);
                        failedlabel.setManaged(false);
                        if (!list_root.getChildren().isEmpty()) {
                            list_root.getChildren().clear();
                        }
                        searching_label.setVisible(true);
                        searching_label.setManaged(true);
                    });

                    // 在后台线程执行搜索
                    userdata.papers = api.GetByName(query);

                    // 搜索完成后的 UI 更新也需要在 JavaFX 线程中执行
                    Platform.runLater(() -> {
                        searching_label.setVisible(false);
                        searching_label.setManaged(false);
                        list_root.setVisible(true);
                        show_result(userdata.papers);
                        Server.log("finished search");
                    });

                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Server.log("Searching failed");
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void show_result(List<Paper> papers){
        result_pane.setVisible(true);
        var temp = list_root.getChildren();
        var list = papers.stream().map((paper)->{
            var elm = makeView.createListElement(paper);
            return elm;
        }).toList();
        if(list.isEmpty()){
            result_pane.setVisible(false);
            failedlabel.setText("No papers containing \""+userdata.search_name+"\" were found.");
            failedlabel.setVisible(true);
            failedlabel.setManaged(true);
        }
        temp.addAll(list);
    }

    public void handleSearch(ActionEvent actionEvent) {
        if (now_search!=null) {
            now_search.cancel(true);
        }
        userdata.search_name = searchField.getText();
        if (!searchField.getText().isEmpty()) {
            performSearch(searchField.getText());
        }
    }

    public void initData(InfoModel userdata) {
        this.userdata = userdata;
        performSearch(userdata.search_name);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        root.addEventHandler(Server.PaperEvent.GETPDF, this::handleGetPDF);
        root.addEventHandler(Server.PaperEvent.GRAPH_BUILT, this::handleGraphBuilt);

    }

    private void handleGraphBuilt(Server.PaperEvent paperEvent) {
        Server.log("graph built");
        switchScene(paperEvent,paperEvent.getPaper());
    }

    private void handleGetPDF(Server.PaperEvent paperEvent) {
        Server.log("get pdf");
        Paper paper = paperEvent.getPaper();
        if (paper.getPdf_url() != null && !paper.getPdf_url().isEmpty()) {
            Server.log(paper.getPdf_url());
            MainApplication.getAppHostServices().showDocument(paper.getPdf_url());
        }
    }
    public void switchScene(Event event,Paper paper) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("graphpanel.fxml"));
            Parent root = loader.load();
            GraphController newController = loader.getController();
            newController.setSearch_paper(paper);  // 先设置数据模型
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("smartgraph.css").toExternalForm());
            stage.setScene(scene);
            newController.buildGraph();
            stage.show();
        } catch(Exception e) {
            log("failed load searcher");
            e.printStackTrace();
        }
    }
}
