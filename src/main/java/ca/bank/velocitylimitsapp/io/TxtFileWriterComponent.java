package ca.bank.velocitylimitsapp.io;

import ca.bank.velocitylimitsapp.model.Response;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class TxtFileWriterComponent implements ResponseWriter {
    private final String outputPath;
    private final ObjectMapper objectMapper;

    public TxtFileWriterComponent(@Value("${output.file:output.txt}") String outputPath) {
        this.outputPath = outputPath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void writeLine(Response response) throws IOException {
        String jsonString = objectMapper.writeValueAsString(response) + System.lineSeparator();

        Files.write(Path.of(outputPath), jsonString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
