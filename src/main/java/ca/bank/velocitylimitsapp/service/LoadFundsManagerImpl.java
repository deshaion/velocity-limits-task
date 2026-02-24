package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.io.ResponseWriter;
import ca.bank.velocitylimitsapp.io.TransactionReader;
import ca.bank.velocitylimitsapp.model.AccountState;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoadFundsManagerImpl implements LoadFundsManager {
    private final TransactionReader transactionReader;
    private final ResponseWriter responseWriter;
    private final VelocityLimitEngineImpl velocityLimitEngine;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @SneakyThrows(IOException.class)
    @Override
    public void load() {
        transactionReader.readAsStream().forEach(this::processLine);
    }

    @SneakyThrows(IOException.class)
    private void processLine(String line) {
        Payload payload = objectMapper.readValue(line, Payload.class);

        // ignore if id was processed
        // getCurrentAccountState
        AccountState accountState = AccountState.builder().dailyLoadCount(0).dailyTotalAmount(BigDecimal.ZERO).weeklyTotalAmount(BigDecimal.ZERO).build();

        Response response = velocityLimitEngine.process(payload, accountState);
        //repository.saveTransaction(response);
        responseWriter.writeLine(response);
    }
}
