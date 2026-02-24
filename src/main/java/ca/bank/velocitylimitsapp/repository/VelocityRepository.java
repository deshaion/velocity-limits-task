package ca.bank.velocitylimitsapp.repository;

import ca.bank.velocitylimitsapp.model.VelocityStats;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class VelocityRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean isDuplicate(String id, String customerId) {
        String sql = "SELECT COUNT(*) FROM load_attempts WHERE id = :id AND customer_id = :customerId";
        Integer count = jdbcTemplate.queryForObject(sql,
                Map.of(
                "id", id,
                "customerId", customerId),
                Integer.class
        );
        return count != null && count > 0;
    }

    public VelocityStats getCustomerStats(String customerId, OffsetDateTime transactionTime) {
        OffsetDateTime startOfDay = transactionTime.truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime startOfWeek = startOfDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        String sql = """
            SELECT
                COUNT(CASE WHEN attempt_time >= :dayStart THEN 1 END) AS daily_count,
                COALESCE(SUM(CASE WHEN attempt_time >= :dayStart THEN load_amount ELSE 0 END), 0) AS daily_sum,
                COALESCE(SUM(load_amount), 0) AS weekly_sum
            FROM load_attempts
            WHERE customer_id = :customerId
              AND accepted = true
              AND attempt_time >= :weekStart
        """;

        Map<String, Object> params = Map.of(
                "dayStart", Timestamp.from(startOfDay.toInstant()),
                "weekStart", Timestamp.from(startOfWeek.toInstant()),
                "customerId", customerId
        );

        return jdbcTemplate.queryForObject(
                sql,
                params,
                (rs, rowNum) -> VelocityStats.builder()
                        .customerId(customerId)
                        .dailyLoadCount(rs.getInt("daily_count"))
                        .dailyTotalAmount(rs.getBigDecimal("daily_sum"))
                        .weeklyTotalAmount(rs.getBigDecimal("weekly_sum"))
                        .build()
        );
    }

    public void saveAttempt(String id, String customerId, BigDecimal amount, OffsetDateTime time, boolean accepted) {
        String sql = "INSERT INTO load_attempts (id, customer_id, load_amount, attempt_time, accepted) VALUES (:id, :customerId, :amount, :time, :accepted)";

        jdbcTemplate.update(sql, Map.of(
                "id", id,
                "customerId", customerId,
                "amount", amount,
                "time", Timestamp.from(time.toInstant()),
                "accepted", accepted
        ));
    }
}
