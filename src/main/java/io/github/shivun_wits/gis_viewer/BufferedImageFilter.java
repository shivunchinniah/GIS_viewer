package io.github.shivun_wits.gis_viewer;

import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class BufferedImageFilter {

    public static BufferedImage resize(BufferedImage src, int width, int height) {
        // Determine how pixels will be mapped
        double scaleW = (double) src.getWidth() / width;
        double scaleH = (double) src.getHeight() / height;

        // Create output buffer
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Sample pixels
        for (int i = 0; i < height; i++) {
            double srcH = (i * scaleH);
            int[] scan = src.getRGB(0, (int) srcH, src.getWidth(), 1, null, 0, src.getWidth());
            for (int j = 0; j < width; j++) {
                double srcW = (j * scaleW);
                out.setRGB(j, i, scan[(int) srcW]);
            }
        }

        return out;
    }

    public static class Resize extends RecursiveAction {

        public static final long threshold = 60;

        BufferedImage src;
        int width;
        int height;
        int start;
        int end;
        public BufferedImage out;

        public Resize(BufferedImage src, int width, int height) {
            this(src, width, height, 0, height);
            out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
        }

        private double scaleW;
        private double scaleH;

        private Resize(BufferedImage src, int width, int height, int start, int end, BufferedImage out) {
            this.src = src;
            this.width = width;
            this.height = height;
            this.start = start;
            this.end = end;
            // Determine how pixels will be mapped
            scaleW = (double) src.getWidth() / width;
            scaleH = (double) src.getHeight() / height;
            this.out = out;
        }

        private Resize(BufferedImage src, int width, int height, int start, int end) {
            this.src = src;
            this.width = width;
            this.height = height;
            this.start = start;
            this.end = end;
            // Determine how pixels will be mapped
            scaleW = (double) src.getWidth() / width;
            scaleH = (double) src.getHeight() / height;
        }

        @Override
        protected void compute() {

            System.out.println("Start: "+start + " end: "+end);

            int length = end - start;

            if (length < threshold) {
                // Sample pixels
                for (int i = start; i < end; i++) {
                    double srcH = (i * scaleH);
                    int[] scan = src.getRGB(0, (int) srcH, src.getWidth(), 1, null, 0, src.getWidth());
                    for (int j = 0; j < width; j++) {
                        double srcW = (j * scaleW);
                        out.setRGB(j, i, scan[(int) srcW]);
                    }
                }
            } else {

                Resize first = new Resize(src, width, height, start, start + (length / 2), out);
                Resize second = new Resize(src, width, height, (start + length / 2), end, out);
                Resize.invokeAll(first, second);

            }
        }

    }

}
