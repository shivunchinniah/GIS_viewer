package io.github.shivun_wits.gis_viewer;

import javafx.beans.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Text;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;

public class ImageCanvas {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    public static final Color STATUS_ERROR = Color.valueOf("red");
    public static final Color STATUS_SUCCESS = Color.valueOf("green");
    public static final Color STATUS_DEFAULT = Color.valueOf("black");

    private Canvas canvas;
    private GraphicsContext ctx;

    private BufferedImage loadedImage;
    private IIOReadProgressListener imageProgressListener;

    private Text statusMessage;
    private ProgressBar imageProgressBar;
    private Text coordinateText;

    private Slider zoomSlider;
    double zoomSliderMax;
    private double zoomLevel, zoomX, zoomY;

    ImageCanvas() {
        canvas = new Canvas(WIDTH, HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        imageProgressBar = new ProgressBar(0);
        imageProgressBar.setVisible(false);

        statusMessage = new Text();
        zoomSlider = new Slider();

        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            zoomLevel = newValue.doubleValue();
            System.out.println("Zoom level" + zoomLevel);
            drawImage();
        });

        coordinateText = new Text();

        imageProgressListener = new IIOReadProgressListener() {

            public void thumbnailComplete(ImageReader source) {
            }

            public void thumbnailProgress(ImageReader source, float percentageDone) {
            }

            public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {
            }

            public void sequenceStarted(ImageReader source, int minIndex) {
            }

            public void sequenceComplete(ImageReader source) {
            }

            public void imageStarted(ImageReader source, int imageIndex) {
                updateStatusMessage(STATUS_DEFAULT, "Loading image...");
                imageProgressBar.setProgress(0);
                imageProgressBar.setVisible(true);
            }

            public void imageProgress(ImageReader source, float percentageDone) {
                imageProgressBar.setProgress(percentageDone / 100);
            }

            public void imageComplete(ImageReader source) {
                updateStatusMessage(STATUS_DEFAULT, "");
                imageProgressBar.setProgress(1);
                imageProgressBar.setVisible(false);
            }

            public void readAborted(ImageReader source) {
                updateStatusMessage(STATUS_ERROR, "Read Aborted");
                imageProgressBar.setProgress(0);
                imageProgressBar.setVisible(false);
            }

        };
    }

    public void updateStatusMessage(Color color, String message) {
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

            updateStatusMessage(STATUS_DEFAULT, "Loading image...");
            loadedImage = reader.read(0);

            initZoom();
            drawImage();

        } catch (FileNotFoundException e) {
            updateStatusMessage(STATUS_ERROR, "The file could not be found.");
        } catch (IOException e) {
            updateStatusMessage(STATUS_ERROR, "An IO exception occurred: " + e.getMessage());
        }

    }

    private void initZoom() {
        // determine the maximum magnification
        zoomSliderMax = 100;// fitFactor() * 0.001 * Math.min(loadedImage.getWidth(),
                            // loadedImage.getHeight());
        zoomX = (double) loadedImage.getWidth() / 2;
        zoomY = (double) loadedImage.getHeight() / 2;

        zoomSlider.setMin(1);
        zoomSlider.setMax(zoomSliderMax);
        zoomLevel = 1;
        zoomSlider.setValue(1);

    }

    private double fitFactor() {

        double out;

        double fitWidth = (double) (loadedImage.getWidth() / WIDTH);
        double fitHeight = (double) (loadedImage.getHeight() / HEIGHT);

        if (fitWidth * loadedImage.getHeight() > HEIGHT) {
            return fitHeight;
        } else {
            return fitWidth;
        }

        // return out;

    }

    public void move(int direction) {

        // up, right, down, left

        switch (direction) {

            case 1:
                zoomY -= calcluateImageCropParameters()[3] * 0.1;
                break;

            case 2:
                zoomX += calcluateImageCropParameters()[2] * 0.1;
                break;

            case 3:
                zoomY += calcluateImageCropParameters()[3] * 0.1;

                break;
            case 4:
                zoomX -= calcluateImageCropParameters()[2] * 0.1;

                break;
            default:
                break;
        }

        drawImage();

    }

    private int[] calcluateImageCropParameters() {

        int rectW, rectH;

        double fit = fitFactor();

        int rectWtemp = (int) (fit * (double) WIDTH / zoomLevel);
        int rectHtemp = (int) (fit * (double) HEIGHT / zoomLevel);

        rectW = rectWtemp;
        rectH = rectHtemp;

        if (loadedImage.getWidth() < rectWtemp) {
            rectW = loadedImage.getWidth();

        }
        if (loadedImage.getHeight() < rectHtemp) {
            rectH = loadedImage.getHeight();
        }

        int topLeftX = (int) (zoomX - (rectW / 2));
        int topLeftY = (int) (zoomY - (rectH / 2));

        if (topLeftX + rectW > loadedImage.getWidth()) {
            topLeftX = loadedImage.getWidth() - rectW - 1;
        }

        if (topLeftY + rectH > loadedImage.getHeight()) {
            topLeftY = loadedImage.getHeight() - rectH - 1;
        }

        if (topLeftX < 0) {
            topLeftX = 0;
        }

        if (topLeftY < 0) {
            topLeftY = 0;
        }

        // modify zoom left and zoom right
        zoomX = topLeftX + (rectW / 2);
        zoomY = topLeftY + (rectH / 2);

        int[] out = { topLeftX, topLeftY, rectW, rectH };
        return out;
    }

    public void drawImage() {

        try {
            ctx.clearRect(0, 0, WIDTH, HEIGHT);
            int[] cropParams = calcluateImageCropParameters();
            Image img = scaleImage(loadedImage.getSubimage(cropParams[0], cropParams[1], cropParams[2], cropParams[3]),
                    WIDTH, HEIGHT);
            System.out.println("Scaled w:" + img.getWidth() + " h:" + img.getHeight());
            ctx.drawImage(img, 0, 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static Image scaleImage(BufferedImage pic, int widthIn, int heightIn) {

        // BufferedImage out = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        int width = widthIn, height = heightIn;

        int targW, targH;

        if (pic.getWidth() > pic.getHeight()) {
            targW = width;
            targH = (int) (pic.getHeight() * ((double) width / pic.getWidth()));
        } else {
            targH = height;
            targW = (int) (pic.getWidth() * ((double) height / pic.getHeight()));
        }

        BufferedImageFilter.Resize r = new BufferedImageFilter.Resize(pic, targW, targH);
        new ForkJoinPool().invoke(r);
        BufferedImage out = r.out;
        // BufferedImage out = BufferedImageFilter.resize(pic, targW, targH);

        // // check if out height is greater than maximum allowed
        // if (out.getHeight() > HEIGHT)
        // out = BufferedImageFilter.resize(out, out.getWidth() * HEIGHT / WIDTH,
        // HEIGHT);

        pic.flush();
        return SwingFXUtils.toFXImage((BufferedImage) out, null);
    }

    // -------- Accessors and Mutators --------

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public GraphicsContext getCtx() {
        return this.ctx;
    }

    public void setCtx(GraphicsContext ctx) {
        this.ctx = ctx;
    }

    public BufferedImage getLoadedImage() {
        return this.loadedImage;
    }

    public void setLoadedImage(BufferedImage loadedImage) {
        this.loadedImage = loadedImage;
    }

    public IIOReadProgressListener getImageProgressListener() {
        return this.imageProgressListener;
    }

    public void setImageProgressListener(IIOReadProgressListener imageProgressListener) {
        this.imageProgressListener = imageProgressListener;
    }

    public Text getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(Text statusMessage) {
        this.statusMessage = statusMessage;
    }

    public ProgressBar getImageProgressBar() {
        return this.imageProgressBar;
    }

    public void setImageProgressBar(ProgressBar imageProgressBar) {
        this.imageProgressBar = imageProgressBar;
    }

    public double getZoomLevel() {
        return this.zoomLevel;
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public double getZoomX() {
        return this.zoomX;
    }

    public void setZoomX(double zoomX) {
        this.zoomX = zoomX;
    }

    public double getZoomY() {
        return this.zoomY;
    }

    public void setZoomY(double zoomY) {
        this.zoomY = zoomY;
    }

    public Text getCoordinateText() {
        return this.coordinateText;
    }

    public void setCoordinateText(Text coordinateText) {
        this.coordinateText = coordinateText;
    }

    public Slider getZoomSlider() {
        return this.zoomSlider;
    }

    public void setZoomSlider(Slider zoomSlider) {
        this.zoomSlider = zoomSlider;
    }

    public double getZoomSliderMax() {
        return this.zoomSliderMax;
    }

    public void setZoomSliderMax(double zoomSliderMax) {
        this.zoomSliderMax = zoomSliderMax;
    }

}
