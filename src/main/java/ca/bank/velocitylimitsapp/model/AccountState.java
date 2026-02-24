package ca.bank.velocitylimitsapp.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class AccountState {
    private String customerId;
    private BigDecimal dailyTotalAmount;
    private BigDecimal weeklyTotalAmount;
    private int dailyLoadCount;
}
