module com.eswasthya.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.eswasthya.desktop to javafx.fxml, javafx.graphics;
    opens com.eswasthya.desktop.controller to javafx.fxml;
    opens com.eswasthya.desktop.model to com.fasterxml.jackson.databind;

    exports com.eswasthya.desktop;
    exports com.eswasthya.desktop.controller;
    exports com.eswasthya.desktop.model;
    exports com.eswasthya.desktop.api;
}
