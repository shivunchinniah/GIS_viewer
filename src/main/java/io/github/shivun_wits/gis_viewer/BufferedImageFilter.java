package io.github.shivun_wits.gis_viewer;

import java.awt.image.BufferedImage;

public class BufferedImageFilter {

    public static BufferedImage resize(BufferedImage src, int width, int height)
    {
        // Determine how pixels will be mapped
        double scaleW = (double)src.getWidth() / width; 
        double scaleH = (double)src.getHeight() / height;

        // Create output buffer
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);


        // Sample pixels
        for(int i = 0; i < height; i++ ){
            double srcH =  (i * scaleH);
            int[] scan = src.getRGB(0, (int) srcH, src.getWidth(), 1, null, 0, src.getWidth());
            for(int j = 0; j < width; j++){
                double srcW = (j * scaleW);
                out.setRGB(j, i, scan[(int)srcW]);
            }
        }

        return out;
    }

}
