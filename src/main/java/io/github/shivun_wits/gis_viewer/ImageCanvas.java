package io.github.shivun_wits.gis_viewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Text;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;

public class ImageCanvas {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    public static final Color STATUS_ERROR = Color.valueOf("red");
    public static final Color STATUS_SUCCESS = Color.valueOf("green");
    public static final Color STATUS_DEFAULT = Color.valueOf("black");

    private Canvas canvas;
    private GraphicsContext ctx;

    private BufferedImage loadedImage;
    private IIOReadProgressListener imageProgressListener;

    public Text statusMessage;
    public ProgressBar imageProgressBar;

    private double zoomLevel, zoomX, zoomY;

    ImageCanvas() {
        canvas = new Canvas(WIDTH, HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        imageProgressBar = new ProgressBar();
        imageProgressBar.setVisible(false);
        statusMessage = new Text();

        imageProgressListener = new IIOReadProgressListener() {

            public void thumbnailComplete(ImageReader source) {
            }

            public void thumbnailProgress(ImageReader source, float percentageDone) {
            }

            public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
            }

            public void sequenceStarted(ImageReader source, int minIndex) {
                // TODO Auto-generated method stub

            }

            public void sequenceComplete(ImageReader source) {
                // TODO Auto-generated method stub

            }

            public void imageStarted(ImageReader source, int imageIndex) {

            }

            public void imageProgress(ImageReader source, float percentageDone) {
                // TODO Auto-generated method stub

            }

            public void imageComplete(ImageReader source) {
                // TODO Auto-generated method stub

            }

            public void readAborted(ImageReader source) {
            }

        };
    }

    public void updateStatusMessage(Color color, String message){
        statusMessage.setText(message);
        statusMessage.setFill(color);
    }

    public void loadImage(File file) {

        try {
            final FileInputStream fin = new FileInputStream(file);
            final ImageInputStream iin = ImageIO.createImageInputStream(fin);
            final Iterator readers = ImageIO.getImageReaders(iin);
            final ImageReader reader = (ImageReader) readers.next();
            reader.addIIOReadProgressListener(imageProgressListener);
            reader.setInput(iin, true);
            
            loadedImage = reader.read(0);

            drawImage();

        } catch (FileNotFoundException e) {
            updateStatusMessage(STATUS_ERROR, "The file could not be found.");
        } catch (IOException e) {
            updateStatusMessage(STATUS_ERROR, "An IO exception occurred: "+e.getMessage());
        }

    }

    public void drawImage(){
        ctx.drawImage(scaleImage(loadedImage, WIDTH, HEIGHT), 0, 0);
    }

    private static Image scaleImage(BufferedImage pic, int width, int height) {

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

}
