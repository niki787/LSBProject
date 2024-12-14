package com.lsb; // Убедитесь, что пакет соответствует вашему проекту

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test; // Импортируйте JUnit 5 API
import static org.junit.jupiter.api.Assertions.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExtractTextTest {
    private static final Logger logger = LogManager.getLogger(ExtractTextTest.class); // Инициализируйте логгер

    private static final String DELIMITER = "END";
    private static final String TEST_MESSAGE = "This is a test message.";

    @Test
    void testEmbedAndExtract() throws IOException {
        logger.info("Starting testEmbedAndExtract"); // Логирование начала теста

        Path tempDir = Files.createTempDirectory("lsb-test");
        File originalImage = new File(tempDir.toFile(), "original.bmp");
        File imageWithText = new File(tempDir.toFile(), "with_text.bmp");

        try {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = img.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2d.dispose();
            ImageIO.write(img, "bmp", originalImage);

            EmbedText.embedText(originalImage.getAbsolutePath(), TEST_MESSAGE, imageWithText.getAbsolutePath(), DELIMITER);
            String extractedText = ExtractText.extractText(imageWithText.getAbsolutePath(), DELIMITER);

            assertEquals(TEST_MESSAGE, extractedText);
            logger.info("Test passed successfully"); // Логирование успешного завершения
        } catch (IOException e) {
            logger.error("Error during test: ", e); // Логирование ошибки с исключением
            fail("Test failed due to IOException: " + e.getMessage()); // JUnit assertion для провала теста
        } finally {
            // Cleanup: delete temporary files
            originalImage.delete();
            imageWithText.delete();
            try {
                Files.delete(tempDir);
            } catch (IOException e) {
                logger.warn("Could not delete temporary directory: {}", tempDir, e); //  Log warning, but don't fail the test
            }
        }
    }

    @Test
    void testLSBVisualizationNullImage() {
        logger.info("Starting testLSBVisualizationNullImage");
        BufferedImage image = null;
        BufferedImage lsbImage = LSBVisualization.visualizeLSBBits(image);
        assertNull(lsbImage);
        logger.info("Test finished successfully");
    }

    @Test
    void testLSBVisualizationValidImage() throws IOException {
        logger.info("Starting testLSBVisualizationValidImage");
        try {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = img.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
            g2d.dispose();


            BufferedImage lsbImage = LSBVisualization.visualizeLSBBits(img);

            assertNotNull(lsbImage); // Проверяем, что метод вернул не null
            assertEquals(img.getWidth(), lsbImage.getWidth()); // Проверяем ширину
            assertEquals(img.getHeight(), lsbImage.getHeight()); // Проверяем высоту

            logger.info("Test finished successfully");
        } catch (Exception e) { // more general exception catching
            logger.error("Error during test:", e);
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }
}
