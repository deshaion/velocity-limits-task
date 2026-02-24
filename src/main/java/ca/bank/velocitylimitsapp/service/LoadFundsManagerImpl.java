package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.io.ResponseWriter;
import ca.bank.velocitylimitsapp.io.TransactionReader;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadFundsManagerImpl implements LoadFundsManager {
    private final TransactionReader transactionReader;
    private final ResponseWriter responseWriter;
    private final VelocityLimitEngineImpl velocityLimitEngine;
    private final VelocityStatsService velocityStatsService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @SneakyThrows(IOException.class)
    @Override
    public void load() {
        transactionReader.readAsStream().forEach(this::processLine);
    }

    @SneakyThrows(IOException.class)
    private void processLine(String line) {
        Payload payload = objectMapper.readValue(line, Payload.class);

        if (velocityStatsService.isDuplicate(payload)) {
            // ignore without processing
            log.debug("Load attempt is ignored as duplicate for id {} and customer {}",
                    payload.getId(), payload.getCustomerId());
            return;
        }

        Response response = velocityLimitEngine.process(payload, velocityStatsService.getVelocityStats(payload));

        velocityStatsService.saveLoad(payload, response);

        responseWriter.writeLine(response);
    }
}
