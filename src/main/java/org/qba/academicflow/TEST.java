package org.qba.academicflow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.qba.RSGraph.RelationshipGraph;

public class TEST extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        RelationshipGraph relationshipGraph = new RelationshipGraph();
        relationshipGraph.startAnimation();
        Scene s = relationshipGraph.getScene();
        s.setUserAgentStylesheet(MainGraph.class.getResource("smartgraph.css").toExternalForm());
        stage.setScene(s);
        stage.show();
        relationshipGraph.addRoot(new RelationshipGraph.plainVertex(0,"efrerbge"));
        for (int i =0;i<10;i++){
            relationshipGraph.addRandomNode();
        }

    }
}
