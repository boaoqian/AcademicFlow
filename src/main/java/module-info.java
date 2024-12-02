module org.qba.academicflow {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.qba.academicflow to javafx.fxml;
    exports org.qba.academicflow;
}