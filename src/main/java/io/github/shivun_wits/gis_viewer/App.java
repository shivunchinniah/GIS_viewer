package io.github.shivun_wits.gis_viewer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import java.awt.image.AffineTransformOp;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Text message;
    static ImageView imageView;
    static ImageView smallView;

    static final double WIDTH = 1080;
    static final double HEIGHT = 720;
    static BufferedImage loadedImage;
    static double zoomLevel = 1;
    static double zoomX = 1, zoomY = 1;
    static Text zoomText = new Text("Zoom: " + zoomLevel);

    @Override
    public void start(Stage stage) throws FileNotFoundException {
        stage.setTitle("GIS Viewer");
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        imageView = new ImageView();
        imageView.setFitWidth(600);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        BorderPane imageViewBorder = new BorderPane(imageView);
        imageViewBorder.setBorder(new Border(new BorderStroke(
                Paint.valueOf("#000"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(0),
                BorderWidths.DEFAULT)));
        BorderPane.setAlignment(imageView, Pos.CENTER);

        smallView = new ImageView();
        smallView.setFitWidth(100);
        smallView.setFitHeight(100);
        smallView.setPreserveRatio(true);

        HBox imageBox = new HBox(imageViewBorder);
        imageBox.getChildren().add(smallView);

        VBox vBox = new VBox(imageBox);
        scene = new Scene(vBox, WIDTH, HEIGHT);

        message = new Text("Hello World!");
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


        Slider zoom = new Slider(1, 100, 1);

        fileSelectButton.setOnAction(event -> {
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                filePathTxt.setText(selectedFile.getAbsolutePath());

                Thread compute = new Thread() {
                    @Override
                    public void run() {
                        statusMessage.setText("Loading image...");
                        try {
                            loadedImage = ImageIO.read(selectedFile);
                        } catch (FileNotFoundException e) {
                            System.out.println("File Not Found.");
                        } catch (IOException e) {
                            System.out.println("IO exception while loading image");
                        }
                        statusMessage.setText("Scaling image...");

                        imageView.setImage(scaleImage(loadedImage, 600, 400));
                        smallView.setImage(scaleImage(loadedImage, 100, 100));

                        zoomX = 0;
                        zoomY = 0;

                        zoom.setValue(1);
                        updateZoom();
                        statusMessage.setText("");
                    }
                };

                compute.start();

            } else {
                filePathTxt.setText("No File Selected");
            }
        });

        fileSelectButton.setText("Select File");

        EventHandler<MouseEvent> findCursor = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                zoomX = event.getX();
                zoomY = event.getY();
                updateZoom();
            }

        };

        ChangeListener<Number> sliderUpdate = new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) {
                zoomLevel = 1 / newVal.doubleValue();
                updateZoom();
            }
        };

        smallView.setOnMouseMoved(findCursor);

        vBox.setPadding(new Insets(5));

        
        zoom.valueProperty().addListener(sliderUpdate);

        vBox.getChildren().add(message);
        vBox.getChildren().add(statusMessage);
        vBox.getChildren().add(filePathTxt);
        vBox.getChildren().add(fileSelectButton);
        vBox.getChildren().addAll(zoom, zoomText);
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

    public static Image scaleImage(BufferedImage pic, int width, int height) {

        // BufferedImage out = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);

        int targW, targH;

        if (pic.getWidth() > pic.getHeight()) {
            targW = width;
            targH = (int) (pic.getHeight() * ((double) width / pic.getWidth()));
        } else {
            targH = height;
            targW = (int) (pic.getWidth() * ((double) height / pic.getHeight()));
        }

        BufferedImage out = BufferedImageFilter.resize(pic, targW, targH);

        pic.flush();
        return SwingFXUtils.toFXImage((BufferedImage) out, null);
    }

    static void updateZoom() {
        zoomText.setText("Zoom: " + 1/zoomLevel);
        message.setText("X: " + zoomX + "\t\tY: " + zoomY);

        int[] fit = fitDimensions(loadedImage, zoomX, zoomY, zoomLevel);
        // System.out.println("Fit x:" + fit[0] + " y:" + fit[1]);

        imageView.setImage(scaleImage(loadedImage.getSubimage(fit[0], fit[1], fit[2], fit[3]), 600, 400));

    }

}