package ca.bank.velocitylimitsapp.io;

import ca.bank.velocitylimitsapp.model.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TxtFileWriterComponentTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteLine() throws IOException {
        Path outputFile = tempDir.resolve("output.txt");
        TxtFileWriterComponent writer = new TxtFileWriterComponent(outputFile.toString());

        Response response1 = Response.builder()
                .id("1")
                .customerId("123")
                .accepted(true)
                .build();

        Response response2 = Response.builder()
                .id("2")
                .customerId("456")
                .accepted(false)
                .build();

        writer.writeLine(response1);
        writer.writeLine(response2);

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());

        // Basic JSON check
        assertTrue(lines.get(0).contains("\"id\":\"1\""));
        assertTrue(lines.get(1).contains("\"id\":\"2\""));
    }
}
