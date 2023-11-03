package io.github.shivun_wits.gis_viewer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

   


    private static Text message;
    // static ImageView imageView;
    static ImageView smallView;

    static final double WIDTH = 1000;
    static final double HEIGHT = 800;

    static ImageCanvas imageCanvas;

    private EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if(event.getCode() == KeyCode.W){
                imageCanvas.move(1);
            }
            else if(event.getCode() == KeyCode.D){
                imageCanvas.move(2);
            } 
            else if(event.getCode() == KeyCode.S){
                imageCanvas.move(3);
            } 
            else if(event.getCode() == KeyCode.A){
                imageCanvas.move(4);
            } 
            event.consume();
        }
    };



    @Override
    public void start(Stage stage) throws FileNotFoundException {
        stage.setTitle("GIS Viewer");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        
        imageCanvas = new ImageCanvas();
        
        StackPane canvasContainer = new StackPane(imageCanvas.getCanvas());
        canvasContainer.setBorder(new Border(new BorderStroke(
                Paint.valueOf("#000"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(0),
                BorderWidths.DEFAULT)));

        HBox imageBox = new HBox(canvasContainer);
        imageBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(imageBox);
        scene = new Scene(vBox, WIDTH, HEIGHT);
        scene.setOnKeyReleased(keyListener);
        
        message = new Text("");
        message.setFill(Paint.valueOf("red"));

        Text statusMessage = new Text();
        statusMessage.setFill(Paint.valueOf("#595"));

        Text filePathTxt = new Text();
        filePathTxt.setFill(Paint.valueOf("#000"));

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpeg", "*.jpg", "*.gif", "*.tif"));

        Button fileSelectButton = new Button();

        fileSelectButton.setOnAction(event -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                filePathTxt.setText(selectedFile.getAbsolutePath());
                Thread compute = new Thread() {
                    @Override
                    public void run() {
                        imageCanvas.loadImage(selectedFile);
                    }
                };
                compute.start();
            } else {
                filePathTxt.setText("No File Selected");
            }
        });

        fileSelectButton.setText("Select File");

        Text controls = new Text("To pan use:\t'W': up\t'D': right\t'S' down\t'A': left");
        Text Zoom = new Text("To zoom use the bar below:");

        vBox.setPadding(new Insets(5));
        vBox.getChildren().add(controls);
        vBox.getChildren().add(message);
        vBox.getChildren().add(statusMessage);
        vBox.getChildren().add(filePathTxt);
        vBox.getChildren().add(fileSelectButton);

        vBox.getChildren().addAll(
                imageCanvas.getImageProgressBar(),
                imageCanvas.getStatusMessage(),
                Zoom,
                imageCanvas.getZoomSlider()
                );

        stage.setScene(scene);
     
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    public static int[] fitDimensions(BufferedImage in, double x, double y, double zoom) {

        int rectW = (int) (in.getWidth() * zoom);
        int rectH = (int) (in.getHeight() * zoom);

        int xpos, ypos;
        if (in.getWidth() > in.getHeight()) {
            xpos = (int) (in.getWidth() * (double) (x / 100));
            ypos = (int) (in.getHeight() * (double) (y * (in.getWidth() / in.getHeight())) / 100);
        } else {
            ypos = (int) (in.getHeight() * (double) (y / 100));
            xpos = (int) (in.getWidth() * (double) (x * (in.getHeight() / in.getWidth())) / 100);
        }

        // check if grab is in bounds of original
        if (rectW + xpos >= in.getWidth()) {
            xpos = in.getWidth() - rectW - 1;
        }
        if (rectH + ypos >= in.getHeight()) {
            ypos = in.getHeight() - rectH - 1;
        }

        int[] out = { xpos, ypos, rectW, rectH };
        return out;
    }

}