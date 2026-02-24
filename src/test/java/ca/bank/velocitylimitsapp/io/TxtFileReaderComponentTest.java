package ca.bank.velocitylimitsapp.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TxtFileReaderComponentTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadAsStream() throws IOException {
        Path inputFile = tempDir.resolve("input.txt");
        List<String> lines = List.of("line1", "line2", "line3");
        Files.write(inputFile, lines);

        TxtFileReaderComponent reader = new TxtFileReaderComponent(inputFile.toString());

        try (Stream<String> stream = reader.readAsStream()) {
            List<String> result = stream.collect(Collectors.toList());
            assertEquals(lines, result);
        }
    }
}
