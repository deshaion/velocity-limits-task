package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.config.LoadLimitProperties;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VelocityLimitEngineImpl implements VelocityLimitEngine {
    private final LoadLimitProperties loadLimitProperties;

    @Override
    public Response process(Payload payload, VelocityStats velocityStats) {
        Response.ResponseBuilder responseBuilder = Response.builder()
                .id(payload.getId())
                .customerId(payload.getCustomerId());

        if (velocityStats.getDailyLoadCount() + 1 > loadLimitProperties.getDailyCount()) {
            log.debug("Daily load count limit exceeded for customer {}: current={}, limit={}",
                    payload.getCustomerId(), velocityStats.getDailyLoadCount(), loadLimitProperties.getDailyCount());
            return responseBuilder.accepted(false).build();
        }

        if (velocityStats.getDailyTotalAmount().add(payload.getLoadAmount()).compareTo(loadLimitProperties.getDailyAmount()) > 0) {
            log.debug("Daily load amount limit exceeded for customer {}: total={}, adding={}, limit={}",
                    payload.getCustomerId(), velocityStats.getDailyTotalAmount(), payload.getLoadAmount(), loadLimitProperties.getDailyAmount());
            return responseBuilder.accepted(false).build();
        }

        if (velocityStats.getWeeklyTotalAmount().add(payload.getLoadAmount()).compareTo(loadLimitProperties.getWeeklyAmount()) > 0) {
            log.debug("Weekly load amount limit exceeded for customer {}: total={}, adding={}, limit={}",
                    payload.getCustomerId(), velocityStats.getWeeklyTotalAmount(), payload.getLoadAmount(), loadLimitProperties.getWeeklyAmount());
            return responseBuilder.accepted(false).build();
        }

        return responseBuilder.accepted(true).build();
    }
}
