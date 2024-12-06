package org.qba.academicflow;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class MainApplication extends Application {
    private static HostServices hostServices;
    private Server server = Server.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        hostServices = getHostServices();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        scene.setUserAgentStylesheet(String.valueOf(MainApplication.class.getResource("Main.css")));
        stage.setTitle("AcademicFlow");
        stage.setScene(scene);
        stage.show();
    }
    public static HostServices getAppHostServices() {
        return hostServices;
    }
    @Override
    public void stop() {
        Platform.exit();
        Server.log("close");
        server.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}