package org.qba.academicflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.qba.RSGraph.RelationshipGraph;

public class MainGraph extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainGraph.class.getResource("graphpanel.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        scene.setUserAgentStylesheet(MainGraph.class.getResource("smartgraph.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Graph");
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
