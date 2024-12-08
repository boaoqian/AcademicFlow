package org.qba.academicflow;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.transform.*;
import javafx.util.Duration;
import org.qba.RSGraph.RelationshipGraph;

public class Graphpanel {
    @FXML
    public Pane root;
    public VBox infoBar;

    private Pane graphPane;

    @FXML
    private void initialize(){
        infoBar.setManaged(false);
        try{
            RelationshipGraph graph = new RelationshipGraph();
            graphPane = graph.getRoot();
            graphPane.setManaged(true);
            root.getChildren().add(graphPane);
            graph.startAnimation();
            AnchorPane.setTopAnchor(graphPane, 0.0);
            AnchorPane.setBottomAnchor(graphPane, 0.0);
            AnchorPane.setLeftAnchor(graphPane, 0.0);
            AnchorPane.setRightAnchor(graphPane, 0.0);
            graph.addRandomNode();
            graph.addNode(new RelationshipGraph.plainVertex(1,"DEWF"));
            graph.addRandomNode();
            Timeline addNodeTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), event -> graph.addRandomNode())
            );
            addNodeTimeline.setCycleCount(20); // 无限循环
            addNodeTimeline.play();
        }catch (Exception e){
            e.printStackTrace();
        }
        // 添加鼠标滚轮缩放事件
        graphPane.setOnScroll(event -> {
            // 获取滚轮滚动的增量
            double delta = event.getDeltaY();
            // 设置缩放系数
            double scaleFactor = 1;
            if (delta > 0){
                scaleFactor = 1.02;
            } else if (delta < 0) {
                scaleFactor = 0.98;
            }

            // 获取当前的缩放变换
            Scale scale = getScaleTransform(graphPane);

            // 计算新的缩放比例
            double newScaleX = scale.getX() * scaleFactor;
            double newScaleY = scale.getY() * scaleFactor;

//            // 限制缩放范围（可选）
            newScaleX = Math.max(1, Math.min(newScaleX, 3.0));
            newScaleY = Math.max(1, Math.min(newScaleY, 3.0));

            // 应用新的缩放
            scale.setX(newScaleX);
            scale.setY(newScaleY);

            // 可选：根据鼠标位置调整缩放中心
            // 这有助于实现以鼠标为中心的缩放
            graphPane.setTranslateX(event.getX() * (1 - newScaleX));
            graphPane.setTranslateY(event.getY() * (1 - newScaleY));
        });
    }

    // 辅助方法：获取或创建缩放变换
    private Scale getScaleTransform(Pane node) {
        // 查找是否已存在缩放变换
        for (Transform transform : node.getTransforms()) {
            if (transform instanceof Scale) {
                return (Scale) transform;
            }
        }

        // 如果不存在，创建新的缩放变换
        Scale scale = new Scale(1.0, 1.0);
        node.getTransforms().add(scale);
        return scale;
    }
}
