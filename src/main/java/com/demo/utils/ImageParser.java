package com.demo.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Base64;

import static com.demo.core.logger.DefaultLogger.logStaticError;


public class ImageParser {

    private static BufferedImage getBufferedImage(String imageSource, int index) {
        try {
            index++;
            String[] split = imageSource.split(",");
            String base64Image;
            if (split.length<3){
                base64Image = split[1];
            } else {
                base64Image = split[index];
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            logStaticError("Failed to decode or read base64 image. Index: {}, Source length: {}", e, index, imageSource.length());
            return null;
        }
    }

    private static void writeBufferedImage(BufferedImage img, String imagePath) {
        try {
            ImageIO.write(img, "png", new File(imagePath));
        } catch (IOException e) {
            logStaticError("Failed to write image to path '{}'", e, imagePath);
        }
    }

    private static void writeBufferedImage(BufferedImage img, String formatName, String imagePath) {
        try {
            ImageIO.write(img, formatName, new File(imagePath));
        } catch (IOException e) {
            logStaticError("Failed to write image to path '{}' with format '{}'", e, imagePath, formatName);
        }
    }

    public static void loadImage(String imageSource, String filename, int index) {
        writeBufferedImage(getBufferedImage(imageSource, index), filename);
    }

    public static void loadImage(String imageSource, String formatName, String filename) {
        writeBufferedImage(getBufferedImage(imageSource, 1), formatName, filename);
    }

    private static BufferedImage getBufferedImageFromUrl(String url) {
        try {
            URL urlFromString = URI.create(url).toURL();
            return ImageIO.read(urlFromString);
        } catch (IOException e) {
            logStaticError("Failed to read image from URL '{}'", e, url);
            return null;
        }
    }

    public static void loadImageFromUrl(String url, String filename) {
        writeBufferedImage(getBufferedImageFromUrl(url), filename);
    }
}