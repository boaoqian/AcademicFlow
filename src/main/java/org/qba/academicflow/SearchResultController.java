package org.qba.academicflow;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
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
import java.util.concurrent.Executors;

public class SearchResultController implements Initializable {
    public Button searchButton;
    public Label searching_label;
    public TextField searchField;
    public VBox list_root;
    public AnchorPane root;

    private InfoModel userdata;
    private GoogleAPI api = Server.getInstance().getApi();
    private ExecutorService executor = Server.getInstance().getExecutor();
    private HostServices hostServices;

    public void setDataModel(InfoModel userdata) {
        this.userdata = userdata;
    }
    public void performSearch(String query) {
        try {
            executor.submit(() -> {
                try {
                    Server.log("search for "+query);

                    // UI 更新需要在 JavaFX 线程中执行
                    Platform.runLater(() -> {
                        list_root.setVisible(false);
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
        var temp = list_root.getChildren();
        var list = papers.stream().map((paper)->{
            var elm = makeView.createListElement(paper);
            return elm;
        }).toList();
        temp.addAll(list);
    }

    public void handleSearch(ActionEvent actionEvent) {
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
    }

    private void handleGetPDF(Server.PaperEvent paperEvent) {
        Server.log("get pdf");
        Paper paper = paperEvent.getPaper();
        if (paper.getPdf_url() != null && !paper.getPdf_url().isEmpty()) {
            Server.log(paper.getPdf_url());
            MainApplication.getAppHostServices().showDocument(paper.getPdf_url());
        }
    }
}
