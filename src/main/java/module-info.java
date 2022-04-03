module io.github.shivun_wits.gis_viewer {
    requires javafx.controls;
    requires javafx.fxml;

    opens io.github.shivun_wits.gis_viewer to javafx.fxml;
    exports io.github.shivun_wits.gis_viewer;
}
