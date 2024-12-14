package com.lsb;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Главный класс приложения, запускающий графический интерфейс для стеганографии.
 * <p>
 *     Приложение предоставляет пользователю возможность встраивать и извлекать текст из изображений
 *     с помощью метода наименьшего значащего бита (LSB).
 * </p>
 */
public class SteganographyApp extends Application {

    /** Поле ввода пути к исходному изображению. */
    private TextField inputPathField;
    /** Поле ввода пути к результирующему изображению. */
    private TextField outputPathField;
    /** Поле ввода текста для встраивания. */
    private TextField inputText;
    /** Область отображения извлеченного текста. */
    private TextArea outputText;
    /** Отображение исходного изображения. */
    private ImageView inputImage;
    /** Отображение результирующего изображения. */
    private ImageView outputImage;
    /** Отображение LSB исходного изображения. */
    private ImageView inputLSBImageView;
    /** Отображение LSB результирующего изображения. */
    private ImageView outputLSBImageView;

    /** Кнопка выбора исходного изображения. */
    private Button chooseInput;
    /** Кнопка выбора результирующего изображения. */
    private Button chooseOutput;
    /** Кнопка встраивания текста. */
    private Button embed;
    /** Кнопка извлечения текста. */
    private Button extract;

    /** Файл исходного изображения. */
    private File inputFile;
    /** Файл результирующего изображения. */
    private File outputFile;

    /** Разделитель, используемый для обозначения конца встроенного текста. */
    private static final String DELIMITER = "END";
    /** Логгер для записи событий приложения. */
    private static final Logger logger = LogManager.getLogger(SteganographyApp.class);

    /** Свойство, хранящее файл с результатом встраивания. */
    private final SimpleObjectProperty<File> resultFileProperty = new SimpleObjectProperty<>();
    /** Свойство, хранящее сообщение об ошибке. */
    private final SimpleStringProperty errorMessageProperty = new SimpleStringProperty();

    /**
     * Точка входа приложения JavaFX.  Инициализирует и отображает графический интерфейс пользователя.
     *
     * @param primaryStage Основное окно приложения.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Приложение для вложения текста в картинки методом LSB");
        inputPathField = createTextField(false);
        outputPathField = createTextField(false);
        inputText = createTextField(true);
        outputText = createTextArea();
        inputImage = new ImageView();
        outputImage = new ImageView();
        chooseInput = createButton("Выбрать файл");
        chooseOutput = createButton("Выбрать файл");
        embed = createButton("Вложить текст");
        extract = createButton("Извлечь текст");
        inputLSBImageView = new ImageView();
        outputLSBImageView = new ImageView();

        chooseInput.setOnAction(e -> handleImageSelection(inputPathField, inputImage, inputLSBImageView, true));
        chooseOutput.setOnAction(e -> handleImageSelection(outputPathField, outputImage, outputLSBImageView, false));
        embed.setOnAction(e -> embedText());
        extract.setOnAction(e -> extractText());

        Platform.runLater(() -> {

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10));

            grid.add(new Label("Оригинал:"), 0, 0);
            grid.add(outputImage, 0, 1);
            grid.add(outputLSBImageView, 1, 1);
            grid.add(new Label("Результат:"), 0, 2);
            grid.add(inputImage, 0, 3);
            grid.add(inputLSBImageView, 1, 3);

            HBox inputImageControls = new HBox(10, inputPathField, chooseInput);
            HBox outputImageControls = new HBox(10, outputPathField, chooseOutput);

            grid.add(new Label("Изображение (Извлечение):"), 0, 4);
            grid.add(inputImageControls, 1, 4);

            grid.add(new Label("Изображение (Вложение):"), 0, 5);
            grid.add(outputImageControls, 1, 5);

            grid.add(new Label("Текст для вложения:"), 0, 6);
            grid.add(inputText, 1, 6);

            grid.add(new Label("Извлеченный текст:"), 0, 7);
            grid.add(outputText, 1, 7);

            HBox buttons = new HBox(10, embed, extract);
            buttons.setAlignment(Pos.CENTER);
            grid.add(buttons, 1, 8);

            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true);

            Scene scene = new Scene(scrollPane, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();
        });

        resultFileProperty.addListener((observable, oldValue, newValue) -> {
            updateImages(outputFile, outputLSBImageView, newValue, inputLSBImageView);
            String message = errorMessageProperty.get();
            if (message != null) {
                showAlert(Alert.AlertType.ERROR, message);
            } else {
                showAlert("Текст успешно вложен!");
                logger.info("Текст успешно вложен в изображение {}", newValue != null ? newValue.getName() : "null");
            }
        });

        chooseInput.setOnAction(e -> handleImageSelection(inputPathField, inputImage, inputLSBImageView, true));
        chooseOutput.setOnAction(e -> handleImageSelection(outputPathField, outputImage, outputLSBImageView, false));
        embed.setOnAction(e -> embedText());
        extract.setOnAction(e -> extractText());
    }

    /**
     * Создает текстовое поле.
     *
     * @param editable Флаг, указывающий, разрешено ли редактирование текста в поле.
     * @return Созданное текстовое поле.
     */
    private TextField createTextField(boolean editable) {
        TextField tf = new TextField();
        tf.setEditable(editable);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    /**
     * Создает область текста.
     *
     * @return Созданная область текста.
     */
    private TextArea createTextArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setStyle("-fx-border-color: gray; -fx-border-width: 1px;");
        ta.setMaxWidth(Double.MAX_VALUE);
        return ta;
    }

    /**
     * Создает кнопку.
     *
     * @param text Текст на кнопке.
     * @return Созданная кнопка.
     */
    private Button createButton(String text) {
        return new Button(text);
    }

