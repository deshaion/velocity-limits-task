package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.config.LoadLimitProperties;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityLimitEngineImplTest {

    @Mock
    private LoadLimitProperties loadLimitProperties;

    @InjectMocks
    private VelocityLimitEngineImpl velocityLimitEngine;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
    }

    private Payload createPayload(String id, String customerId, String amount, String time) throws JsonProcessingException {
        String json = String.format("{\"id\":\"%s\",\"customer_id\":\"%s\",\"load_amount\":\"%s\",\"time\":\"%s\"}", id, customerId, amount, time);
        return objectMapper.readValue(json, Payload.class);
    }

    @Test
    void testProcessAccepted() throws JsonProcessingException {
        when(loadLimitProperties.getDailyCount()).thenReturn(3);
        when(loadLimitProperties.getDailyAmount()).thenReturn(new BigDecimal("5000.00"));
        when(loadLimitProperties.getWeeklyAmount()).thenReturn(new BigDecimal("20000.00"));

        Payload payload = createPayload("1", "123", "$100.00", "2022-01-01T12:00:00Z");
        VelocityStats stats = VelocityStats.builder()
                .customerId("123")
                .dailyLoadCount(0)
                .dailyTotalAmount(BigDecimal.ZERO)
                .weeklyTotalAmount(BigDecimal.ZERO)
                .build();

        Response response = velocityLimitEngine.process(payload, stats);

        assertTrue(response.getAccepted());
        assertEquals("1", response.getId());
        assertEquals("123", response.getCustomerId());
    }

    @Test
    void testProcessDailyCountExceeded() throws JsonProcessingException {
        when(loadLimitProperties.getDailyCount()).thenReturn(3);

        Payload payload = createPayload("1", "123", "$100.00", "2022-01-01T12:00:00Z");
        VelocityStats stats = VelocityStats.builder()
                .customerId("123")
                .dailyLoadCount(3) // Already at limit
                .dailyTotalAmount(BigDecimal.ZERO)
                .weeklyTotalAmount(BigDecimal.ZERO)
                .build();

        Response response = velocityLimitEngine.process(payload, stats);

        assertFalse(response.getAccepted());
    }

    @Test
    void testProcessDailyAmountExceeded() throws JsonProcessingException {
        when(loadLimitProperties.getDailyCount()).thenReturn(3);
        when(loadLimitProperties.getDailyAmount()).thenReturn(new BigDecimal("5000.00"));

        Payload payload = createPayload("1", "123", "$1000.00", "2022-01-01T12:00:00Z");
        VelocityStats stats = VelocityStats.builder()
                .customerId("123")
                .dailyLoadCount(0)
                .dailyTotalAmount(new BigDecimal("4500.00")) // Adding 1000 will exceed 5000
                .weeklyTotalAmount(BigDecimal.ZERO)
                .build();

        Response response = velocityLimitEngine.process(payload, stats);

        assertFalse(response.getAccepted());
    }

    @Test
    void testProcessWeeklyAmountExceeded() throws JsonProcessingException {
        when(loadLimitProperties.getDailyCount()).thenReturn(3);
        when(loadLimitProperties.getDailyAmount()).thenReturn(new BigDecimal("5000.00"));
        when(loadLimitProperties.getWeeklyAmount()).thenReturn(new BigDecimal("20000.00"));

        Payload payload = createPayload("1", "123", "$1000.00", "2022-01-01T12:00:00Z");
        VelocityStats stats = VelocityStats.builder()
                .customerId("123")
                .dailyLoadCount(0)
                .dailyTotalAmount(BigDecimal.ZERO)
                .weeklyTotalAmount(new BigDecimal("19500.00")) // Adding 1000 will exceed 20000
                .build();

        Response response = velocityLimitEngine.process(payload, stats);

        assertFalse(response.getAccepted());
    }
}
