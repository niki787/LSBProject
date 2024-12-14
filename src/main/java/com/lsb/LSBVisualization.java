package com.lsb;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Класс, предоставляющий методы для визуализации младших значащих битов (LSB) синего канала изображения.
 * Позволяет создавать изображение в градациях серого, где каждый пиксель представляет LSB соответствующего пикселя
 * исходного изображения.
 */
public class LSBVisualization {

    /**
     * Визуализирует младшие биты синего канала входного изображения.
     * Создает новое изображение в градациях серого, где значение каждого пикселя (0 или 255) соответствует
     * младшему биту синего канала пикселя исходного изображения.
     *
     * @param inputImage Исходное изображение.
     * @return Изображение в градациях серого, визуализирующее LSB синего канала.
     *         Возвращает {@code null}, если входное изображение {@code null}.
     */
    public static BufferedImage visualizeLSBBits(BufferedImage inputImage) {
        if (inputImage == null) {
            return null;
        }

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY); // Используем градации серого

        WritableRaster raster = outputImage.getRaster();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = inputImage.getRGB(x, y);
                int blue = rgb & 0xFF;
                int lsb = blue & 1;
                int grayScaleValue = lsb * 255;

                raster.setSample(x, y, 0, grayScaleValue);
            }
        }

        return outputImage;
    }
}