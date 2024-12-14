package com.lsb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Класс, реализующий встраивание текста в изображение
 * с использованием метода наименьшего значащего бита (LSB).
 * <p>
 * Текст кодируется в UTF-8, затем преобразуется в последовательность байтов,
 * и каждый бит этих байтов записывается в младший бит синего канала
 * пикселей изображения. Если текст вместе с разделителем короче, чем
 * доступное количество битов в изображении, оставшиеся биты заполняются
 * псевдослучайными значениями.
 * </p>
 * <p>Важно: входное и выходное изображения должны быть в формате BMP.</p>
 */
public class EmbedText {

    /**
     * Генератор псевдослучайных чисел для заполнения неиспользуемых битов.
     */
    private static final Random random = new Random();

    /**
     * Встраивает текст в изображение, используя метод LSB.
     *
     * @param imagePath  Путь к исходному изображению в формате BMP.
     * @param text       Текст для встраивания (будет закодирован в UTF-8).
     * @param outputPath Путь для сохранения результирующего изображения в формате BMP.
     * @param delimiter  Разделитель, добавляемый к тексту для обозначения его конца при извлечении.
     * @throws IOException Если произошла ошибка чтения или записи изображения,
     *                     если изображение слишком мало для встраивания текста,
     *                     или если формат изображения не поддерживается.
     * @see com.lsb.ExtractText#extractText(String, String)
     */
    public static void embedText(String imagePath, String text, String outputPath, String delimiter) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        if (image == null) {
            throw new IOException("Не удалось загрузить изображение: " + imagePath);
        }

        byte[] textBytes = (text + delimiter).getBytes();
        long totalBitsNeeded = (long) textBytes.length * 8;
        long totalBitsAvailable = (long) image.getWidth() * image.getHeight();

        if (totalBitsNeeded > totalBitsAvailable) {
            throw new IOException("Изображение слишком мало для встраивания текста");
        }

        int textIndex = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;

                if (textIndex < textBytes.length * 8) {
                    int bitToEmbed = (textBytes[textIndex / 8] >> (7 - (textIndex % 8))) & 1;
                    blue = (blue & ~1) | bitToEmbed;
                    textIndex++;
                }
                else {
                    blue = (blue & ~1) | random.nextInt(2); // Set LSB to 0 or 1 randomly
                }

                rgb = (rgb & 0xFFFFFF00) | blue;
                image.setRGB(x, y, rgb);
            }
        }

        ImageIO.write(image, "bmp", new File(outputPath));
    }
}