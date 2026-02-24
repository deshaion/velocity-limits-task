package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;
import ca.bank.velocitylimitsapp.repository.VelocityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VelocityStatsServiceImpl implements VelocityStatsService {
    private final VelocityRepository velocityRepository;

    @Override
    public boolean isDuplicate(Payload payload) {
        return velocityRepository.isDuplicate(payload.getId(), payload.getCustomerId());
    }

    @Override
    public VelocityStats getVelocityStats(Payload payload) {
        return velocityRepository.getCustomerStats(payload.getCustomerId(), payload.getTime());
    }

    @Override
    public void saveLoad(Payload payload, Response response) {
        velocityRepository.saveAttempt(payload.getId(), payload.getCustomerId(), payload.getLoadAmount(), payload.getTime(), response.getAccepted());
    }
}
