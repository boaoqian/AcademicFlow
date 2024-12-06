package org.qba.academicflow;

import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.qba.backend.paper.Paper;

public class makeView {
    public static VBox createListElement(Paper paper) {
        VBox vbox = new VBox();
        vbox.setPrefHeight(200.0);
        vbox.setPrefWidth(100.0);
        vbox.getStyleClass().add("list-elm");

        Label titleLabel = new Label(paper.getTitle());
        titleLabel.setPrefHeight(20.0);
        titleLabel.setPrefWidth(1795.0);
        titleLabel.setFont(Font.font("Noto Sans CJK SC Medium", 26.0));
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        Label authorLabel = new Label(paper.getAuthor() + " - " + paper.getYear() + " - cited:" + paper.getCited_count());
        authorLabel.setPrefHeight(9.0);
        authorLabel.setPrefWidth(1070.0);
        authorLabel.setFont(Font.font("Noto Sans CJK SC Light", 17.0));
        authorLabel.setMaxWidth(Double.MAX_VALUE);
        Label thirdLabel = new Label(paper.getAbstract());
        thirdLabel.setPrefHeight(9.0);
        thirdLabel.setPrefWidth(1070.0);
        thirdLabel.setFont(Font.font("Noto Sans Mono CJK SC", 20.0));
        thirdLabel.setMaxWidth(Double.MAX_VALUE);
        // 创建按钮容器
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPrefHeight(68.0);
        buttonBox.setPrefWidth(1076.0);
        buttonBox.setSpacing(20); // 按钮之间的间距
        buttonBox.setPadding(new Insets(10, 20, 10, 20)); // 边距
        buttonBox.setVisible(false);

        // 创建按钮
        Button buildGraphBtn = new Button("Build Knowledge Graph");
        Button getPdfBtn = new Button("Download PDF");
        // 设置按钮填充HBox
        HBox.setHgrow(buildGraphBtn, Priority.ALWAYS);
        HBox.setHgrow(getPdfBtn, Priority.ALWAYS);
        buildGraphBtn.setMaxWidth(Double.MAX_VALUE);
        getPdfBtn.setMaxWidth(Double.MAX_VALUE);
//        // 添加图标（如果需要）
//        buildGraphBtn.setGraphic(new ImageView(new Image("/path/to/graph-icon.png"))); // 添加你的图标
//        getPdfBtn.setGraphic(new ImageView(new Image("/path/to/pdf-icon.png"))); // 添加你的图标
        // 添加样式类
        buildGraphBtn.getStyleClass().add("action-button");
        buildGraphBtn.getStyleClass().add("graph-button");
        buildGraphBtn.setOnMouseClicked(event -> {
            var paperevent = new Server.PaperEvent(Server.PaperEvent.GRAPH_BUILT, paper);
            Event.fireEvent(event.getTarget(), paperevent);
        });
        getPdfBtn.getStyleClass().add("action-button");
        getPdfBtn.getStyleClass().add("pdf-button");
        if (paper.getPdf_url() != null && !paper.getPdf_url().isEmpty()) {
            getPdfBtn.setOnMouseClicked(event -> {
                var paperevent = new Server.PaperEvent(Server.PaperEvent.GETPDF, paper);
                Event.fireEvent(event.getTarget(), paperevent);
            });
        } else {
            getPdfBtn.setDisable(true);
        }
        buttonBox.getChildren().addAll(buildGraphBtn, getPdfBtn);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), buttonBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), buttonBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        //动画
        vbox.setOnMouseClicked(e -> {
            if(!buttonBox.isVisible()){
                buttonBox.setVisible(true);
                buttonBox.setManaged(true);
                fadeIn.play();
            }
        });

        vbox.setOnMouseExited(e -> {
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                buttonBox.setVisible(false);
                buttonBox.setManaged(false);
            });
        });

        vbox.getChildren().addAll(titleLabel, authorLabel, thirdLabel, buttonBox);
        return vbox;
    }

}
