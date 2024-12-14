module com.lsb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media; // Add this if you're using javafx.media
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;


    exports com.lsb;
    opens com.lsb to javafx.fxml;
}