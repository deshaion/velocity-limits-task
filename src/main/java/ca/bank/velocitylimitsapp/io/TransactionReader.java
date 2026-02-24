package ca.bank.velocitylimitsapp.io;

import java.io.IOException;
import java.util.stream.Stream;

public interface TransactionReader {

    Stream<String> readAsStream() throws IOException;
}
