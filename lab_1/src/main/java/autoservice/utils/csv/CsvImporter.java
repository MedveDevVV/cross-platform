package autoservice.utils.csv;

import autoservice.model.Identifiable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CsvImporter {
    public static <T extends Identifiable> @NotNull List<T> importFromCsv(
            Path filePath,
            Function<String[], T> fieldsToItem,
            String[] header) throws IOException {

        List<T> items = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

        if (lines.isEmpty()) {
            return items;
        }

        int startIndex = 0;
        String firstLine = lines.get(0);

        // Обработка BOM
        if (firstLine.startsWith("\uFEFF")) {
            firstLine = firstLine.substring(1);
            lines.set(0, firstLine);
        }

        // ВАЛИДАЦИЯ: проверяем, что первая строка - это ожидаемый заголовок
        String expectedHeader = String.join(";", header);
        if (firstLine.equals(expectedHeader)) {
            startIndex = 1;
        } else {
            System.out.println("Предупреждение: заголовок CSV не совпадает с ожидаемым");
            return items;
        }

        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = line.split(";");
            T item = fieldsToItem.apply(fields);
            items.add(item);
        }

        return items;
    }
}