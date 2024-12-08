module org.qba.academicflow {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;

    requires org.kordamp.bootstrapfx.core;
    requires org.jsoup;
    requires java.sql;
    requires org.slf4j;
    opens org.qba.academicflow to javafx.fxml;
    exports org.qba.academicflow;
    exports org.qba.backend.api;
    exports org.qba.backend.paper;
    exports org.qba.backend.database;
    exports org.qba.RSGraph;
    opens org.qba.RSGraph to javafx.fxml;
}