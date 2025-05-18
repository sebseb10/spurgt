module com.example.osmparsing {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires javafx.graphics;
    requires jdk.jdi;

    opens com.example.osmparsing to javafx.fxml;
    exports com.example.osmparsing;
    exports com.example.osmparsing.wayEnums;
    opens com.example.osmparsing.wayEnums to javafx.fxml;
    exports com.example.osmparsing.MVC;
    opens com.example.osmparsing.MVC to javafx.fxml;
    exports com.example.osmparsing.Routing.RoutingAlgorithms;
    opens com.example.osmparsing.Routing.RoutingAlgorithms to javafx.fxml;
}