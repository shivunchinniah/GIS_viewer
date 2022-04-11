module io.github.shivun_wits.gis_viewer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.base;
    requires javafx.swing;


    opens io.github.shivun_wits.gis_viewer to javafx.fxml;
    exports io.github.shivun_wits.gis_viewer;
}
