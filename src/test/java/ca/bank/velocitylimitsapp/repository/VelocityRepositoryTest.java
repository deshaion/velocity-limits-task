package ca.bank.velocitylimitsapp.repository;

import ca.bank.velocitylimitsapp.model.VelocityStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(VelocityRepository.class)
class VelocityRepositoryTest {

    @Autowired
    private VelocityRepository velocityRepository;

    @Test
    void testSaveAndDuplicate() {
        String id = "1";
        String customerId = "123";
        OffsetDateTime time = OffsetDateTime.now();

        velocityRepository.saveAttempt(id, customerId, new BigDecimal("100.00"), time, true);

        assertTrue(velocityRepository.isDuplicate(id, customerId));
        assertFalse(velocityRepository.isDuplicate("2", customerId));
    }

    @Test
    void testGetCustomerStats() {
        String customerId = "stats-test";
        OffsetDateTime now = OffsetDateTime.parse("2023-10-25T12:00:00Z"); // Wednesday

        // Same day, accepted
        velocityRepository.saveAttempt("1", customerId, new BigDecimal("100.00"), now.minusHours(1), true);
        // Same day, rejected (should not count towards amount)
        velocityRepository.saveAttempt("2", customerId, new BigDecimal("200.00"), now.minusHours(2), false);
        // Previous day (should count towards weekly but not daily)
        velocityRepository.saveAttempt("3", customerId, new BigDecimal("50.00"), now.minusDays(1), true);
        // Previous week (should not count)
        velocityRepository.saveAttempt("4", customerId, new BigDecimal("1000.00"), now.minusDays(8), true);

        VelocityStats stats = velocityRepository.getCustomerStats(customerId, now);

        // Daily count: 1 (only the accepted one today)
        assertEquals(1, stats.getDailyLoadCount());
        // Daily total: 100.00
        assertEquals(0, new BigDecimal("100.00").compareTo(stats.getDailyTotalAmount()));
        // Weekly total: 100.00 + 50.00 = 150.00
        assertEquals(0, new BigDecimal("150.00").compareTo(stats.getWeeklyTotalAmount()));
    }
}
