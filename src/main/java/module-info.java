module com.swe.canvas {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.swe.canvas to javafx.fxml;
    opens com.swe.canvas.gui to javafx.fxml;
    
    opens com.swe.canvas.viewmodel to javafx.fxml;
    
    exports com.swe.canvas;
    exports com.swe.canvas.gui;
    
    exports com.swe.canvas.viewmodel;
}