    /**
     * Обрабатывает выбор изображения.
     *
     * @param pathField Поле ввода пути к файлу.
     * @param imageView Отображение изображения.
     * @param lsbImageView Отображение LSB изображения.
     * @param isInput  {@code true}, если выбирается исходное изображение, {@code false} - если результирующее.
     */
    private void handleImageSelection(TextField pathField, ImageView imageView, ImageView lsbImageView, boolean isInput) {
        File file = chooseFile(isInput);
        if (file != null) {
            pathField.setText(file.getAbsolutePath());
            try {
                Image image = new Image(file.toURI().toString());
                if (image.isError()) {
                    throw new IOException("Не удалось загрузить изображение");
                }
                imageView.setImage(image);
                updateLSBVisualization(lsbImageView, file);
                if (isInput) inputFile = file; else outputFile = file;
                logger.info("Изображение {} успешно загружено", file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка загрузки изображения: " + e.getMessage());
                logger.error("Ошибка загрузки изображения: {}", e.getMessage());
            }
        }
    }

    /**
     * Выбирает файл изображения.
     *
     * @param forInput {@code true}, если файл выбирается для извлечения текста, {@code false} - для вложения.
     * @return Выбранный файл или {@code null}, если выбор отменен.
     */
    private File chooseFile(boolean forInput) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(forInput ? "Выберите изображение для извлечения" : "Выберите изображение для вложения");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP Files", "*.bmp"));
        return chooser.showOpenDialog(null);
    }

    /**
     * Встраивает текст в изображение.
     */
    private void embedText() {
        if (outputFile == null || inputText.getText().isEmpty()) {
            showAlert("Выберите изображение и введите текст.");
            return;
        }

        File outputFileToSave = chooseSaveFile();
        if (outputFileToSave == null) {
            showAlert("Сохранение отменено");
            return;
        }

        Task<File> task = new Task<File>() {
            protected File call() throws Exception {
                EmbedText.embedText(outputFile.getAbsolutePath(), inputText.getText() + DELIMITER, outputFileToSave.getAbsolutePath(), DELIMITER);
                return outputFileToSave;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            File resultFile = task.getValue(); // Get the result File
            updateImages(outputFile, outputLSBImageView, resultFile, inputLSBImageView);
            showAlert("Текст успешно вложен!");
            logger.info("Текст успешно вложен в изображение {}", resultFile.getName());
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            Throwable exception = task.getException();
            String errorMessage = "Ошибка при вложении текста: " + (exception != null ? exception.getMessage() : "Неизвестная ошибка");
            showAlert(Alert.AlertType.ERROR, errorMessage);
            logger.error("Ошибка при вложении текста");
        }));

        new Thread(task).start();
    }

    /**
     * Обновляет отображаемые изображения.
     *
     * @param original  Исходное изображение.
     * @param originalLSB Отображение LSB исходного изображения.
     * @param result Результирующее изображение.
     * @param resultLSB Отображение LSB результирующего изображения.
     */
    private void updateImages(File original, ImageView originalLSB, File result, ImageView resultLSB) {
        if (original != null) {
            inputImage.setImage(new Image(original.toURI().toString()));
            updateLSBVisualization(originalLSB, original);
        } else {
            inputImage.setImage(null);
            originalLSB.setImage(null);
        }
        if (result != null) {
            outputImage.setImage(new Image(result.toURI().toString()));
            updateLSBVisualization(resultLSB, result);
        } else {
            outputImage.setImage(null);
            resultLSB.setImage(null);
        }
    }

    /**
     * Обновляет визуализацию LSB для заданного изображения.
     *
     * @param imageView Отображение для визуализации LSB.
     * @param file Файл изображения.
     */
    private void updateLSBVisualization(ImageView imageView, File file) {
        try {
            BufferedImage lsbImage = LSBVisualization.visualizeLSBBits(ImageIO.read(file));
            if (lsbImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(lsbImage, null);
                imageView.setImage(fxImage);
            } else {
                showAlert("Ошибка создания визуализации LSB.");
            }
        } catch (IOException e) {
            showAlert("Ошибка чтения изображения при обновлении визуализации LSB: " + e.getMessage());
            logger.error("Ошибка чтения изображения при обновлении визуализации LSB: {}", e.getMessage(), e);
        }
    }

    /**
     * Выбирает файл для сохранения результирующего изображения.
     *
     * @return Выбранный файл или {@code null}, если выбор отменен.
     */
    private File chooseSaveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить изображение");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP Files", "*.bmp"));
        return chooser.showSaveDialog(null);
    }

    /**
     * Извлекает текст из изображения.
     */
    private void extractText() {
        if (inputFile == null) {
            showAlert("Выберите изображение для извлечения текста.");
            return;
        }
        try {
            String extracted = ExtractText.extractText(inputFile.getAbsolutePath(), DELIMITER);
            if (extracted.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Текст не найден в изображении.");
                logger.warn("Текст не найден в изображении {}", inputFile.getName());
            } else {
                outputText.setText(extracted);
                showAlert("Текст успешно извлечен!");
                logger.info("Текст успешно извлечен из изображения {}", inputFile.getName());
            }
        } catch (IOException | IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка извлечения текста: " + e.getMessage());
            logger.error("Ошибка извлечения текста: {}", e.getMessage(), e);
        }
    }

    /**
     * Отображает диалоговое окно с сообщением.
     *
     * @param message Сообщение для отображения.
     */
    private void showAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    /**
     * Отображает диалоговое окно с сообщением.
     *
     * @param type Тип диалогового окна (ошибка, информация и т.д.).
     * @param message Сообщение для отображения.
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Ошибка" : "Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Главный метод запуска приложения.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

