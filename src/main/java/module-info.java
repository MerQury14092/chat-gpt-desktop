module com.example.gptclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.gptclient to javafx.fxml;
    opens com.example.gptclient.controllers to javafx.fxml;
    exports com.example.gptclient;
}