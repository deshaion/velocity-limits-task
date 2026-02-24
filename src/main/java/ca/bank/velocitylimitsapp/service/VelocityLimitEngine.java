package ca.bank.velocitylimitsapp.service;

import ca.bank.velocitylimitsapp.model.VelocityStats;
import ca.bank.velocitylimitsapp.model.Payload;
import ca.bank.velocitylimitsapp.model.Response;

public interface VelocityLimitEngine {
    Response process(Payload payload, VelocityStats velocityStats);
}
