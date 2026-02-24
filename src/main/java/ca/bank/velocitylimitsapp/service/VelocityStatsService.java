package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;
import ca.bank.velocitylimitsapp.model.VelocityStats;

public interface VelocityStatsService {

    boolean isDuplicate(Payload payload);

    VelocityStats getVelocityStats(Payload payload);

    void saveLoad(Payload payload, Response response);
}
