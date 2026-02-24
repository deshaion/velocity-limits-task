package ca.bank.velocitylimitsapp.io;

import ca.bank.velocitylimitsapp.model.Response;

import java.io.IOException;

public interface ResponseWriter {
    void writeLine(Response response) throws IOException;
}
