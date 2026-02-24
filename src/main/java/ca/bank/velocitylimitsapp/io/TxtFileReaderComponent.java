package ca.bank.velocitylimitsapp.io;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class TxtFileReaderComponent implements TransactionReader {
    private final String filePath;

    public TxtFileReaderComponent(@Value("${input.file}") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Stream<String> readAsStream() throws IOException {
        return Files.lines(Path.of(filePath));
    }
}
