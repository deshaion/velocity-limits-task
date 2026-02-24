package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;
import ca.bank.velocitylimitsapp.repository.VelocityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VelocityStatsServiceImplTest {

    @Mock
    private VelocityRepository velocityRepository;

    @InjectMocks
    private VelocityStatsServiceImpl velocityStatsService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Payload createPayload(String id, String customerId, String amount, String time) throws JsonProcessingException {
        String json = String.format("{\"id\":\"%s\",\"customer_id\":\"%s\",\"load_amount\":\"%s\",\"time\":\"%s\"}", id, customerId, amount, time);
        return objectMapper.readValue(json, Payload.class);
    }

    @Test
    void testIsDuplicate() throws JsonProcessingException {
        Payload payload = createPayload("1", "123", "$100.00", "2022-01-01T12:00:00Z");
        when(velocityRepository.isDuplicate("1", "123")).thenReturn(true);

        boolean result = velocityStatsService.isDuplicate(payload);

        assertTrue(result);
        verify(velocityRepository).isDuplicate("1", "123");
    }

    @Test
    void testGetVelocityStats() throws JsonProcessingException {
        Payload payload = createPayload("1", "123", "$100.00", "2022-01-01T12:00:00Z");
        VelocityStats stats = VelocityStats.builder().build();
        when(velocityRepository.getCustomerStats(eq("123"), any(OffsetDateTime.class))).thenReturn(stats);

        VelocityStats result = velocityStatsService.getVelocityStats(payload);

        assertEquals(stats, result);
        verify(velocityRepository).getCustomerStats(eq("123"), any(OffsetDateTime.class));
    }

    @Test
    void testSaveLoad() throws JsonProcessingException {
        Payload payload = createPayload("1", "123", "$100.00", "2022-01-01T12:00:00Z");
        Response response = Response.builder().accepted(true).build();

        velocityStatsService.saveLoad(payload, response);

        verify(velocityRepository).saveAttempt(eq("1"), eq("123"), eq(new BigDecimal("100.00")), any(OffsetDateTime.class), eq(true));
    }
}
