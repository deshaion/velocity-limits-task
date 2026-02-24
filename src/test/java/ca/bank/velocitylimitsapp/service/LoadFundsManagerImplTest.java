package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.io.ResponseWriter;
import ca.bank.velocitylimitsapp.io.TransactionReader;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadFundsManagerImplTest {

    @Mock
    private TransactionReader transactionReader;

    @Mock
    private ResponseWriter responseWriter;

    @Mock
    private VelocityLimitEngineImpl velocityLimitEngine;

    @Mock
    private VelocityStatsService velocityStatsService;

    @InjectMocks
    private LoadFundsManagerImpl loadFundsManager;

    @Test
    void testLoad() throws IOException {
        String jsonLine = "{\"id\":\"1\",\"customer_id\":\"123\",\"load_amount\":\"$100.00\",\"time\":\"2000-01-01T00:00:00Z\"}";
        when(transactionReader.readAsStream()).thenReturn(Stream.of(jsonLine));
        when(velocityStatsService.isDuplicate(any(Payload.class))).thenReturn(false);

        VelocityStats stats = VelocityStats.builder().build();
        when(velocityStatsService.getVelocityStats(any(Payload.class))).thenReturn(stats);

        Response response = Response.builder().accepted(true).build();
        when(velocityLimitEngine.process(any(Payload.class), eq(stats))).thenReturn(response);

        loadFundsManager.load();

        verify(velocityStatsService).saveLoad(any(Payload.class), eq(response));
        verify(responseWriter).writeLine(response);
    }

    @Test
    void testLoadDuplicate() throws IOException {
        String jsonLine = "{\"id\":\"1\",\"customer_id\":\"123\",\"load_amount\":\"$100.00\",\"time\":\"2000-01-01T00:00:00Z\"}";
        when(transactionReader.readAsStream()).thenReturn(Stream.of(jsonLine));
        when(velocityStatsService.isDuplicate(any(Payload.class))).thenReturn(true);

        loadFundsManager.load();

        verify(velocityLimitEngine, never()).process(any(), any());
        verify(velocityStatsService, never()).saveLoad(any(), any());
        verify(responseWriter, never()).writeLine(any());
    }
}
