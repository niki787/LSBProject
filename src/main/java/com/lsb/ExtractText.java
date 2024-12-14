package com.lsb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Класс, предоставляющий функциональность для извлечения текста,
 * встроенного в изображение с помощью метода наименьшего значащего бита (LSB).
 * <p>
 * Предполагается, что текст был встроен в синий канал изображения в формате BMP.
 * </p>
 */
public class ExtractText {

    /**
     * Извлекает текст из изображения, в которое он был встроен с помощью метода LSB.
     *
     * <p>Метод считывает изображение в формате BMP, извлекает младшие биты синего канала
     * каждого пикселя, объединяет их в байты и декодирует полученную
     * последовательность байтов в строку, используя кодировку UTF-8.
     * Извлечение продолжается до тех пор, пока не будет найден разделитель,
     * указывающий на конец встроенного текста.</p>
     *
     * @param imagePath Путь к файлу изображения в формате BMP, содержащему встроенный текст.
     * @param delimiter Разделитель, обозначающий конец встроенного текста.
     * @return Извлеченный текст, декодированный с использованием UTF-8.
     *         Если текст не найден или произошла ошибка, возвращается пустая строка.
     * @throws IOException Если произошла ошибка при чтении файла изображения
     *                     или если файл изображения не соответствует формату BMP.
     * @see com.lsb.EmbedText#embedText(String, String, String, String)
     */
    public static String extractText(String imagePath, String delimiter) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        if (image == null) {
            throw new IOException("Не удалось загрузить изображение"); // Кидаем исключение, если изображение не загрузилось.
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate((image.getWidth() * image.getHeight()) / 8);
        byte currentByte = 0;
        int bitIndex = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xff;
                int bit = blue & 1;

                currentByte = (byte) ((currentByte << 1) | bit);
                bitIndex++;
                if (bitIndex == 8) {
                    byteBuffer.put(currentByte);
                    currentByte = 0;
                    bitIndex = 0;
                }
            }
        }
        byteBuffer.flip();
        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
        String extractedText = new String(bytes, StandardCharsets.UTF_8);

        int delimiterIndex = extractedText.indexOf(delimiter);
        if (delimiterIndex != -1) {
            return extractedText.substring(0, delimiterIndex);
        } else {
            return ""; // Возвращаем пустую строку, если разделитель не найден.
        }
    }
}